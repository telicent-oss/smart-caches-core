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

import io.telicent.jena.abac.ABAC;
import io.telicent.jena.abac.DatasetFilterProvider;
import io.telicent.jena.abac.DefaultDatasetFilterProvider;
import io.telicent.jena.abac.SysABAC;
import io.telicent.jena.abac.attributes.syntax.AEX;
import io.telicent.jena.abac.core.AttributesStoreLocal;
import io.telicent.jena.abac.core.DatasetGraphABAC;
import io.telicent.jena.abac.labels.Labels;
import io.telicent.smart.cache.security.data.distribution.DistributionLifecycleFilters;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Path;

import static org.testng.Assert.*;

public class TestDistributionLifecycleFilters {

    private static final String STATE_FILE_PATH = "/tmp/scg-test-lifecycle-state.json";
    private static final String APPLICATION_ID = "some-application-id";

    private DatasetGraphABAC dataset;

    @BeforeMethod
    public void setUp() {
        this.dataset = ABAC.authzDataset(DatasetGraphFactory.createTxnMem(),
                AEX.strALLOW,
                Labels.createLabelsStoreMem(),
                SysABAC.denyLabel,
                new AttributesStoreLocal());
    }

    @Test
    public void installIfConfigured_returnsFalse_whenLifecycleFilterAlreadyInstalled() {
        final DistributionLifecycleDatasetFilterProvider existing = new DistributionLifecycleDatasetFilterProvider(
                new DistributionLifecycleStateFile(Path.of(STATE_FILE_PATH), null), null);
        dataset.setFilterProvider(existing);
        final DistributionLifecycleFilters filters = new RdfAbacDistributionLifecycleFilters();
        boolean installed = filters.installIfConfigured(dataset, APPLICATION_ID, STATE_FILE_PATH);

        assertFalse(installed, "Should not reinstall when a lifecycle filter is already present");
        assertSame(dataset.getFilterProvider(), existing,
                "Existing lifecycle filter provider should be left untouched");
    }

    @Test
    public void installIfConfigured_returnsTrue_whenInstallingWithExplicitArguments() {
        final DistributionLifecycleFilters filters = new RdfAbacDistributionLifecycleFilters();
        boolean installed = filters.installIfConfigured(dataset, APPLICATION_ID, STATE_FILE_PATH);

        assertTrue(installed, "Should install when explicit lifecycle arguments are provided");
        assertTrue(dataset.getFilterProvider() instanceof DistributionLifecycleDatasetFilterProvider,
                "Installed filter provider should be the lifecycle one");
    }

    @Test
    public void installIfConfigured_returnsTrue_andInstallsFreshFilter_whenStateFileConfigured() {
        assertNull(dataset.getFilterProvider(), "Pre-condition: dataset has no filter provider");
        final DistributionLifecycleFilters filters = new RdfAbacDistributionLifecycleFilters();
        boolean installed = filters.installIfConfigured(dataset, APPLICATION_ID, STATE_FILE_PATH);

        assertTrue(installed, "Should install when state file is configured");
        assertTrue(dataset.getFilterProvider() instanceof DistributionLifecycleDatasetFilterProvider,
                "Installed filter provider should be the lifecycle one");
    }

    @Test
    public void installIfConfigured_returnsTrue_andWrapsExistingDelegate_whenStateFileConfigured() {
        DatasetFilterProvider existingDelegate = new DefaultDatasetFilterProvider();
        dataset.setFilterProvider(existingDelegate);
        final DistributionLifecycleFilters filters = new RdfAbacDistributionLifecycleFilters();
        boolean installed = filters.installIfConfigured(dataset, APPLICATION_ID, STATE_FILE_PATH);

        assertTrue(installed, "Should install (wrapping the existing delegate) when state file configured");
        assertTrue(dataset.getFilterProvider() instanceof DistributionLifecycleDatasetFilterProvider,
                "Installed filter provider should be the lifecycle one");
        // The integration test processorSCG_namedGraph_lifecycleFilterComposesWithExistingDatasetFilterProvider
        // in AbstractSmartCacheGraphSinkTests verifies that the delegate is actually invoked during query;
        // here we only need to verify that the wrapping install path returned true and replaced the field.
    }

    @Test
    public void installIfConfigured_returnsTrue_whenApplicationIdIsNull() {
        final DistributionLifecycleFilters filters = new RdfAbacDistributionLifecycleFilters();
        boolean installed = filters.installIfConfigured(dataset, null, STATE_FILE_PATH);

        assertTrue(installed, "Application id is optional - install should still succeed");
        assertTrue(dataset.getFilterProvider() instanceof DistributionLifecycleDatasetFilterProvider);
    }

}
