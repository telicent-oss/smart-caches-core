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
package io.telicent.smart.cache.sources.kafka.tombstones;

import io.telicent.smart.cache.sources.kafka.serializers.GraphDeserializer;
import io.telicent.smart.cache.sources.kafka.serializers.GraphSerializer;
import org.apache.jena.graph.Graph;
import org.apache.jena.sparql.graph.GraphFactory;

public class DockerTestKafkaTombstonesGraph extends AbstractKafkaTombstoneTests<Graph> {

    @Override
    protected Graph exemplarValue() {
        return GraphFactory.emptyGraph();
    }

    @Override
    protected Class<GraphDeserializer> valueDeserializerClass() {
        return GraphDeserializer.class;
    }

    @Override
    protected Class<GraphSerializer> valueSerializerClass() {
        return GraphSerializer.class;
    }
}
