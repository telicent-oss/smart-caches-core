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
package io.telicent.smart.cache.server.jaxrs.utils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import jakarta.ws.rs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;

/**
 * Represents information about an API parameter used for error handling
 */
public class ParamInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParamInfo.class);

    private final String name, type;

    /**
     * Creates new parameter information
     *
     * @param name Name
     * @param type Type
     */
    public ParamInfo(String name, String type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Gets the parameter information for a parameter that has a constraint violation against it
     *
     * @param violation Constraint violation
     * @return Parameter Info
     */
    public static ParamInfo fromViolation(ConstraintViolation<?> violation) {
        Path path = violation.getPropertyPath();
        Iterator<Path.Node> iter = path.iterator();
        Class<?> cls = violation.getRootBeanClass();
        Method m = null;
        while (iter.hasNext()) {
            Path.Node node = iter.next();
            if (node.getKind() == ElementKind.BEAN) {
                Path.BeanNode bean = (Path.BeanNode) node;
                cls = bean.getContainerClass();
            } else if (node.getKind() == ElementKind.METHOD) {
                Path.MethodNode method = (Path.MethodNode) node;
                if (cls != null) {
                    try {
                        m = cls.getMethod(method.getName(), method.getParameterTypes().toArray(new Class[0]));
                    } catch (NoSuchMethodException e) {
                        // Ignore, just means we won't be able to find a friendly parameter name for the parameter
                        LOGGER.warn("Constraint violation path identifies method {} which does not exist on class {}",
                                    method.getName(), cls.getCanonicalName());
                    }
                }
            } else if (node.getKind() == ElementKind.PARAMETER) {
                Path.ParameterNode param = (Path.ParameterNode) node;
                boolean shouldContinue = false;
                if (m != null) {
                    for (Annotation annotation : m.getParameterAnnotations()[param.getParameterIndex()]) {
                        ParamInfo info = findParamInfoFromAnnotation(annotation);
                        if (info != null) {
                            return info;
                        } else if (annotation instanceof BeanParam) {
                            cls = m.getParameterTypes()[param.getParameterIndex()];
                            // Need to continue on to next step of the violation path to find the bean property that is
                            // annotated
                            shouldContinue = true;
                            break;
                        }
                    }
                }
                if (shouldContinue) {
                    continue;
                }
                return new ParamInfo(param.getName(), null);
            } else if (node.getKind() == ElementKind.PROPERTY) {
                // If @BeanParam parameters are used then the fields of that are Property nodes in the violation path
                Path.PropertyNode property = (Path.PropertyNode) node;
                try {
                    if (cls != null) {
                        Field field = cls.getField(property.getName());
                        for (Annotation annotation : field.getAnnotations()) {
                            ParamInfo info = findParamInfoFromAnnotation(annotation);
                            if (info != null) {
                                return info;
                            }
                        }
                    }
                } catch (NoSuchFieldException e) {
                    // Ignored, can't get a more specific param info
                }
                return new ParamInfo(property.getName(), null);
            }
        }
        return new ParamInfo(path.toString(), null);
    }

    /**
     * Finds parameter information based on annotations
     *
     * @param annotation Annotation to test for parameter info
     * @return Param info, or {@code null} if not an annotation that provides parameter info
     */
    protected static ParamInfo findParamInfoFromAnnotation(Annotation annotation) {
        if (annotation instanceof QueryParam queryParam) {
            return new ParamInfo(queryParam.value(), "Query");
        } else if (annotation instanceof PathParam pathParam) {
            return new ParamInfo(pathParam.value(), "Path");
        } else if (annotation instanceof HeaderParam headerParam) {
            return new ParamInfo(headerParam.value(), "Header");
        } else if (annotation instanceof CookieParam cookieParam) {
            return new ParamInfo(cookieParam.value(), "Cookie");
        } else if (annotation instanceof FormParam formParam) {
            return new ParamInfo(formParam.value(), "Form");
        } else {
            return null;
        }
    }

    /**
     * Gets the name of the parameter
     *
     * @return Name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the type of the parameter
     *
     * @return Type
     */
    public String getType() {
        return type;
    }
}
