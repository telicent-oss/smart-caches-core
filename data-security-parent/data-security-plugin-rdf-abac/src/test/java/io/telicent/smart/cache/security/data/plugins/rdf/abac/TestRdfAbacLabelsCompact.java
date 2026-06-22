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

import io.telicent.jena.abac.ABAC;
import io.telicent.jena.abac.SysABAC;
import io.telicent.jena.abac.attributes.syntax.AEX;
import io.telicent.jena.abac.core.AttributesStoreLocal;
import io.telicent.jena.abac.core.DatasetGraphABAC;
import io.telicent.jena.abac.labels.Labels;
import io.telicent.jena.abac.labels.LabelsStore;
import io.telicent.smart.cache.security.data.DataSecurityException;
import io.telicent.smart.cache.storage.CompactCapable;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public class TestRdfAbacLabelsCompact {

    private RdfAbacLabelsCompact compact;

    @BeforeMethod
    public void setUp() {
        compact = new RdfAbacLabelsCompact();
    }

    @Test
    public void givenNonAbacDataset_whenCompacting_thenNoOpWithoutError() throws DataSecurityException {
        DatasetGraph plainDsg = DatasetGraphFactory.createTxnMem();
        compact.compact(plainDsg);
        // No exception — compaction is silently skipped for non-ABAC datasets
    }

    @Test
    public void givenAbacDatasetWithPlainLabelsStore_whenCompacting_thenNoOpWithoutError() throws DataSecurityException {
        // Labels.createLabelsStoreMem() returns a plain LabelsStore (not CompactCapable/RocksDB)
        DatasetGraphABAC abac = ABAC.authzDataset(DatasetGraphFactory.createTxnMem(),
                AEX.strALLOW, Labels.createLabelsStoreMem(), SysABAC.denyLabel, new AttributesStoreLocal());
        compact.compact(abac);
        // No exception — plain store has no compaction to do
    }

    @Test
    public void givenAbacDatasetWithCompactCapableLabelsStore_whenCompacting_thenCompactIsCalled() throws Exception {
        LabelsStore compactableStore = mock(LabelsStore.class, withSettings().extraInterfaces(CompactCapable.class));
        DatasetGraphABAC abac = ABAC.authzDataset(DatasetGraphFactory.createTxnMem(),
                AEX.strALLOW, compactableStore, SysABAC.denyLabel, new AttributesStoreLocal());

        compact.compact(abac);

        verify((CompactCapable) compactableStore).compact();
    }

    @Test(expectedExceptions = DataSecurityException.class)
    public void givenCompactCapableStoreThatThrows_whenCompacting_thenDataSecurityException() throws Exception {
        LabelsStore compactableStore = mock(LabelsStore.class, withSettings().extraInterfaces(CompactCapable.class));
        doThrow(new RuntimeException("compact failed")).when((CompactCapable) compactableStore).compact();
        DatasetGraphABAC abac = ABAC.authzDataset(DatasetGraphFactory.createTxnMem(),
                AEX.strALLOW, compactableStore, SysABAC.denyLabel, new AttributesStoreLocal());

        compact.compact(abac);
    }
}