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
package io.telicent.smart.cache.server.jaxrs.resources;

import io.telicent.jena.abac.AttributeValueSet;
import io.telicent.jena.abac.Hierarchy;
import io.telicent.jena.abac.attributes.Attribute;
import io.telicent.jena.abac.core.AttributesStore;
import io.telicent.jena.abac.core.AttributesStoreLocal;
import io.telicent.smart.cache.server.jaxrs.model.Problem;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Path("/")
public class AttributesResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttributesResource.class);
    private static final AttributesStoreLocal DEFAULT_EMPTY_STORE = new AttributesStoreLocal();

    private AttributesStore getAttributesStore() {
        try {
            AttributesStore store =
                    (AttributesStore) System.getProperties().get(AttributesStore.class.getCanonicalName());
            return store != null ? store : DEFAULT_EMPTY_STORE;
        } catch (ClassCastException e) {
            return DEFAULT_EMPTY_STORE;
        }
    }

    @GET
    @Path("users/lookup/{user}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response lookupUser(@Context HttpHeaders headers, @PathParam("user") @NotBlank String user) {
        AttributesStore store = getAttributesStore();
        AttributeValueSet attributes = store.attributes(user);
        if (attributes == null) {
            LOGGER.warn("No attributes for user {}", user);
            return new Problem("NotFound", "User Not Found", Response.Status.NOT_FOUND.getStatusCode(),
                               "No attributes for user " + user, null).toResponse(headers);
        } else {
            LOGGER.info("Returning attributes for user {}", user);
        }

        LinkedHashMap<String, Object> json = new LinkedHashMap<>();
        List<String> attrList = new ArrayList<>();
        attributes.attributeValues(v -> attrList.add(v.asString()));
        json.put("attributes", attrList);

        return Response.ok().entity(json).build();
    }

    @GET
    @Path("hierarchies/lookup/{name}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response lookupHierarchy(@Context HttpHeaders headers, @PathParam("name") @NotBlank String name) {
        AttributesStore store = getAttributesStore();
        Attribute key = Attribute.create(name);
        if (store.hasHierarchy(key)) {
            Hierarchy hierarchy = store.getHierarchy(key);

            LinkedHashMap<String, Object> json = new LinkedHashMap<>();
            List<String> tiers = new ArrayList<>();
            hierarchy.values().forEach(v -> tiers.add(v.asString()));
            json.put("tiers", tiers);
            LOGGER.info("Returning tiers for hierarchy {}", name);

            return Response.ok().entity(json).build();
        } else {
            LOGGER.warn("No hierarchy {}", name);
            return new Problem("NotFound", "Hierarchy Not Found", Response.Status.NOT_FOUND.getStatusCode(),
                               "No hierarchy " + name + " exists", null).toResponse(headers);
        }
    }
}
