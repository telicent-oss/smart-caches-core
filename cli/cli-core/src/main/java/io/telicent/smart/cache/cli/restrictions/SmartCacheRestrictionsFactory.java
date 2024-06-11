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
package io.telicent.smart.cache.cli.restrictions;

import com.github.rvesse.airline.restrictions.OptionRestriction;
import com.github.rvesse.airline.restrictions.common.AllowedRawValuesRestriction;
import com.github.rvesse.airline.restrictions.factories.OptionRestrictionFactory;
import io.telicent.smart.cache.sources.file.FileEventFormats;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Locale;

/**
 * A restriction factory for our Smart Cache related restrictions
 */
public class SmartCacheRestrictionsFactory implements OptionRestrictionFactory {
    @Override
    public OptionRestriction createOptionRestriction(Annotation annotation) {
        if (annotation instanceof AllowedEventFileFormats) {
            return new AllowedRawValuesRestriction(false, Locale.ROOT,
                                                   FileEventFormats.available().toArray(new String[0]));
        } else if (annotation instanceof SourceRequired sourceRequired) {
            return new SourceRequiredRestriction(sourceRequired.name(), sourceRequired.unlessEnvironment());
        } else if (annotation instanceof RequiredForSource requiredForSource) {
            return new RequiredForSourceRestriction(requiredForSource.sourceName(),
                                                    requiredForSource.unlessEnvironment());
        }
        return null;
    }

    @Override
    public List<Class<? extends Annotation>> supportedOptionAnnotations() {
        return List.of(AllowedEventFileFormats.class, SourceRequired.class, RequiredForSource.class);
    }
}
