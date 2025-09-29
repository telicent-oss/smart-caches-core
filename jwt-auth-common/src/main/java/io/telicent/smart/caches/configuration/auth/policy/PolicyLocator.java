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
package io.telicent.smart.caches.configuration.auth.policy;

import io.telicent.smart.caches.configuration.auth.annotations.RequirePermissions;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Helper class for locating authorization policies based upon annotations present on resource methods and classes
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PolicyLocator {

    /**
     * Finds the most specific annotation for the method, or it's containing class, or any parent class
     * <p>
     * Finds the annotation that occurs at the most specific level i.e. prefers method annotations over class
     * annotations, and prefers class annotations lower down the inheritance tree.
     * </p>
     *
     * @param method              Method
     * @param eligibleAnnotations Eligible annotations to find in order of precedence, if multiple of these annotations
     *                            are present then only the first found is returned so callers should take care to
     *                            provide their desired precedence order
     * @return Annotation, or {@code null} if no such annotations
     */
    private static Annotation findMostSpecific(Method method, Class<? extends Annotation>[] eligibleAnnotations) {
        if (method == null) {
            return null;
        }

        // Check whether the method is directly annotated with any of the given annotations
        for (Class<? extends Annotation> eligible : eligibleAnnotations) {
            if (method.isAnnotationPresent(eligible)) {
                return method.getAnnotation(eligible);
            }
        }

        // If not annotated on the method see if there are any annotations at the class level
        return findMostSpecific(method.getDeclaringClass(), eligibleAnnotations);
    }

    /**
     * Finds the most specific annotation for the class, or any of its parent classes
     * <p>
     * Finds the annotation that occurs at the most specific level i.e. prefers method annotations over class *
     * annotations, and prefers class annotations lower down the inheritance tree.
     * </p>
     *
     * @param clazz               Class
     * @param eligibleAnnotations Eligible annotations to find in order of precedence, if multiple of these annotations
     *                            are present then only the first found is returned so callers should take care to
     *                            provide their desired precedence order
     * @return Annotation, or {@code null} if no such annotations
     */
    private static Annotation findMostSpecific(Class<?> clazz, Class<? extends Annotation>[] eligibleAnnotations) {
        if (clazz == null) {
            return null;
        }

        // Check whether the class is annotated with any of the given annotations
        for (Class<? extends Annotation> eligible : eligibleAnnotations) {
            if (clazz.isAnnotationPresent(eligible)) {
                return clazz.getAnnotation(eligible);
            }
        }

        // Walk up the type hierarchy to see if there are any annotations on a parent class
        if (clazz.getSuperclass() != null) {
            return findMostSpecific(clazz.getSuperclass(), eligibleAnnotations);
        }

        return null;
    }


    /**
     * Finds the role policy based upon the strictest and most specific roles annotation present on the given method,
     * its containing class, or a parent class
     *
     * @param method Method
     * @return Role Policy, or {@code null} if no annotation defined policy
     */
    @SuppressWarnings("unchecked")
    public static Policy findRolesPolicyFromAnnotations(Method method) {
        Annotation annotation =
                findMostSpecific(method, new Class[] { DenyAll.class, RolesAllowed.class, PermitAll.class });
        if (annotation == null) {
            return Policy.NONE;
        } else if (annotation instanceof DenyAll) {
            return Policy.DENY_ALL;
        } else if (annotation instanceof RolesAllowed rolesAllowed) {
            return Policy.requireAny("roles", rolesAllowed.value());
        } else if (annotation instanceof PermitAll) {
            return Policy.ALLOW_ALL;
        } else {
            return Policy.NONE;
        }
    }

    /**
     * Finds the most specific permissions annotations present on the given method, its containing class, or a parent
     * class
     *
     * @param method Method
     * @return Most specific permissions annotation present i.e. {@link RequirePermissions}, or {@code null} if no such
     * annotations
     */
    @SuppressWarnings("unchecked")
    public static Policy findPermissionsPolicyFromAnnotations(Method method) {
        Annotation annotation = findMostSpecific(method, new Class[] { RequirePermissions.class });
        if (annotation == null) {
            return Policy.NONE;
        } else if (annotation instanceof RequirePermissions requirePermissions) {
            return Policy.requireAll("permissions", requirePermissions.value());
        } else {
            return Policy.NONE;
        }
    }
}
