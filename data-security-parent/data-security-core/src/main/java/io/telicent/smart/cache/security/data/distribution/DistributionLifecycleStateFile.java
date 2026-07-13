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

/** Reads the active distribution set from the lifecycle state file. */
public class DistributionLifecycleStateFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistributionLifecycleStateFile.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String TMP_EXTENSION = ".tmp";
    private static final String BAK_EXTENSION = ".bak";
    private static final Cache EMPTY_CACHE = new Cache(null, null, Set.of(), Map.of(), false);

    private final Path stateFile;
    private final String applicationId;
    private volatile Cache cache = EMPTY_CACHE;

    public DistributionLifecycleStateFile(Path stateFile, String applicationId) {
        this.stateFile = Objects.requireNonNull(stateFile, "stateFile cannot be null");
        this.applicationId = StringUtils.trimToNull(applicationId);
    }

    public Set<Node> activeGraphNodes() {
        refresh();
        return this.cache.activeGraphs();
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
        if (candidates.isEmpty()) {
            this.cache = EMPTY_CACHE;
            return;
        }

        for (final Path candidate : candidates) {
            try {
                final byte[] content = Files.readAllBytes(candidate);
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
        LOGGER.warn("All candidate lifecycle state files for {} failed to parse - dropping cached active set",
                this.stateFile);
        this.cache = EMPTY_CACHE;
    }

    private List<Path> candidateFiles() {
        final List<Path> candidates = new ArrayList<>(3);
        addCandidate(candidates, this.stateFile);
        addCandidate(candidates, Path.of(this.stateFile + TMP_EXTENSION));
        addCandidate(candidates, Path.of(this.stateFile + BAK_EXTENSION));
        return candidates;
    }

    private static void addCandidate(List<Path> candidates, Path path) {
        if (Files.isRegularFile(path)) {
            candidates.add(path);
        }
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

            distributions.properties().forEach(entry ->  {
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
                    "Lifecycle state file " + candidate + " is for application " + fileApplication
                            + " not expected application " + this.applicationId);
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
