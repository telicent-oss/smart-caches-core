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

import io.telicent.jena.abac.core.DatasetGraphABAC;
import io.telicent.jena.abac.labels.LabelsStore;
import io.telicent.jena.abac.labels.store.rocksdb.legacy.LegacyLabelsStoreRocksDB;
import io.telicent.smart.cache.security.data.DataSecurityException;
import io.telicent.smart.cache.security.data.labels.SecurityLabelsCompact;
import io.telicent.smart.cache.storage.CompactCapable;
import org.apache.jena.atlas.lib.Timer;
import org.apache.jena.sparql.core.DatasetGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RdfAbacLabelsCompact implements SecurityLabelsCompact {

    static final Logger LOGGER = LoggerFactory.getLogger(RdfAbacLabelsCompact.class);

    @Override
    public void compact(DatasetGraph dsg) throws DataSecurityException {

        if (dsg instanceof DatasetGraphABAC abac) {
            try (final LabelsStore labelsStore = abac.labelsStore()) {
                final Timer timer = new Timer();
                timer.startTimer();
                LOGGER.info("[Compaction] >>>> Start label store compaction.");
                if (labelsStore instanceof LegacyLabelsStoreRocksDB rocksDB) {
                    rocksDB.compact();
                } else if (labelsStore instanceof CompactCapable compactCapable) {
                    compactCapable.compact();
                }
                LOGGER.info("[Compaction] <<<< Finish label store compaction. Took {} seconds.",
                        Timer.timeStr(timer.endTimer()));
                return;
            } catch (Exception e) {
                throw new DataSecurityException(e.getMessage(),e);
            }
        }
        LOGGER.info("[Compaction] <<<< Label store compaction not needed.");
    }
}
