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
package io.telicent.smart.cache.sources.kafka;

import org.testng.annotations.Test;

// Tests currently enabled as EncryptedKafkaTestCluster is not yet functional
@Test(enabled = false)
public class DockerTestEncryptedKafkaCluster /*extends DockerTestSecureKafkaCluster*/ {

    /*

    @BeforeClass
    @Override
    public void setup() {
        if (StringUtils.contains(System.getProperty("os.name"), "Windows"))
            throw new SkipException("These tests cannot run on Windows because the SSL certificates generator script assumes a Posix compatible OS");

        this.kafka = new EncryptedKafkaTestCluster(SecureKafkaTestCluster.DEFAULT_ADMIN_USERNAME,
                                                   SecureKafkaTestCluster.DEFAULT_ADMIN_PASSWORD,
                                                   Map.of(SecureKafkaTestCluster.DEFAULT_CLIENT_USERNAME,
                                                          SecureKafkaTestCluster.DEFAULT_CLIENT_PASSWORD, "extra",
                                                          "secret-squirrel"));
        this.kafka.setup();
    }

    @AfterClass
    @Override
    public void teardown() {
        super.teardown();
    }

    */
}
