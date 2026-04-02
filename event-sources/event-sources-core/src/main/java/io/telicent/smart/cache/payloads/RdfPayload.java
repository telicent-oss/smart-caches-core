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
package io.telicent.smart.cache.payloads;

import org.apache.jena.rdfpatch.RDFPatch;
import org.apache.jena.rdfpatch.RDFPatchOps;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.WebContent;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;

import java.io.ByteArrayInputStream;
import java.util.Objects;

import static org.apache.commons.lang3.Strings.CI;

/**
 * Represents an RDF Payload, this may either be a purely additive {@link DatasetGraph} or a mutative {@link RDFPatch}
 * containing an ordered sequence of additions and deletions.
 * <p>
 * As of {@code 0.14.0} payloads are lazily deserialised, meaning that they hold the raw RDF value, and only deserialize
 * it when a consumer actually accesses it for the first time via the {@link #getDataset()} or {@link #getPatch()}
 * methods.  This avoids malformed RDF, or RDF events with incorrect {@code Content-Type} headers, causing head of line
 * blocking because it allows for the event to always be read from Kafka.  Consumers need to take note of the new
 * {@link RdfPayloadException} that may be thrown by these methods when they attempt to deserialise an invalid RDF event
 * and handle these appropriately.
 * </p>
 */
public class RdfPayload extends LazyPayload<Either<DatasetGraph, RDFPatch>> {

    private static final CharSequence[] RDF_PATCH_CONTENT_TYPES = {
            WebContent.contentTypePatch, WebContent.contentTypePatchThrift
    };

    /**
     * Creates a new additive RDF Payload from a Dataset Graph
     *
     * @param dsg Dataset graph
     * @return Payload
     */
    public static RdfPayload of(DatasetGraph dsg) {
        return new RdfPayload(dsg);
    }

    /**
     * Creates a new mutative RDF Payload from an RDF Patch
     *
     * @param patch RDF Patch
     * @return Payload
     */
    public static RdfPayload of(RDFPatch patch) {
        return new RdfPayload(patch);
    }

    /**
     * Creates a new lazily deserialised RDF Payload
     *
     * @param contentType Content Type (if known, if {@code null} then a suitable default will be assumed)
     * @param rawData     Raw RDF payload data for later deserialization
     * @return RDF Payload
     */
    public static RdfPayload of(String contentType, byte[] rawData) {
        return new RdfPayload(contentType, rawData);
    }

    /**
     * Creates a lazily deserialised payload
     *
     * @param contentType Content Type (if known, if {@code null} a suitable default is assumed)
     * @param rawData     The raw data for lazy deserialisation
     */
    private RdfPayload(String contentType, byte[] rawData) {
        super(contentType, rawData);
    }

    /**
     * Creates a dataset payload
     *
     * @param dsg Dataset
     */
    private RdfPayload(DatasetGraph dsg) {
        super(new Either<>(Objects.requireNonNull(dsg, "Dataset cannot be null"), null));
    }

    /**
     * Creates a patch payload
     *
     * @param patch Patch
     */
    private RdfPayload(RDFPatch patch) {
        super(new Either<>(null, Objects.requireNonNull(patch, "Patch cannot be null")), WebContent.contentTypePatch);
    }

    @Override
    protected Either<DatasetGraph, RDFPatch> deserialize() {
        if (isRdfPatchContentType()) {
            return new Either<>(null, deserializePatch());
        } else {
            return new Either<>(deserializeDataset(), null);
        }
    }

    /**
     * Gets whether this payload contains an RDF Dataset and is thus purely additive
     *
     * @return True if contains a dataset, false otherwise
     */
    public boolean isDataset() {
        return !isRdfPatchContentType() || (this.isReady() && this.getValue().isA());
    }

    /**
     * Gets whether this payload contains an RDF Patch and thus may contain sequences of additions and deletions
     *
     * @return True if contains a patch, false otherwise
     */
    public boolean isPatch() {
        return isRdfPatchContentType() || (this.isReady() && this.getValue().isB());
    }

    /**
     * Gets the dataset for this payload (if any)
     *
     * @return Dataset, or {@code null} if not a dataset payload
     * @throws RdfPayloadException Thrown if the raw data for this payload cannot be parsed into a valid RDF Dataset
     */
    public DatasetGraph getDataset() {
        Either<DatasetGraph, RDFPatch> value = this.getValue();
        if (value.isA()) {
            return value.getA();
        } else {
            return null;
        }
    }

    protected DatasetGraph deserializeDataset() {
        if (this.isReady() && this.getValue().isA()) {
            return this.getValue().getA();
        }

        // Otherwise try to deserialise into a DatasetGraph now
        Lang lang = RDFLanguages.contentTypeToLang(contentType);
        Lang selectedLang = lang != null ? lang : Lang.NQUADS;
        try {
            DatasetGraph dsg = DatasetGraphFactory.create();
            RDFParserBuilder.create()
                            .lang(selectedLang)
                            .source(new ByteArrayInputStream(this.getRawData()))
                            .build()
                            .parse(dsg);
            // NB - We could just call toDatasetGraph() but that creates a much more expensive transactional dataset
            //      graph which tanks performance in our test scenarios
            return dsg;
        } catch (JenaException e) {
            throw new RdfPayloadException(String.format(
                    "Failed to deserialise RDF Payload, selected RDF Language '%s' based on Content-Type header '%s', which could not successfully parse the provided RDF data",
                    selectedLang.getName(), contentType), e);
        }
    }

    private boolean isRdfPatchContentType() {
        return CI.equalsAny(contentType, RDF_PATCH_CONTENT_TYPES);
    }

    /**
     * Gets the patch for this payload (if any)
     *
     * @return Patch, or {@code null} if not a patch payload
     * @throws RdfPayloadException Thrown if the raw data for this payload cannot be parsed into a valid RDF Patch
     */
    public RDFPatch getPatch() {
        Either<DatasetGraph, RDFPatch> value = this.getValue();
        if (value.isB()) {
            return value.getB();
        } else {
            return null;
        }
    }


    protected RDFPatch deserializePatch() {
        if (this.isReady() && this.getValue().isB()) {
            return this.getValue().getB();
        }

        // Otherwise try to deserialise now
        {
            try {
                RDFPatch patch = null;
                if (CI.equals(contentType, WebContent.contentTypePatch)) {
                    patch = RDFPatchOps.read(new ByteArrayInputStream(this.getRawData()));
                } else if (CI.equals(contentType, WebContent.contentTypePatchThrift)) {
                    patch = RDFPatchOps.readBinary(new ByteArrayInputStream(this.getRawData()));
                }

                if (patch == null) {
                    // NB - This code is essentially unreachable because we're already checking for all our supported
                    //      patch content types earlier, this serves mainly as a future-proofing should new patch
                    //      serialisations be introduced
                    throw new RdfPayloadException(String.format(
                            "Failed to deserialise RDF Payload, Content-Type '%s' is not a known RDF Patch serialisation",
                            contentType));
                } else {
                    return patch;
                }
            } catch (JenaException e) {
                throw new RdfPayloadException(String.format(
                        "Failed to deserialise RDF Payload, selected RDF Patch based on Content-Type header '%s', which could not successfully parse the provided RDF patch",
                        contentType), e);
            }
        }
    }
}
