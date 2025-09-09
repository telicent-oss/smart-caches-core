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
 * Annotation applied to JAX-RS resource classes or methods to indicate the permissions required by an authenticated
 * user to access the endpoint represented by a given method.
 * <p>
 * Users <strong>MUST</strong> hold all the listed permissions in order to access an endpoint, this differs from
 * {@link RolesAllowed} where they need only hold one of the listed roles.  Therefore, this annotation allows for
 * stricter authorization controls on an endpoint.
 * </p>
 * <p>
 * The most specific role annotation present takes precedence e.g. if a resource class defines
 * {@code @RequirePermissions({"a"}} and a method within that resource class defines
 * {@code @RequirePermissions({"a", "b})} then only users with both the {@code a} and {@code b} permissions would be
 * permitted to call the endpoint represented by that method.  If you wish to relax the restriction imposes by the
 * resource class then you can specify this annotation with an empty array of permissions, i.e.
 * {@code @RequirePermissions({})}, which has the effect of not requiring any further permissions to access the
 * endpoint.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface RequirePermissions {

    /**
     * Specifies the list of permissions that an authenticated user <strong>MUST</strong> hold to access this resource
     * <p>
     * An empty list is used to indicate that no further permissions are required.
     * </p>
     *
     * @return Permissions
     */
    String[] value();
}
