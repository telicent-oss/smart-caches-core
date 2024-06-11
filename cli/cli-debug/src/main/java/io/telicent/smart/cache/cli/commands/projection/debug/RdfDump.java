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
package io.telicent.smart.cache.cli.commands.projection.debug;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.model.CommandMetadata;
import io.telicent.smart.cache.cli.commands.projection.AbstractKafkaRdfProjectionCommand;
import io.telicent.smart.cache.live.model.IODescriptor;
import io.telicent.smart.cache.payloads.RdfPayload;
import io.telicent.smart.cache.payloads.RdfPayloadException;
import io.telicent.smart.cache.projectors.NoOpProjector;
import io.telicent.smart.cache.projectors.Projector;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.kafka.serializers.RdfPayloadDeserializer;
import io.telicent.smart.cache.sources.kafka.serializers.RdfPayloadSerializer;
import org.apache.jena.rdfpatch.RDFPatchOps;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.utils.Bytes;

import java.util.Map;

/**
 * A debug command that dumps a Kafka topic to the console assuming the values are RDF Datasets
 */
@Command(name = "rdf-dump", description = "Dumps the RDF from a Knowledge topic to standard output")
public class RdfDump extends AbstractKafkaRdfProjectionCommand<Event<Bytes, RdfPayload>> {

    private static final Map<String, String>
            DEFAULT_NAMESPACES = Map.of("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdfs",
                                        "http://www.w3.org/2000/01/rdf-schema#", "ies",
                                        "http://ies.data.gov.uk/ontology/ies4#", "telicent",
                                        "http://telicent.io/ontology/");

    @Option(name = "--output-language", title = "RdfLanguage",
            description = "Specifies the desired RDF Output Language e.g. RDF/XML, defaults to TriG.  May be expressed as either a name or a Content Type.")
    private final String outputLang = Lang.TRIG.getName();

    @Override
    protected Serializer<Bytes> keySerializer() {
        return new BytesSerializer();
    }

    @Override
    protected Deserializer<Bytes> keyDeserializer() {
        return new BytesDeserializer();
    }

    @Override
    protected Serializer<RdfPayload> valueSerializer() {
        return new RdfPayloadSerializer();
    }

    @Override
    protected Deserializer<RdfPayload> valueDeserializer() {
        return new RdfPayloadDeserializer();
    }

    @Override
    protected String getThroughputItemsName() {
        return "RDF Graphs";
    }

    @Override
    protected Projector<Event<Bytes, RdfPayload>, Event<Bytes, RdfPayload>> getProjector() {
        return new NoOpProjector();
    }

    @Override
    protected Sink<Event<Bytes, RdfPayload>> prepareWorkSink() {
        final Lang lang = parseRdfLanguage();
        return item -> {
            try {
                if (item.value().isDataset()) {
                    DatasetGraph dsg = item.value().getDataset();
                    dsg.getDefaultGraph().getPrefixMapping().setNsPrefixes(DEFAULT_NAMESPACES);
                    RDFDataMgr.write(System.out, dsg, lang);
                } else if (item.value().isPatch()) {
                    RDFPatchOps.write(System.out, item.value().getPatch());
                }
            } catch (RdfPayloadException e) {
                System.err.println("Ignored malformed RDF event: " + e.getMessage());
            }
        };
    }

    private Lang parseRdfLanguage() {
        Lang lang = RDFLanguages.nameToLang(this.outputLang);
        if (lang == null) {
            lang = Lang.TRIG;
        }
        return lang;
    }

    @Override
    protected String getLiveReporterApplicationName(CommandMetadata metadata) {
        return "Kafka RDF Dumper";
    }

    @Override
    protected IODescriptor getLiveReporterOutputDescriptor() {
        return new IODescriptor("stdout", "stream");
    }
}
