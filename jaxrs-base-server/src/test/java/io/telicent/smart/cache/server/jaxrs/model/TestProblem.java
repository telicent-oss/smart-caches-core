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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestProblem {

    private final ObjectMapper json = new ObjectMapper();

    @Test
    public void problem_test_01() throws JsonProcessingException {
        Problem problem = new Problem("Test", "Title", 400, "You were being bad", "BadRequest#1234");
        Assert.assertTrue(problem.equals(problem));
        Assert.assertFalse(problem.equals(null));
        Assert.assertFalse(problem.equals(new Object()));

        String json = this.json.writeValueAsString(problem);
        Problem retrieved = this.json.readValue(json, Problem.class);

        Assert.assertEquals(retrieved, problem);
        Assert.assertTrue(retrieved.equals(problem));
        Assert.assertEquals(retrieved.hashCode(), problem.hashCode());
        Assert.assertEquals(retrieved.toString(), problem.toString());
    }

    @Test
    public void problem_test_02() {
        Problem a = new Problem("Test", "Title", 400, "You were being bad", "BadRequest#1234");
        Problem b = new Problem("Test", "Title", 400, "You were being bad", "BadRequest#1234");

        Assert.assertEquals(a, b);
        Assert.assertTrue(a.equals(b));

        // Mutate each field checking equality no longer holds
        b.setDetail(null);
        verifyNotEquals(a, b);
        b.setInstance(null);
        verifyNotEquals(a, b);
        b.setTitle(null);
        verifyNotEquals(a, b);
        b.setType(null);
        verifyNotEquals(a, b);
        b.setStatus(-1);
        verifyNotEquals(a, b);
    }

    private static void verifyNotEquals(Problem a, Problem b) {
        Assert.assertNotEquals(a, b);
        Assert.assertFalse(a.equals(b));
    }
}
