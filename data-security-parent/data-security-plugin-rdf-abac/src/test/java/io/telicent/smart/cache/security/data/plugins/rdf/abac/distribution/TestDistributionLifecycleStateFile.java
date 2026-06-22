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
package io.telicent.smart.cache.security.data.plugins.rdf.abac.distribution;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class TestDistributionLifecycleStateFile {

    private Path stateFile;
    private Path tmpFile;
    private Path bakFile;
    private DistributionLifecycleStateFile reader;

    @BeforeClass
    public void setUp() throws IOException {
        this.stateFile = Files.createTempFile("scg-test-lifecycle-", ".json");
        this.tmpFile = Path.of(this.stateFile + ".tmp");
        this.bakFile = Path.of(this.stateFile + ".bak");
        this.reader = new DistributionLifecycleStateFile(this.stateFile, null);
    }

    @AfterClass
    public void tearDown() throws IOException {
        Files.deleteIfExists(this.stateFile);
        Files.deleteIfExists(this.tmpFile);
        Files.deleteIfExists(this.bakFile);
    }

    @Test
    public void activeGraphNodes_returnsActiveDistributions_whenStateFileIsValid() throws IOException {
        Files.writeString(this.stateFile, """
                {
                  "distributions" : {
                    "http://example/a" : "Active",
                    "http://example/b" : "Withdrawn"
                  }
                }
                """, StandardCharsets.UTF_8);

        Set<Node> active = this.reader.activeGraphNodes();

        Assert.assertEquals(active, Set.of(NodeFactory.createURI("http://example/a")));
    }

    @Test
    public void activeGraphNodes_isEmpty_whenStateFileMissing() {
        // The temp file was created in setUp; remove it so the primary, .tmp and .bak are all absent.
        final Path missingFile = this.stateFile.resolveSibling("does-not-exist.json");
        final DistributionLifecycleStateFile missingReader = new DistributionLifecycleStateFile(missingFile, null);

        final Set<Node> active = missingReader.activeGraphNodes();

        Assert.assertTrue(active.isEmpty(), "No state file present -> no active distributions");
    }

    @Test
    public void activeGraphNodes_dropsCachedActiveSet_whenAllCandidatesBecomeUnparseable() throws IOException {
        // Prime cache with a valid state file.
        Files.writeString(this.stateFile, """
                {
                  "distributions" : {
                    "http://example/a" : "Active"
                  }
                }
                """, StandardCharsets.UTF_8);

        final Set<Node> activeBefore = this.reader.activeGraphNodes();
        Assert.assertEquals(activeBefore, Set.of(NodeFactory.createURI("http://example/a")),
                "Pre-condition: primed cache contains the Active distribution");

        // Corrupt candidates.
        Files.writeString(this.stateFile, "this is not valid json - corruption padding to change size",
                StandardCharsets.UTF_8);
        Files.writeString(this.tmpFile, "also not valid json", StandardCharsets.UTF_8);
        Files.writeString(this.bakFile, "still not valid json", StandardCharsets.UTF_8);

        final Set<Node> activeAfter = this.reader.activeGraphNodes();
        Assert.assertTrue(activeAfter.isEmpty(),
                "All candidates unparseable -> active set must be dropped (fail closed); was " + activeAfter);
    }

    @Test
    public void activeGraphNodes_fallsBackToBackup_whenPrimaryIsUnparseable() throws IOException {
        Files.writeString(this.stateFile, "garbage", StandardCharsets.UTF_8);
        Files.writeString(this.bakFile, """
                {
                  "distributions" : {
                    "http://example/from-bak" : "Active"
                  }
                }
                """, StandardCharsets.UTF_8);

        final Set<Node> active = this.reader.activeGraphNodes();

        Assert.assertEquals(active, Set.of(NodeFactory.createURI("http://example/from-bak")),
                ".bak should be used when primary fails to parse");
    }

}
