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
package io.telicent.smart.cache.observability;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Properties;
import java.util.Set;

public class TestLibraryVersion {

    public static final String OBSERVABILITY_CORE = "observability-core";

    @Test
    public void library_version_self() {
        String selfVersion = LibraryVersion.get(OBSERVABILITY_CORE);
        Assert.assertNotEquals(selfVersion, LibraryVersion.UNKNOWN);
    }

    @Test
    public void library_version_unknown() {
        String unknownVersion = LibraryVersion.get("some-library");
        Assert.assertEquals(unknownVersion, LibraryVersion.UNKNOWN);
    }

    @Test
    public void library_version_01() {
        String version = LibraryVersion.get("foo");
        Assert.assertEquals(version, "1.2.3");

        Properties foo = LibraryVersion.getProperties("foo");
        Assert.assertEquals(foo.get("build"), "super-duper");
    }

    @Test
    public void library_version_02() {
        String version = LibraryVersion.get("bar");
        Assert.assertEquals(version, "0.9.8");
    }

    @Test
    public void library_version_03() {
        String version = LibraryVersion.get("malformed");
        Assert.assertEquals(version, LibraryVersion.UNKNOWN);
    }

    @Test
    public void library_version_list_cached() {
        LibraryVersion.resetCaches();
        LibraryVersion.get(OBSERVABILITY_CORE);
        LibraryVersion.get("foo");
        LibraryVersion.get("bar");
        LibraryVersion.get("malformed");
        LibraryVersion.get("unknown-library");

        Set<String> libraries = LibraryVersion.cachedLibraries();
        Assert.assertEquals(libraries.size(), 5);
    }

    @Test
    public void library_version_reset_caches() {
        LibraryVersion.resetCaches();
        Assert.assertEquals(LibraryVersion.cachedLibraries().size(), 0);

        Assert.assertNotEquals(LibraryVersion.get(OBSERVABILITY_CORE), LibraryVersion.UNKNOWN);
        Assert.assertEquals(LibraryVersion.cachedLibraries().size(), 1);

        LibraryVersion.resetCaches();
    }
}
