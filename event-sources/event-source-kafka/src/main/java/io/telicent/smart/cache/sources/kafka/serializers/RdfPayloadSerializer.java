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

import io.telicent.smart.cache.payloads.RdfPayload;
import io.telicent.smart.cache.payloads.RdfPayloadException;
import org.apache.jena.rdfpatch.RDFPatch;
import org.apache.jena.rdfpatch.RDFPatchOps;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.WebContent;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;

import static org.apache.commons.lang3.Strings.CI;

/**
 * A Kafka serializer that serializes RDF Payloads
 */
public class RdfPayloadSerializer extends AbstractRdfSerdes implements Serializer<RdfPayload> {

    private final DatasetGraphSerializer dsgSerializer;

    /**
     * Creates a new payload serializer
     */
    public RdfPayloadSerializer() {
        this(Lang.NQUADS);
    }

    /**
     * Creates a new payload serializer
     *
     * @param defaultLang Default RDF language to use
     */
    public RdfPayloadSerializer(Lang defaultLang) {
        super(defaultLang);
        this.dsgSerializer = new DatasetGraphSerializer(defaultLang);
    }

    @Override
    public byte[] serialize(String topic, RdfPayload payload) {
        return serialize(topic, null, payload);
    }

    private static SerializationException unableToSerialize() {
        return new SerializationException(
                "Cannot serialize a RDF Payload as it is not valid for serialisation into the format indicated by the Content-Type header");
    }

    private static SerializationException unableToSerializePatch() {
        return new SerializationException(
                "Cannot serialize a RDF Payload containing a Patch without a suitable Content-Type Header");
    }

    @Override
    public byte[] serialize(String topic, Headers headers, RdfPayload payload) {
        if (payload == null) {
            return new byte[0];
        }
        try {
            if (payload.isDataset()) {
                return this.dsgSerializer.serialize(topic, headers, payload.getDataset());
            }

            // Serialize RDF Patches
            String contentType = findContentType(headers);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            RDFPatch patch = payload.getPatch();
            try {
                if (CI.equals(contentType, WebContent.ctPatch.getContentTypeStr())) {
                    RDFPatchOps.write(output, patch);
                    return output.toByteArray();
                } else if (CI.equals(contentType, WebContent.ctPatchThrift.getContentTypeStr())) {
                    RDFPatchOps.writeBinary(output, patch);
                    return output.toByteArray();
                }
            } catch (Throwable e) {
                // If anything goes wrong serializing the patch wrap into a Kafka exception
                throw new SerializationException(e);
            }
            throw unableToSerializePatch();
        } catch (RdfPayloadException e) {
            // This could happen if we have a lazily deserialised payload that is actually invalid, however as the raw
            // data is still stored we can just write it back to Kafka and leave it to the downstream consumers to
            // decide how to deal with the malformed events
            if (payload.hasRawData()) {
                return payload.getRawData();
            }
            throw unableToSerialize();
        }
    }
}
