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
package io.telicent.smart.cache.sources.file;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.Objects;

/**
 * A filter that selects only files with a specific extension and a numeric portion within their filename
 */
public class NumericallyNamedWithExtensionFilter implements FileFilter {
    private final String extension;

    /**
     * Creates a new filter
     *
     * @param extension Required file extension
     */
    public NumericallyNamedWithExtensionFilter(String extension) {
        if (StringUtils.isBlank(extension)) {
            throw new IllegalArgumentException("File extension cannot be null/blank");
        }
        if (!StringUtils.startsWith(extension, ".")) {
            throw new IllegalArgumentException("File extension must start with a . character");
        }
        this.extension = extension;
    }

    @Override
    public boolean accept(File f) {
        if (!f.isFile()) {
            return false;
        }
        if (!f.getName().contains(".")) {
            return false;
        }
        if (!Objects.equals(f.getName().substring(f.getName().indexOf('.')), extension)) {
            return false;
        }
        if (StringUtils.getDigits(f.getName()).isEmpty()) {
            return false;
        }
        return true;
    }
}
