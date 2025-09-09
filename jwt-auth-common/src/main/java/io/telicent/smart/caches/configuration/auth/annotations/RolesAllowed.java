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
package io.telicent.smart.caches.configuration.auth.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used on JAX-RS resource classes and methods to indicate the user roles required by an authenticated user
 * to access the endpoint represented by a given method.
 * <p>
 * Users need only hold at least one of the listed methods, the {@link RequirePermissions} annotation can be used to
 * indicate that additional permissions are required beyond just holding a role. This is generally enforced by a filter
 * in the servlet runtime, see the {@code jaxrs-base-server} module for an example JAX-RS implementation of this.
 * </p>
 * <p>
 * The most specific role annotation present takes precedence e.g. if a resource class defines
 * {@code @AllowedRoles({"USER", "ADMIN"}} and a method within that resource class defines
 * {@code @AllowedRoles({"ADMIN"})} then only users with the {@code ADMIN} role would be permitted to call the endpoint
 * represented by that method.  The {@link DenyAll} and {@link PermitAll} are also role annotations which can be used to
 * further restrict or permit access to a resource as needed.  If multiple annotations are applied on the same
 * method/class then the strictest wins i.e. {@link DenyAll}, then {@link RolesAllowed} and finally {@link PermitAll}.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface RolesAllowed {

    /**
     * Specifies one/more roles that are permitted to access the resource
     *
     * @return Roles
     */
    String[] value();
}
