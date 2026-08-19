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
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ParamInfo#fromViolation(ConstraintViolation)} which walks a constraint violation's property path to
 * work out which request parameter a validation failure relates to.
 */
public class TestParamInfo {

    private static final String PATH_TO_STRING = "testViolationPath";

    /**
     * An example JAX-RS style resource whose annotated methods are resolved reflectively by the class under test.  The
     * methods must be public for {@code Class#getMethod()} to find them.
     */
    public static class ExampleResource {
        public String withQuery(@QueryParam("q") String q) {
            return q;
        }

        public String withPath(@PathParam("p") String p) {
            return p;
        }

        public String withHeader(@HeaderParam("h") String h) {
            return h;
        }

        public String withCookie(@CookieParam("c") String c) {
            return c;
        }

        public String withForm(@FormParam("f") String f) {
            return f;
        }

        public String withBean(@BeanParam ExampleBean bean) {
            return bean != null ? bean.beanHeader : null;
        }

        public String withNothing(String plain) {
            return plain;
        }
    }

    /**
     * An example {@link BeanParam} target.  The fields must be public for {@code Class#getField()} to find them.
     */
    public static class ExampleBean {
        @HeaderParam("X-Bean-Header")
        public String beanHeader;

        public String unannotated;
    }

    /**
     * Builds a real {@link Path} rather than a mock so that {@code toString()} is controllable
     *
     * @param nodes Path nodes, in the order the class under test will walk them
     * @return Property path
     */
    private static Path pathOf(Path.Node... nodes) {
        List<Path.Node> list = List.of(nodes);
        return new Path() {
            @Override
            public Iterator<Node> iterator() {
                return list.iterator();
            }

            @Override
            public String toString() {
                return PATH_TO_STRING;
            }
        };
    }

    private static ConstraintViolation<?> violation(Class<?> rootBeanClass, Path path) {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        doReturn(path).when(violation).getPropertyPath();
        doReturn(rootBeanClass).when(violation).getRootBeanClass();
        return violation;
    }

    private static Path.MethodNode methodNode(String name, Class<?>... parameterTypes) {
        Path.MethodNode node = mock(Path.MethodNode.class);
        when(node.getKind()).thenReturn(ElementKind.METHOD);
        when(node.getName()).thenReturn(name);
        doReturn(List.of(parameterTypes)).when(node).getParameterTypes();
        return node;
    }

    private static Path.ParameterNode parameterNode(String name, int index) {
        Path.ParameterNode node = mock(Path.ParameterNode.class);
        when(node.getKind()).thenReturn(ElementKind.PARAMETER);
        when(node.getName()).thenReturn(name);
        when(node.getParameterIndex()).thenReturn(index);
        return node;
    }

    private static Path.PropertyNode propertyNode(String name) {
        Path.PropertyNode node = mock(Path.PropertyNode.class);
        when(node.getKind()).thenReturn(ElementKind.PROPERTY);
        when(node.getName()).thenReturn(name);
        return node;
    }

    private static Path.BeanNode beanNode(Class<?> containerClass) {
        Path.BeanNode node = mock(Path.BeanNode.class);
        when(node.getKind()).thenReturn(ElementKind.BEAN);
        doReturn(containerClass).when(node).getContainerClass();
        return node;
    }

