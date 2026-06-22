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
package io.telicent.smart.cache.security.data.plugins.rdf.abac;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.telicent.jena.abac.ABAC;
import io.telicent.jena.abac.SysABAC;
import io.telicent.jena.abac.attributes.syntax.AEX;
import io.telicent.jena.abac.core.AttributesStoreLocal;
import io.telicent.jena.abac.core.DatasetGraphABAC;
import io.telicent.jena.abac.labels.Labels;
import io.telicent.jena.abac.labels.LabelsStore;
import io.telicent.smart.cache.storage.BackupRestoreCapable;
import io.telicent.smart.cache.storage.RestoreStatus;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.*;

public class TestRdfAbacLabelsRestore {

    private RdfAbacLabelsRestore restore;
    private ObjectNode node;

    @BeforeMethod
    public void setUp() {
        restore = new RdfAbacLabelsRestore();
        node = new ObjectMapper().createObjectNode();
    }

    // --- requestIsEmpty ---

    @Test
    public void givenNullPath_whenCheckingRequestIsEmpty_thenTrue() {
        Assert.assertTrue(RdfAbacLabelsRestore.requestIsEmpty(null));
    }

    @Test
    public void givenBlankPath_whenCheckingRequestIsEmpty_thenTrue() {
        Assert.assertTrue(RdfAbacLabelsRestore.requestIsEmpty("   "));
    }

    @Test
    public void givenRootSlash_whenCheckingRequestIsEmpty_thenTrue() {
        Assert.assertTrue(RdfAbacLabelsRestore.requestIsEmpty("/"));
    }

    @Test
    public void givenRootSlashWithSpaces_whenCheckingRequestIsEmpty_thenTrue() {
        Assert.assertTrue(RdfAbacLabelsRestore.requestIsEmpty("  /  "));
    }

    @Test
    public void givenValidPath_whenCheckingRequestIsEmpty_thenFalse() {
        Assert.assertFalse(RdfAbacLabelsRestore.requestIsEmpty("/tmp/backup"));
    }

    // --- checkPathExistsAndIsDir ---

    @Test
    public void givenNullPath_whenCheckingPathExistsAndIsDir_thenFalse() {
        Assert.assertFalse(RdfAbacLabelsRestore.checkPathExistsAndIsDir(null));
    }

    @Test
    public void givenNonExistentPath_whenCheckingPathExistsAndIsDir_thenFalse() {
        Assert.assertFalse(RdfAbacLabelsRestore.checkPathExistsAndIsDir("/nonexistent/path/xyz"));
    }

