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
package io.telicent.smart.cache.server.jaxrs.filters;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.UriInfo;

/**
 * A helper record class to supply the necessary context for the {@link JaxRsRateLimitingEngine} as used by the
 * {@link RateLimitFilter}
 *
 * @param requestContext Container request context
 * @param resourceInfo   Resource Info
 * @param uriInfo        URI info
 */
public record JaxRsRatelimitContext(ContainerRequestContext requestContext, ResourceInfo resourceInfo,
                                    UriInfo uriInfo) {
}