    private static void verifyParamInfo(ParamInfo actual, String expectedName, String expectedType) {
        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.getName(), expectedName);
        Assert.assertEquals(actual.getType(), expectedType);
    }

    private static ParamInfo forParameterOf(String method, String parameterName) {
        return ParamInfo.fromViolation(violation(ExampleResource.class,
                                                pathOf(methodNode(method, String.class),
                                                       parameterNode(parameterName, 0))));
    }

    @Test
    public void givenQueryParam_whenDerivingParamInfo_thenNameAndTypeAreResolved() {
        verifyParamInfo(forParameterOf("withQuery", "arg0"), "q", "Query");
    }

    @Test
    public void givenPathParam_whenDerivingParamInfo_thenNameAndTypeAreResolved() {
        verifyParamInfo(forParameterOf("withPath", "arg0"), "p", "Path");
    }

    @Test
    public void givenHeaderParam_whenDerivingParamInfo_thenNameAndTypeAreResolved() {
        verifyParamInfo(forParameterOf("withHeader", "arg0"), "h", "Header");
    }

    @Test
    public void givenCookieParam_whenDerivingParamInfo_thenNameAndTypeAreResolved() {
        verifyParamInfo(forParameterOf("withCookie", "arg0"), "c", "Cookie");
    }

    @Test
    public void givenFormParam_whenDerivingParamInfo_thenNameAndTypeAreResolved() {
        verifyParamInfo(forParameterOf("withForm", "arg0"), "f", "Form");
    }

    @Test
    public void givenUnannotatedParameter_whenDerivingParamInfo_thenParameterNameIsUsed() {
        verifyParamInfo(forParameterOf("withNothing", "plain"), "plain", null);
    }

    @Test
    public void givenMethodMissingFromRootBean_whenDerivingParamInfo_thenParameterNameIsUsed() {
        // The method cannot be resolved, so no parameter annotations are available and we fall back to the raw name
        verifyParamInfo(forParameterOf("doesNotExistOnThisClass", "arg0"), "arg0", null);
    }

    @Test
    public void givenBeanParam_whenDerivingParamInfo_thenAnnotatedBeanPropertyIsResolved() {
        ParamInfo info = ParamInfo.fromViolation(
                violation(ExampleResource.class,
                          pathOf(methodNode("withBean", ExampleBean.class), parameterNode("arg0", 0),
                                 propertyNode("beanHeader"))));
        verifyParamInfo(info, "X-Bean-Header", "Header");
    }

    @Test
    public void givenBeanParamWithUnannotatedProperty_whenDerivingParamInfo_thenPropertyNameIsUsed() {
        ParamInfo info = ParamInfo.fromViolation(
                violation(ExampleResource.class,
                          pathOf(methodNode("withBean", ExampleBean.class), parameterNode("arg0", 0),
                                 propertyNode("unannotated"))));
        verifyParamInfo(info, "unannotated", null);
    }

    @Test
    public void givenPropertyMissingFromBean_whenDerivingParamInfo_thenPropertyNameIsUsed() {
        ParamInfo info = ParamInfo.fromViolation(
                violation(ExampleResource.class,
                          pathOf(methodNode("withBean", ExampleBean.class), parameterNode("arg0", 0),
                                 propertyNode("noSuchField"))));
        verifyParamInfo(info, "noSuchField", null);
    }

    @Test
    public void givenBeanNode_whenDerivingParamInfo_thenContainerClassIsSearched() {
        ParamInfo info = ParamInfo.fromViolation(
                violation(ExampleResource.class, pathOf(beanNode(ExampleBean.class), propertyNode("beanHeader"))));
        verifyParamInfo(info, "X-Bean-Header", "Header");
    }

    @Test
    public void givenNoRootBeanClass_whenDerivingParamInfoForProperty_thenPropertyNameIsUsed() {
        ParamInfo info = ParamInfo.fromViolation(violation(null, pathOf(propertyNode("beanHeader"))));
        verifyParamInfo(info, "beanHeader", null);
    }

    @Test
    public void givenNoRootBeanClass_whenDerivingParamInfoForParameter_thenParameterNameIsUsed() {
        // With no class the method node cannot be resolved, so the parameter annotations are never consulted
        ParamInfo info = ParamInfo.fromViolation(
                violation(null, pathOf(methodNode("withQuery", String.class), parameterNode("arg0", 0))));
        verifyParamInfo(info, "arg0", null);
    }

    @Test
    public void givenEmptyPath_whenDerivingParamInfo_thenPathStringIsUsed() {
        verifyParamInfo(ParamInfo.fromViolation(violation(ExampleResource.class, pathOf())), PATH_TO_STRING, null);
    }
}