    @Test
    public void givenExistingFile_whenCheckingPathExistsAndIsDir_thenFalse() throws IOException {
        Path tempFile = Files.createTempFile("test-restore", ".tmp");
        try {
            Assert.assertFalse(RdfAbacLabelsRestore.checkPathExistsAndIsDir(tempFile.toString()));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    public void givenExistingDirectory_whenCheckingPathExistsAndIsDir_thenTrue() throws IOException {
        Path tempDir = Files.createTempDirectory("test-restore");
        try {
            Assert.assertTrue(RdfAbacLabelsRestore.checkPathExistsAndIsDir(tempDir.toString()));
        } finally {
            Files.delete(tempDir);
        }
    }

    // --- restore dispatch ---

    @Test
    public void givenNonAbacDataset_whenRestoring_thenSuccessFalse() {
        DatasetGraph plainDsg = DatasetGraphFactory.createTxnMem();
        restore.restore(plainDsg, "/tmp/backup", node);

        Assert.assertFalse(node.get("success").asBoolean());
        Assert.assertTrue(node.get("reason").asText().contains("not ABAC"));
    }

    @Test
    public void givenAbacDatasetWithPlainLabelsStore_whenRestoring_thenSuccessFalse() {
        DatasetGraphABAC abac = ABAC.authzDataset(DatasetGraphFactory.createTxnMem(),
                AEX.strALLOW, Labels.createLabelsStoreMem(), SysABAC.denyLabel, new AttributesStoreLocal());

        restore.restore(abac, "/tmp/backup", node);

        Assert.assertFalse(node.get("success").asBoolean());
        Assert.assertTrue(node.get("reason").asText().contains("not RocksDB"));
    }

    @Test
    public void givenAbacDatasetWithRestoreCapableStore_whenRestoring_thenSuccessTrue() throws IOException {
        Path tempDir = Files.createTempDirectory("test-restore");
        try {
            LabelsStore restorableStore = mock(LabelsStore.class, withSettings().extraInterfaces(BackupRestoreCapable.class));
            RestoreStatus successStatus = RestoreStatus.builder().success(true).build();
            when(((BackupRestoreCapable) restorableStore).restore(any())).thenReturn(successStatus);

            DatasetGraphABAC abac = ABAC.authzDataset(DatasetGraphFactory.createTxnMem(),
                    AEX.strALLOW, restorableStore, SysABAC.denyLabel, new AttributesStoreLocal());

            restore.restore(abac, tempDir.toString(), node);

            Assert.assertTrue(node.get("success").asBoolean());
        } finally {
            Files.delete(tempDir);
        }
    }

    @Test
    public void givenAbacDatasetWithRestoreCapableStoreThatFails_whenRestoring_thenSuccessFalse() throws IOException {
        Path tempDir = Files.createTempDirectory("test-restore");
        try {
            LabelsStore restorableStore = mock(LabelsStore.class, withSettings().extraInterfaces(BackupRestoreCapable.class));
            RestoreStatus failStatus = RestoreStatus.builder().success(false).errorMessage("restore failed").build();
            when(((BackupRestoreCapable) restorableStore).restore(any())).thenReturn(failStatus);

            DatasetGraphABAC abac = ABAC.authzDataset(DatasetGraphFactory.createTxnMem(),
                    AEX.strALLOW, restorableStore, SysABAC.denyLabel, new AttributesStoreLocal());

            restore.restore(abac, tempDir.toString(), node);

            Assert.assertFalse(node.get("success").asBoolean());
            Assert.assertEquals(node.get("reason").asText(), "restore failed");
        } finally {
            Files.delete(tempDir);
        }
    }

    @Test
    public void givenAbacDatasetWithRestoreCapableStoreThatThrows_whenRestoring_thenSuccessFalse() throws IOException {
        Path tempDir = Files.createTempDirectory("test-restore");
        try {
            LabelsStore restorableStore = mock(LabelsStore.class, withSettings().extraInterfaces(BackupRestoreCapable.class));
            when(((BackupRestoreCapable) restorableStore).restore(any())).thenThrow(new RuntimeException("unexpected"));

            DatasetGraphABAC abac = ABAC.authzDataset(DatasetGraphFactory.createTxnMem(),
                    AEX.strALLOW, restorableStore, SysABAC.denyLabel, new AttributesStoreLocal());

            restore.restore(abac, tempDir.toString(), node);

            Assert.assertFalse(node.get("success").asBoolean());
            Assert.assertEquals(node.get("reason").asText(), "unexpected");
        } finally {
            Files.delete(tempDir);
        }
    }

    // --- executeRestore ---

    @Test
    public void givenRestoreCapableStore_whenExecuteRestore_thenNodePopulated() {
        BackupRestoreCapable restoreCapable = mock(BackupRestoreCapable.class);
        RestoreStatus successStatus = RestoreStatus.builder().success(true).build();
        when(restoreCapable.restore(any())).thenReturn(successStatus);

        restore.executeRestore(restoreCapable, "/tmp/backup", node);

        Assert.assertTrue(node.get("success").asBoolean());
    }

    @Test
    public void givenRestoreCapableStoreWithError_whenExecuteRestore_thenReasonPopulated() {
        BackupRestoreCapable restoreCapable = mock(BackupRestoreCapable.class);
        RestoreStatus failStatus = RestoreStatus.builder().success(false).errorMessage("io error").build();
        when(restoreCapable.restore(any())).thenReturn(failStatus);

        restore.executeRestore(restoreCapable, "/tmp/backup", node);

        Assert.assertFalse(node.get("success").asBoolean());
        Assert.assertEquals(node.get("reason").asText(), "io error");
    }
}