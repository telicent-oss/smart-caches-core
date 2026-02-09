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
package io.telicent.smart.caches.benchmarks;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.openjdk.jmh.annotations.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class JwtParsingBenchmark {

    @Param({"HS256", "RS256"})
    public String algorithm;

    @Param({"1", "10", "50"})
    public int claimCount;

    @Param({"128", "512", "2048"})
    public int claimValueLength;

    private String jwt;
    private JwtParser parser;

    private SecretKey hmacKey;
    private KeyPair rsaKeyPair;

    @Setup(Level.Trial)
    public void setup() {

        // Build a set of random claims based on params
        Map<String, Object> claims = generateClaims(claimCount, claimValueLength);

        Instant now = Instant.now();

        if (algorithm.startsWith("HS")) {
            hmacKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            jwt = Jwts.builder()
                    .setSubject("benchmark-user")
                    .setIssuedAt(Date.from(now))
                    .setExpiration(Date.from(now.plusSeconds(3600)))
                    .addClaims(claims)
                    .signWith(hmacKey, SignatureAlgorithm.HS256)
                    .compact();

            parser = Jwts.parser()
                    .setSigningKey(hmacKey)
                    .build();

        } else if (algorithm.startsWith("RS")) {
            rsaKeyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);

            jwt = Jwts.builder()
                    .setSubject("benchmark-user")
                    .setIssuedAt(Date.from(now))
                    .setExpiration(Date.from(now.plusSeconds(3600)))
                    .addClaims(claims)
                    .signWith(rsaKeyPair.getPrivate(), SignatureAlgorithm.RS256)
                    .compact();

            parser = Jwts.parser()
                    .setSigningKey(rsaKeyPair.getPublic())
                    .build();
        }
    }

    private Map<String, Object> generateClaims(int count, int valueLength) {
        String value = randomString(valueLength);
        return IntStream.range(0, count)
                .boxed()
                .collect(Collectors.toMap(
                        i -> "claim_" + i,
                        i -> value
                ));
    }

    private String randomString(int n) {
        byte[] bytes = new byte[n];
        new Random(12345).nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    @Benchmark
    public Jws<Claims> parseAndVerify() {
        return parser.parseClaimsJws(jwt);
    }

    @Benchmark
    public int decodeWithoutVerify() {
        int dot1 = jwt.indexOf('.');
        int dot2 = jwt.indexOf('.', dot1 + 1);

        String headerJson = new String(
                Base64.getUrlDecoder().decode(jwt.substring(0, dot1)),
                StandardCharsets.US_ASCII
        );

        String payloadJson = new String(
                Base64.getUrlDecoder().decode(jwt.substring(dot1 + 1, dot2)),
                StandardCharsets.US_ASCII
        );

        return headerJson.length() + payloadJson.length();
    }

    @Benchmark
    public Jws<Claims> parseWithRotatingKeys() {
        KeyPair key = Keys.keyPairFor(SignatureAlgorithm.RS256);
        JwtParser p = Jwts.parser().setSigningKey(key.getPublic()).build();
        try {
            return p.parseClaimsJws(jwt);
        } catch (Exception e) {
            return null;
        }
    }
}
