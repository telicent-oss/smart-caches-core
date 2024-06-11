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
package io.telicent.smart.cache.sources.kafka.serializers;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;

/**
 * A Kafka deserializer that deserializes RDF Datasets represented using Jena's
 * {@link org.apache.jena.sparql.core.DatasetGraph} class
 * <p>
 * This will use either the configured default language, or the {@code Content-Type} header from the event headers, to
 * try and parse the event key/value as an RDF dataset.
 * </p>
 */
public class DatasetGraphDeserializer extends AbstractRdfDeserializer<DatasetGraph> {

    /**
     * Creates a new deserializer that defaults to {@link Lang#NQUADS}
     */
    public DatasetGraphDeserializer() {
        this(Lang.NQUADS);
    }

    /**
     * Creates a new deserializer with the given default language
     * <p>
     * This is declared as package private so that the default language can only be changed by explicitly deriving a
     * sub-class from this implementation, or in unit test cases.
     * </p>
     *
     * @param defaultLang Default language
     */
    DatasetGraphDeserializer(Lang defaultLang) {
        super(defaultLang);
    }

    @Override
    protected DatasetGraph deserializeInternal(RDFParser parser) {
        DatasetGraph g = DatasetGraphFactory.create();
        parser.parse(g);
        return g;
    }

}
