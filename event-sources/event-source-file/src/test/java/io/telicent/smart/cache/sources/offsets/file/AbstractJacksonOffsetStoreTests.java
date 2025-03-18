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
package io.telicent.smart.cache.sources.offsets.file;

import io.telicent.smart.cache.sources.offsets.AbstractOffsetStoreTests;
import io.telicent.smart.cache.sources.offsets.OffsetStore;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public abstract class AbstractJacksonOffsetStoreTests extends AbstractOffsetStoreTests {
    protected File currentFile = null;

    @AfterMethod
    public void afterTest() {
        if (this.currentFile != null) {
            this.currentFile.delete();
            this.currentFile = null;
        }
    }

    @Override
    protected boolean isPersistent() {
        return true;
    }

    @Override
    protected boolean isPersistenceDelayed() {
        return true;
    }

    protected void ensureTemporaryFile() {
        if (this.currentFile == null) {
            try {
                this.currentFile = File.createTempFile("offsets", ".test");
            } catch (IOException e) {
                throw new RuntimeException("Failed to create temporary file storage for Offset store");
            }
        }
    }

    protected abstract OffsetStore createOffsetStore(File offsetsFile);

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Failed to create offsets file.*")
    public void givenNonExistentOffsetFilePath_whenClosing_thenErrorIsThrown() {
        // Given
        File bad = new File("/no/such/directory/path/to/offsets.file");
        if (bad.exists()) {
            throw new SkipException("Bad file path exists on this file system!");
        }
        OffsetStore store = this.createOffsetStore(bad);

        // When and Then
        store.close();
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Failed to read offsets.*")
    public void givenOffsetFileWithJunkData_whenCreating_thenErrorIsThrown() throws IOException {
        // Given
        File bad = new File("target/bad.txt");
        try (FileWriter writer = new FileWriter(bad)) {
            writer.write("junk data");
        }

        // When and Then
        this.createOffsetStore(bad);
    }
}
