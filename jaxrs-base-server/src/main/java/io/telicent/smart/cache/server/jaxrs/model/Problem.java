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
package io.telicent.smart.cache.server.jaxrs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.ws.rs.core.Response;

import java.util.Objects;

/**
 * Represents a Problem
 * <p>
 * This POJO is an implementation of the Problem response described in
 * <a href="https://www.rfc-editor.org/rfc/rfc7807.html">RFC 7807</a>.
 * </p>
 */
@JsonPropertyOrder({ "type", "title", "status", "detail", "instance" })
public class Problem {

    private String type, title, instance, detail;
    private int status;

    /**
     * Creates a new empty problem
     */
    public Problem() {
    }

    /**
     * Creates a problem
     *
     * @param type     Type
     * @param title    Title
     * @param status   HTTP Status Code
     * @param detail   Problem details
     * @param instance Problem instance
     */
    public Problem(String type, String title, int status, String detail, String instance) {
        this.type = type;
        this.title = title;
        this.instance = instance;
        this.detail = detail;
        this.status = status;
    }

    /**
     * Converts the problem into an HTTP Response that can be returned by the server
     *
     * @return HTTP Response
     */
    public Response toResponse() {
        return Response.status(this.status)
                       .entity(this)
                       .type("application/problem+json")
                       .build();
    }

    /**
     * Gets the type of the problem
     *
     * @return Type
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getType() {
        return type;
    }

    /**
     * Sets the type of the problem
     *
     * @param type Type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the title of the problem
     *
     * @return Title
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the problem
     *
     * @param title Title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the instance of the problem
     *
     * @return Instance
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getInstance() {
        return instance;
    }

    /**
     * Sets the instance of the problem
     *
     * @param instance Instance
     */
    public void setInstance(String instance) {
        this.instance = instance;
    }

    /**
     * Gets the detailed description of the problem
     *
     * @return Detailed description
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getDetail() {
        return detail;
    }

    /**
     * Sets the detailed description of the problem
     *
     * @param detail Detailed description
     */
    public void setDetail(String detail) {
        this.detail = detail;
    }

    /**
     * Gets the HTTP Status code for this problem
     *
     * @return HTTP Status Code
     */
    public int getStatus() {
        return status;
    }

    /**
     * Sets the HTTP Status Code for this problem
     *
     * @param status HTTP Status Code
     */
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Problem problem = (Problem) o;
        return status == problem.status &&
                Objects.equals(type, problem.type) &&
                Objects.equals(title, problem.title) &&
                Objects.equals(instance, problem.instance) &&
                Objects.equals(detail, problem.detail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, title, instance, detail, status);
    }

    @Override
    public String toString() {
        String sb = "Problem{" + "type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", instance='" + instance + '\'' +
                ", detail='" + detail + '\'' +
                ", status=" + status +
                '}';
        return sb;
    }
}
