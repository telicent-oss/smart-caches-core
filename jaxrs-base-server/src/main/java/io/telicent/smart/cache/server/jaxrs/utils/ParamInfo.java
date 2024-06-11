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
                        m = cls.getMethod(method.getName(),
                                          method.getParameterTypes()
                                                .toArray(new Class[0]));
                    } catch (NoSuchMethodException e) {
                        // Ignore, just means we won't be able to find a friendly parameter name for the parameter
                        LOGGER.warn("Constraint violation path identifies method {} which does not exist on class {}",
                                    method.getName(), cls.getCanonicalName());
                    }
                }
            } else if (node.getKind() == ElementKind.PARAMETER) {
                Path.ParameterNode param = (Path.ParameterNode) node;
                if (m != null) {
                    for (Annotation annotation : m.getParameterAnnotations()[param.getParameterIndex()]) {
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
                        }
                    }
                }
                return new ParamInfo(param.getName(), null);
            }
        }
        return new ParamInfo(path.toString(), null);
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
