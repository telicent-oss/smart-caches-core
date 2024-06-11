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
package io.telicent.smart.cache.sources.file.rdf;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

import java.io.File;
import java.io.FileFilter;

/**
 * A filter that selects only files with a specific extension and a numeric portion within their filename
 */
public class NumericallyNamedRdfFilter implements FileFilter {

    @Override
    public boolean accept(File f) {
        if (!f.isFile()) {
            return false;
        }
        if (!f.getName().contains(".")) {
            return false;
        }
        Lang lang = RDFLanguages.filenameToLang(f.getName());
        if (lang == null) {
            return false;
        }
        if (!RDFLanguages.hasRegisteredParser(lang)) {
            return false;
        }
        if (StringUtils.getDigits(f.getName()).isEmpty()) {
            return false;
        }
        return true;
    }
}
