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
package io.telicent.smart.cache.security.data.plugins;

import io.telicent.smart.cache.security.data.DataAccessAuthorizer;
import io.telicent.smart.cache.security.data.labels.SecurityLabelsApplicator;
import io.telicent.smart.cache.security.data.labels.SecurityLabelsParser;
import io.telicent.smart.cache.security.data.labels.SecurityLabelsValidator;
import io.telicent.smart.cache.security.data.requests.RequestContext;
import org.apache.jena.sparql.core.DatasetGraph;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Optional;
import java.util.Set;

public class TestDefaultPlugin {

    private static final class DefaultPlugin implements DataSecurityPlugin {

        @Override
        public SecurityLabelsParser labelsParser() {
            return null;
        }

        @Override
        public SecurityLabelsValidator labelsValidator() {
            return null;
        }

        @Override
        public SecurityLabelsApplicator prepareLabelsApplicator(byte[] defaultLabel, DatasetGraph datasetGraph) {
            return null;
        }

        @Override
        public DataAccessAuthorizer prepareAuthorizer(RequestContext context) {
            return null;
        }

        @Override
        public void close() {

        }
    }

    @Test
    public void givenDefaultPlugin_whenInteracting_thenDefaultMethodsInvoked() {
        // Given
        DataSecurityPlugin defaultPlugin = new DefaultPlugin();

        // When and Then
        Assert.assertFalse(defaultPlugin.areLabelsStringSafe());
        Assert.assertEquals(defaultPlugin.prepareLabelsBackup(), Optional.empty());
        Assert.assertEquals(defaultPlugin.prepareLabelsRestore(), Optional.empty());
        Assert.assertEquals(defaultPlugin.prepareLabelsCompact(), Optional.empty());
        Assert.assertEquals(defaultPlugin.prepareLabelsRemover(), Optional.empty());
        Assert.assertEquals(defaultPlugin.prepareLabelsBackup(), Optional.empty());
        Assert.assertEquals(defaultPlugin.prepareLabelsModule(), Optional.empty());
        Assert.assertEquals(defaultPlugin.prepareFusekiSink(null, true, null), Optional.empty());
        Assert.assertNotNull(defaultPlugin.prepareLabelToNode());
        Assert.assertEquals(defaultPlugin.prepareDistributionLifecycleFilters(), Optional.empty());
        Assert.assertEquals(defaultPlugin.getReadOperations(), Set.of());
        Assert.assertEquals(defaultPlugin.getReadWriteOperations(), Set.of());
    }
}
