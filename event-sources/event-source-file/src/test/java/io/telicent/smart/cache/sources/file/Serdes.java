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
package io.telicent.smart.cache.sources.file;

import io.telicent.smart.cache.sources.Header;
import io.telicent.smart.cache.sources.file.rdf.RdfEventReaderWriter;
import io.telicent.smart.cache.sources.file.yaml.YamlEventReaderWriter;
import io.telicent.smart.cache.sources.kafka.serializers.*;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.common.utils.Bytes;

import java.util.Map;

public class Serdes {
    public static final StringSerializer STRING_SERIALIZER = new StringSerializer();
    public static final StringDeserializer STRING_DESERIALIZER = new StringDeserializer();
    public static final YamlEventReaderWriter<String, String> YAML_STRING_STRING =
            YamlEventReaderWriter.<String, String>create()
                                 .keySerializer(STRING_SERIALIZER)
                                 .valueSerializer(STRING_SERIALIZER)
                                 .keyDeserializer(STRING_DESERIALIZER)
                                 .valueDeserializer(STRING_DESERIALIZER)
                                 .build();
    public static final IntegerSerializer INTEGER_SERIALIZER = new IntegerSerializer();
    public static final IntegerDeserializer INTEGER_DESERIALIZER = new IntegerDeserializer();
    public static final YamlEventReaderWriter<Integer, String> YAML_INTEGER_STRING =
            YamlEventReaderWriter.<Integer, String>create()
                                 .keySerializer(INTEGER_SERIALIZER)
                                 .valueSerializer(STRING_SERIALIZER)
                                 .keyDeserializer(INTEGER_DESERIALIZER)
                                 .valueDeserializer(STRING_DESERIALIZER)
                                 .build();
    public static final GraphSerializer GRAPH_SERIALIZER = new GraphSerializer();
    public static final GraphDeserializer GRAPH_DESERIALIZER = new GraphDeserializer();
    public static final DatasetGraphSerializer DATASET_GRAPH_SERIALIZER = new DatasetGraphSerializer();
    public static final DatasetGraphDeserializer DATASET_GRAPH_DESERIALIZER = new DatasetGraphDeserializer();
    public static final RdfEventReaderWriter<Integer, DatasetGraph>
            RDF_INTEGER_STRING =
            new RdfEventReaderWriter<>(INTEGER_DESERIALIZER, DATASET_GRAPH_DESERIALIZER,
                                       INTEGER_SERIALIZER, DATASET_GRAPH_SERIALIZER);
    public static final BytesSerializer BYTES_SERIALIZER = new BytesSerializer();
    public static final BytesDeserializer BYTES_DESERIALIZER = new BytesDeserializer();
    public static final YamlEventReaderWriter<Bytes, String> YAML_BYTES_STRING =
            YamlEventReaderWriter.<Bytes, String>create()
                                 .keySerializer(BYTES_SERIALIZER)
                                 .valueSerializer(STRING_SERIALIZER)
                                 .keyDeserializer(BYTES_DESERIALIZER)
                                 .valueDeserializer(STRING_DESERIALIZER)
                                 .build();

    public static final Serializer<Header> HEADER_SERIALIZER = new AbstractJacksonSerializer<>();

    public static final Deserializer<Header> HEADER_DESERIALIZER = new HeaderDeserializer();

    public static final Serializer<Map> MAP_SERIALIZER = new AbstractJacksonSerializer<>();

    public static final Deserializer<Map> MAP_DESERIALIZER = new MapDeserializer();
}
