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
package io.telicent.smart.cache.sources.file.kafka;

import io.telicent.smart.cache.sources.file.FileEventAccessMode;
import org.testng.annotations.Test;

public class TestAbstractKafkaDelegatingEventReaderWriter {

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*not configured for write.*")
    public void access_modes_01() {
        AbstractKafkaDelegatingEventReaderWriter.ensureWritesPermitted(FileEventAccessMode.ReadOnly);
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*not configured for read.*")
    public void access_modes_02() {
        AbstractKafkaDelegatingEventReaderWriter.ensureReadsPermitted(FileEventAccessMode.WriteOnly);
    }

    @Test
    public void access_modes_03() {
        AbstractKafkaDelegatingEventReaderWriter.ensureReadsPermitted(FileEventAccessMode.ReadOnly);
        AbstractKafkaDelegatingEventReaderWriter.ensureReadsPermitted(FileEventAccessMode.ReadWrite);
    }

    @Test
    public void access_modes_04() {
        AbstractKafkaDelegatingEventReaderWriter.ensureWritesPermitted(FileEventAccessMode.WriteOnly);
        AbstractKafkaDelegatingEventReaderWriter.ensureWritesPermitted(FileEventAccessMode.ReadWrite);
    }
}
