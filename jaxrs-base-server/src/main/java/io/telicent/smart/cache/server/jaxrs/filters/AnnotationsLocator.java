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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for locating annotations present on resource methods and classes
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AnnotationsLocator {

    /**
     * Finds all the instances of a given annotation for the resource method, it's containing class, and any parent
     * classes thereof
     *
     * @param method               Method
     * @param resourceClass        The resource class, note that this is not necessarily the declaring class of the
     *                             method as this method might be inherited by a child resource class.  If {@code null}
     *                             then {@link Method#getDeclaringClass()} is used to find any class level annotations
     *                             if method level annotations are not present.
     * @param annotation           Annotation to find
     * @param collectedAnnotations List to populate with found annotations
     */
    protected static <T extends Annotation> void findAll(Method method, Class<?> resourceClass, Class<T> annotation,
                                                         List<T> collectedAnnotations) {
        if (method == null) {
            findAll(resourceClass, annotation, collectedAnnotations);
            return;
        }

        // Check whether the method is directly annotated with the desired annotation
        if (method.isAnnotationPresent(annotation)) {
            CollectionUtils.addAll(collectedAnnotations, method.getAnnotationsByType(annotation));
        }

        // Also see if there are any annotations at the class level
        findAll(resourceClass != null ? resourceClass : method.getDeclaringClass(), annotation, collectedAnnotations);
    }

    /**
     * Finds the most specific annotation for the class, or any of its parent classes
     * <p>
     * Finds the annotation that occurs at the most specific level i.e. prefers method annotations over class *
     * annotations, and prefers class annotations lower down the inheritance tree.
     * </p>
     *
     * @param clazz                Class
     * @param annotation           Annotation to find
     * @param collectedAnnotations List to populate with found annotations
     */
    protected static <T extends Annotation> void findAll(Class<?> clazz, Class<T> annotation,
                                                         List<T> collectedAnnotations) {
        if (clazz == null) {
            return;
        }

        // Check whether the class is annotated with any of the given annotations
        if (clazz.isAnnotationPresent(annotation)) {
            CollectionUtils.addAll(collectedAnnotations, clazz.getAnnotationsByType(annotation));
        }

        // Walk up the type hierarchy to see if there are any annotations on a parent class
        if (clazz.getSuperclass() != null) {
            findAll(clazz.getSuperclass(), annotation, collectedAnnotations);
        }
    }


    /**
     * Finds the role policy based upon the strictest and most specific roles annotation present on the given method,
     * its containing class, or a parent class
     *
     * @param method Method
     * @return Role Policy, or {@code null} if no annotation defined policy
     */
    public static <T extends Annotation> List<T> findAnnotations(Method method, Class<?> resourceClass,
                                                                 Class<T> annotation) {
        List<T> found = new ArrayList<>();
        findAll(method, resourceClass, annotation, found);
        return found;
    }
}
