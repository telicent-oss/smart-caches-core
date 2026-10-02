/**
 * Copyright (C) Telicent Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.telicent.smart.cache.security.data.distribution;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.telicent.smart.cache.distribution.lifecycle.DistributionLifecycleState;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Reads the active distribution set from the lifecycle state file.
 */
// java:S3077 - false positive: the value is an immutable record / a thread-safe Caffeine cache, so volatile is correct
@SuppressWarnings("java:S3077")
public class DistributionLifecycleStateFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistributionLifecycleStateFile.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String TMP_EXTENSION = ".tmp";
    private static final String BAK_EXTENSION = ".bak";
    /**
     * Special cache value used when {@link #refresh()} determines that state files are missing/empty and in a writeable
     * location. This is a legitimate occurrence on a fresh clean deployment where there may yet to be any lifecycle
     * events to be received and thus no lifecycle state to be written.  This serves to ensure that the
     * {@link #available()} method returns {@code true} and thus any health checks that rely on this correctly report
     * the application as healthy.
     */
    private static final Cache EMPTY_CACHE = new Cache(null, null, Set.of(), Map.of(), true);
    /**
     * Special cache value used when {@link #refresh()} determines that state files exist, are non-empty and invalid.
     * This serves to ensure the {@link #available()} method returns {@code false} and thus any health checks that rely
     * on this correctly report the application as unhealthy.
     */
    private static final Cache INVALID_CACHE = new Cache(null, null, Set.of(), Map.of(), false);

    private final Path stateFile;
    private final String applicationId;
    private volatile Cache cache = INVALID_CACHE;

    public DistributionLifecycleStateFile(Path stateFile, String applicationId) {
        this.stateFile = Objects.requireNonNull(stateFile, "stateFile cannot be null");
        this.applicationId = StringUtils.trimToNull(applicationId);
    }

    public Set<Node> activeGraphNodes() {
        refresh();
        return this.cache.activeGraphs();
    }

    public boolean available() {
        refresh();
        return this.cache.available();
    }

    public String distributionState(String distributionId) {
        return distributionStateResult(distributionId).state();
    }

    public DistributionStateResult distributionStateResult(String distributionId) {
        refresh();
        if (StringUtils.isBlank(distributionId)) {
            return new DistributionStateResult(null, this.cache.available());
        }
        return new DistributionStateResult(this.cache.distributionStates().get(distributionId), this.cache.available());
    }

    private synchronized void refresh() {
        final List<Path> candidates = candidateFiles();
        final List<Boolean> validEmpty = new ArrayList<>();
        for (final Path candidate : candidates) {
            try {
                // If the file doesn't exist yet this could be legitimate if no state has yet arrived to be written
                // Add it to the list of valid empty candidates if, and only if, the location is writeable
                if (!Files.exists(candidate)) {
                    validEmpty.add(verifyWriteableLocation(candidate));
                    continue;
                }

                final byte[] content = Files.readAllBytes(candidate);
                if (content.length == 0) {
                    // The file could exist but be empty, again this could be legitimate if something created a
                    // placeholder file and no state has yet arrived to be written
                    // Add it to the list of valid empty candidates if, and only if, the location is writeable
                    validEmpty.add(verifyWriteableLocation(candidate));
                    continue;
                } else {
                    // If non-empty then add a false to the list of valid empty candidates, if the file exists and
                    // non-empty then we'll try and parse it below.  In the case that it is invalid then we don't
                    // want to incorrectly report we're ready when we're not
                    validEmpty.add(false);
                }
                final String fingerprint = fingerprint(content);
                final Cache current = this.cache;
                if (Objects.equals(current.source(), candidate) && Objects.equals(current.fingerprint(), fingerprint)) {
                    return;
                }

                this.cache = loadState(candidate, fingerprint, content);
                return;
            } catch (IOException | IllegalArgumentException e) {
                LOGGER.warn("Failed to load distribution lifecycle state from {}", candidate, e);
            }
        }

        if (validEmpty.stream().allMatch(b -> b)) {
            // All candidates files were empty and in writeable locations so state file is available just not populated
            // yet.  This is expected in a clean slate deployment so return a ready but empty cache
            this.cache = EMPTY_CACHE;
        } else {
            // All candidate files were invalid, or in non-writeable location so state cannot be recorded in this
            // location and thus invalid configuration.  So return an unready and empty cache
            LOGGER.warn("All candidate lifecycle state files for {} failed to parse - dropping cached active set",
                        this.stateFile);
            this.cache = INVALID_CACHE;
        }
    }

    /**
     * Verifies that the candidate file is in a writeable location
     * <p>
     * This is important for distinguishing correct configuration on clean slate deployments (missing/empty state files)
     * versus incorrect configuration where the state file does not exist because the application cannot write to the
     * configured location.
     * </p>
     * <p>
     * This works by trying to write (and delete) a temporary file in the candidate state files directory, if we can do
     * that then the state file configuration is viable and an empty/missing file simply denotes a fresh deployment.  If
     * it fails then it indicates misconfiguration.
     * </p>
     *
     * @param candidate Candidate state file
     * @return True if the candidate state file is in a writeable location, false otherwise
     */
    private Boolean verifyWriteableLocation(Path candidate) {
        try {
            Path probe =
                    Files.createTempFile(Path.of(candidate.toAbsolutePath().toFile().getParentFile().getAbsolutePath()),
                                         "probe", ".txt");
            Files.delete(probe);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private List<Path> candidateFiles() {
        final List<Path> candidates = new ArrayList<>(3);
        candidates.add(this.stateFile);
        candidates.add(Path.of(this.stateFile + TMP_EXTENSION));
        candidates.add(Path.of(this.stateFile + BAK_EXTENSION));
        return candidates;
    }

    private Cache loadState(Path candidate, String fingerprint, byte[] content) throws IOException {
        try (final InputStream input = new java.io.ByteArrayInputStream(content)) {
            final JsonNode root = MAPPER.readTree(input);
            verifyApplication(root, candidate);

            final JsonNode distributions = root.path("distributions");
            if (!distributions.isObject()) {
                return new Cache(candidate, fingerprint, Set.of(), Map.of(), true);
            }

            final Set<Node> activeGraphs = new LinkedHashSet<>();
            final Map<String, String> distributionStates = new LinkedHashMap<>();

            distributions.properties().forEach(entry -> {
                final String state = entry.getValue().asText();
                distributionStates.put(entry.getKey(), state);
                if (!DistributionLifecycleState.Active.name().equals(entry.getValue().asText())) {
                    return;
                }
                activeGraphs.add(NodeFactory.createURI(entry.getKey()));
            });
            return new Cache(candidate, fingerprint, Collections.unmodifiableSet(activeGraphs),
                             Collections.unmodifiableMap(distributionStates), true);
        }
    }

    private void verifyApplication(JsonNode root, Path candidate) {
        if (this.applicationId == null) {
            return;
        }

        final String fileApplication = root.path("application").asText(null);
        if (!Objects.equals(this.applicationId, fileApplication)) {
            throw new IllegalArgumentException(
                    "Lifecycle state file " + candidate + " is for application " + fileApplication + " not expected application " + this.applicationId);
        }
    }

    private static String fingerprint(byte[] content) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(content);
            final StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(Character.forDigit((b >> 4) & 0xF, 16));
                builder.append(Character.forDigit(b & 0xF, 16));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 digest unavailable", e);
        }
    }

    public record DistributionStateResult(String state, boolean available) {
    }

    private record Cache(Path source, String fingerprint, Set<Node> activeGraphs,
                         Map<String, String> distributionStates, boolean available) {
    }

}
