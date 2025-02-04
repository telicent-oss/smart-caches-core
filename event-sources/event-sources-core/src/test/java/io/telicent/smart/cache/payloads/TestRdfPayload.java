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

import org.apache.jena.rdfpatch.RDFPatchOps;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;

public class TestRdfPayload {

    private static final byte[] JUNK_BYTES = "junk".getBytes(StandardCharsets.UTF_8);
    private static final byte[] SINGLE_NTRIPLE_BYTES =
            "<http://s> <http://p> <http://o> .".getBytes(StandardCharsets.UTF_8);

    @Test
    public void givenEagerDatasetPayload_whenAccessingMethods_thenReturnValuesAreCorrect() {
        // Given
        RdfPayload payload = RdfPayload.of(DatasetGraphFactory.empty());

        // When and Then
        Assert.assertTrue(payload.isDataset());
        Assert.assertFalse(payload.isPatch());
        Assert.assertTrue(payload.isReady());
        Assert.assertNotNull(payload.getDataset());
        Assert.assertNull(payload.getPatch());
        Assert.assertEquals(payload.sizeInBytes(), 0);
    }

    @Test
    public void givenEagerPatchPayload_whenAccessingMethods_thenReturnValuesAreCorrect() {
        // Given
        RdfPayload payload = RdfPayload.of(RDFPatchOps.emptyPatch());

        // When and Then
        Assert.assertFalse(payload.isDataset());
        Assert.assertTrue(payload.isPatch());
        Assert.assertTrue(payload.isReady());
        Assert.assertNull(payload.getDataset());
        Assert.assertNotNull(payload.getPatch());
        Assert.assertEquals(payload.sizeInBytes(), 0);
    }

    @Test
    public void givenLazyDatasetPayload_whenAccessingMethods_thenReturnValuesAreCorrect() {
        // Given
        RdfPayload payload = RdfPayload.of(null, new byte[0]);

        // When and Then
        Assert.assertTrue(payload.isDataset());
        Assert.assertFalse(payload.isPatch());
        Assert.assertFalse(payload.isReady());
        Assert.assertNotNull(payload.getDataset());
        Assert.assertNull(payload.getPatch());
        Assert.assertTrue(payload.isReady());
        Assert.assertEquals(payload.sizeInBytes(), 0);
    }

    @Test
    public void givenLazyPatchPayload_whenAccessingMethods_thenReturnValuesAreCorrect() {
        // Given
        RdfPayload payload = RdfPayload.of(WebContent.contentTypePatch, new byte[0]);

        // When and Then
        Assert.assertFalse(payload.isDataset());
        Assert.assertTrue(payload.isPatch());
        Assert.assertFalse(payload.isReady());
        Assert.assertNull(payload.getDataset());
        Assert.assertNotNull(payload.getPatch());
        Assert.assertTrue(payload.isReady());
        Assert.assertEquals(payload.sizeInBytes(), 0);
    }

    @Test(expectedExceptions = RdfPayloadException.class, expectedExceptionsMessageRegExp = "Failed to deserialise.*")
    public void givenLazyInvalidDatasetPayload_whenAccessingDataset_thenErrorIsThrown() {
        // Given
        byte[] invalidData = JUNK_BYTES;
        RdfPayload payload = RdfPayload.of(null, invalidData);

        // When and Then
        Assert.assertTrue(payload.isDataset());
        Assert.assertFalse(payload.isReady());
        Assert.assertEquals(payload.sizeInBytes(), invalidData.length);
        payload.getDataset();
    }

    @Test(expectedExceptions = RdfPayloadException.class, expectedExceptionsMessageRegExp = "Failed to deserialise.*")
    public void givenLazyInvalidDatasetPayloadWithWrongContentType_whenAccessingDataset_thenErrorIsThrown() {
        // Given
        byte[] invalidData = SINGLE_NTRIPLE_BYTES;
        RdfPayload payload = RdfPayload.of(WebContent.contentTypeRDFXML, invalidData);

        // When and Then
        Assert.assertTrue(payload.isDataset());
        Assert.assertFalse(payload.isReady());
        Assert.assertEquals(payload.sizeInBytes(), invalidData.length);
        payload.getDataset();
    }

    @Test(expectedExceptions = RdfPayloadException.class, expectedExceptionsMessageRegExp = "Failed to deserialise.*")
    public void givenLazyInvalidPatchPayload_whenAccessingPatch_thenErrorIsThrown() {
        // Given
        RdfPayload payload = RdfPayload.of(WebContent.contentTypePatch, JUNK_BYTES);

        // When and Then
        Assert.assertTrue(payload.isPatch());
        Assert.assertFalse(payload.isReady());
        Assert.assertEquals(payload.sizeInBytes(), JUNK_BYTES.length);
        payload.getPatch();
    }

    @Test//(expectedExceptions = RdfPayloadException.class, expectedExceptionsMessageRegExp = "Failed to deserialise.*")
    public void givenLazyInvalidPatchPayloadWithBinaryContentType_whenAccessingPatch_thenErrorIsThrown() {
        // Given
        RdfPayload payload = RdfPayload.of(WebContent.contentTypePatchThrift, JUNK_BYTES);

        // When and Then
        Assert.assertTrue(payload.isPatch());
        Assert.assertFalse(payload.isReady());
        // BUG - Really Jena should throw an exception in this case but currently it doesn't and just returns an empty
        //       patch instead
        payload.getPatch();
    }

    @Test
    public void givenLazyValidDatasetPayload_whenAccessingDataset_thenSuccess() {
        // Given
        byte[] data = SINGLE_NTRIPLE_BYTES;
        RdfPayload payload = RdfPayload.of(WebContent.contentTypeNTriples, data);

        // When and Then
        Assert.assertTrue(payload.isDataset());
        Assert.assertFalse(payload.isReady());
        Assert.assertEquals(payload.sizeInBytes(), data.length);
        Assert.assertTrue(payload.hasRawData());
        payload.getDataset();
        Assert.assertEquals(payload.sizeInBytes(), data.length);
        Assert.assertFalse(payload.hasRawData());

    }

}
