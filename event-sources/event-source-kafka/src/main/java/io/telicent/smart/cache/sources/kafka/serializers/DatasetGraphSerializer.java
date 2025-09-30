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

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFWriterBuilder;
import org.apache.jena.riot.RiotException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * A Kafka serializer for RDF Dataset Graphs
 */
public class DatasetGraphSerializer extends AbstractRdfSerdes implements Serializer<DatasetGraph> {
    /**
     * Creates a new serializer that uses NQuads as the output serialization
     */
    public DatasetGraphSerializer() {
        this(RDFLanguages.NQUADS);
    }

    /**
     * Creates a new serializer that uses the given RDF Language as the output serialization
     *
     * @param defaultLang Default language
     */
    public DatasetGraphSerializer(Lang defaultLang) {
        super(defaultLang);
    }

    /**
     * Prepares an RDF writer
     *
     * @param lang Language to use for the output serialization, if {@code null} defaults to the default language
     * @return RDF writer
     */
    protected RDFWriterBuilder prepareWriter(Lang lang) {
        return RDFWriterBuilder.create().lang(lang != null ? lang : this.defaultLang);
    }

    @Override
    public byte[] serialize(String topic, DatasetGraph data) {
        if (data == null) {
            return new byte[0];
        }
        return serializeInternal(data, null);
    }

    @Override
    public byte[] serialize(String topic, Headers headers, DatasetGraph data) {
        if (data == null) {
            return new byte[0];
        }

        String contentType = findContentType(headers);
        Lang lang = StringUtils.isNotBlank(contentType) ? RDFLanguages.contentTypeToLang(contentType) : null;
        return serializeInternal(data, lang);
    }

    /**
     * Serializes the dataset graph to a byte sequence
     *
     * @param data Dataset graph
     * @param lang Language, if {@code null} then the serializers default language is used
     * @return Serialized dataset graph
     */
    protected byte[] serializeInternal(DatasetGraph data, Lang lang) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            prepareWriter(lang).source(data).output(output);
            return output.toByteArray();
        } catch (RiotException e) {
            // CORE-976
            // We could have a payload where the Content-Type declared is a graph format which doesn't have a registered
            // dataset writer.  In this scenario we can still successfully serialize the payload if the dataset ONLY
            // contains a default graph.
            // Unfortunately Jena doesn't have a way for us to check in advance whether this is the case so we have to
            // rely on catching the exception and checking for the error message which is hacky but works as long as
            // they don't change their error messages
            if (Strings.CI.startsWith(e.getMessage(), "No dataset writer")) {
                List<Node> gs = IteratorUtils.toList(data.listGraphNodes());
                if (gs.isEmpty()) {
                    // Only the default graph exists so safe to write as a graph
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    prepareWriter(lang).source(data.getDefaultGraph()).output(output);
                    return output.toByteArray();
                } else {
                    throw new SerializationException(
                            "RDF serialization " + lang.getName() + " does not support named graphs but the dataset to be serialized has " + gs.size() + " named graph(s) present.  Please ensure the Content-Type header declares the MIME type of an RDF serialization that supports named graphs",
                            e);
                }
            }
            // Any other error then fail
            throw new SerializationException(e);
        } catch (Throwable e) {
            // If anything goes wrong serializing the dataset wrap into a Kafka exception
            throw new SerializationException(e);
        }
    }
}
