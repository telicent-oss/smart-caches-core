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
package io.telicent.smart.cache.security.attributes;

import io.telicent.smart.cache.security.Authorizer;
import io.telicent.smart.cache.security.labels.SecurityLabels;
import io.telicent.smart.cache.security.requests.RequestContext;

public interface AttributesProvider {

    /**
     * Obtains the attributes for the user
     * <p>
     * This might involve reaching out to some external attributes service, querying some local authorization
     * database, interpreting claims from the users JSON Web Token (JWT), aspects of the request, some combination
     * thereof, or some other mechanism entirely.  Generally speaking an implementation should only throw
     * {@link MalformedAttributesException} if these underlying attribute sources return bad data in some way.
     * </p>
     * </p>
     * If the user simply does not have any attributes in the system they should return an "empty" attributes
     * object, however that may be represented in concrete terms for a plugins implementation.  As long as when their
     * {@link Authorizer} implementation is used with this "empty" attributes object it makes correct fail-safe access
     * decisions as discussed on {@link Authorizer#canRead(SecurityLabels)}, i.e., providing an "empty" attributes
     * object <strong>MUST NOT</strong> cause incorrect access decisions to be made.
     * </p>
     *
     * @param context Request Context
     * @return Users attributes
     * @throws MalformedAttributesException Thrown if the users attributes are malformed or otherwise invalid
     */
    UserAttributes<?> attributesForUser(RequestContext context) throws MalformedAttributesException;
}
