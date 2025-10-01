/**
 * Copyright (C) Telicent Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.telicent.smart.cache.server.jaxrs.auth;

import io.jsonwebtoken.*;
import io.telicent.servlet.auth.jwt.JwtHttpConstants;
import io.telicent.servlet.auth.jwt.sources.HeaderSource;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.Map;

public class TestJwtAuthEngineWithProblemChallenges extends JwtAuthEngineWithProblemChallenges {

    private static final SecretKey TEST_KEY = Jwts.SIG.HS256.key().build();
    private static final JwtParser PARSER = Jwts.parser().verifyWith(TEST_KEY).build();

    public TestJwtAuthEngineWithProblemChallenges() {
        this(JwtHttpConstants.HEADER_AUTHORIZATION, JwtHttpConstants.AUTH_SCHEME_BEARER, null, "username", "email");
    }

    public TestJwtAuthEngineWithProblemChallenges(String header, String headerPrefix, String realm,
                                                  String... usernameClaims) {
        super(List.of(new HeaderSource(header, headerPrefix)), realm, usernameClaims, new String[] { "roles" });
    }

    @Test
    public void extract_username_01() {
        Jws<Claims> jws = PARSER.parseSignedClaims(Jwts.builder().subject("test").signWith(TEST_KEY).compact());
        String actual = this.extractUsername(jws);
        Assert.assertEquals(actual, "test");
    }

    @Test
    public void extract_username_02() {
        Jws<Claims> jws = PARSER.parseSignedClaims(Jwts.builder()
                                                       .subject("test")
                                                       .claims()
                                                       .add(Map.of("username", "Mr T. Test"))
                                                       .and()
                                                       .signWith(TEST_KEY)
                                                       .compact());
        String actual = this.extractUsername(jws);
        Assert.assertEquals(actual, "Mr T. Test");
    }

    @Test
    public void extract_username_03() {
        Jws<Claims> jws = PARSER.parseSignedClaims(
                Jwts.builder().subject("test").claims().add(Map.of("username", "")).and().signWith(TEST_KEY).compact());
        String actual = this.extractUsername(jws);
        Assert.assertEquals(actual, "test");
    }

    @Test
    public void extract_username_04() {
        Jws<Claims> jws = PARSER.parseSignedClaims(Jwts.builder()
                                                       .subject("test")
                                                       .claims()
                                                       .add(Map.of("username", 12345))
                                                       .and()
                                                       .signWith(TEST_KEY)
                                                       .compact());
        String actual = this.extractUsername(jws);
        Assert.assertEquals(actual, "test");
    }

    @Test
    public void extract_username_05() {
        JwtAuthEngineWithProblemChallenges engine =
                new TestJwtAuthEngineWithProblemChallenges(JwtHttpConstants.HEADER_AUTHORIZATION,
                                                           JwtHttpConstants.AUTH_SCHEME_BEARER, null, "", "email");
        Jws<Claims> jws = PARSER.parseSignedClaims(Jwts.builder()
                                                       .subject("test")
                                                       .claims()
                                                       .add(Map.of("username", 12345))
                                                       .and()
                                                       .signWith(TEST_KEY)
                                                       .compact());
        String actual = engine.extractUsernameForTesting(jws);
        Assert.assertEquals(actual, "test");
    }

    @Test
    public void extract_username_06() {
        JwtAuthEngineWithProblemChallenges engine =
                new TestJwtAuthEngineWithProblemChallenges(JwtHttpConstants.HEADER_AUTHORIZATION,
                                                           JwtHttpConstants.AUTH_SCHEME_BEARER, null, "email");
        Jws<Claims> jws = PARSER.parseSignedClaims(Jwts.builder()
                                                       .subject("test")
                                                       .claims()
                                                       .add(Map.of("email", "test@telicent.io"))
                                                       .and()
                                                       .signWith(TEST_KEY)
                                                       .compact());
        String actual = engine.extractUsernameForTesting(jws);
        Assert.assertEquals(actual, "test@telicent.io");
    }
}
