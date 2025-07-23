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

import com.github.rvesse.airline.help.sections.HelpFormat;
import com.github.rvesse.airline.help.sections.HelpHint;
import com.github.rvesse.airline.model.OptionMetadata;
import com.github.rvesse.airline.parser.ParseState;
import com.github.rvesse.airline.parser.errors.ParseOptionGroupException;
import com.github.rvesse.airline.restrictions.OptionRestriction;
import com.github.rvesse.airline.utils.predicates.parser.ParsedOptionFinder;
import io.telicent.smart.cache.configuration.Configurator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A restriction that requires that one and only one source option is specified, or a suitable environment variable
 */
public class SourceRequiredRestriction implements OptionRestriction, HelpHint {

    //@formatter:off
    private static final Predicate<Pair<OptionMetadata, Object>>
            IS_SOURCE_OPTION = o -> o.getKey()
                                     .getRestrictions()
                                     .stream()
                                     .anyMatch(r -> r instanceof SourceRequiredRestriction);
    private static final Predicate<OptionMetadata>
            HAS_SOURCE_RESTRICTION = o -> o.getRestrictions()
                                           .stream()
                                           .anyMatch(r -> r instanceof SourceRequiredRestriction);
    //@formatter:on
    private final String sourceName;
    private final Set<String> envVars = new HashSet<>();

    /**
     * Creates a new source required restriction
     *
     * @param name          Name of the event source
     * @param unlessEnvVars Environment variables that may be used to specify the source instead
     */
    public SourceRequiredRestriction(String name, String... unlessEnvVars) {
        this.sourceName = name;
        if (unlessEnvVars != null) {
            CollectionUtils.addAll(envVars, unlessEnvVars);
        }
    }

    /**
     * Gets the event source name to which this restriction is applied
     *
     * @return Event source name
     */
    public String getSourceName() {
        return this.sourceName;
    }

    /**
     * Gets the fallback environment variables that may be used to configure the source without explicitly specifying
     * the option to which this restriction applies
     *
     * @return Fallback environment variables
     */
    public Set<String> getUnlessEnvironmentVariables() {
        return this.envVars;
    }

    @Override
    public <T> void finalValidate(ParseState<T> state, OptionMetadata option) {
        ParsedOptionFinder parsedOptionFinder = new ParsedOptionFinder(option);
        Collection<Pair<OptionMetadata, Object>> parsedOptions =
                state.getParsedOptions().stream().filter(parsedOptionFinder::evaluate).collect(Collectors.toList());


        // Find other parsed options which have the source required restriction
        Collection<Pair<OptionMetadata, Object>> otherParsedOptions =
                state.getParsedOptions().stream().filter(IS_SOURCE_OPTION).collect(Collectors.toList());

        if (parsedOptions.isEmpty() && otherParsedOptions.isEmpty()) {
            // No source options specified, are any of the fallback environment variables specified for any of the
            // source restrictions?
            Collection<OptionMetadata> sourceOptions = getSourceOptions(state);
            //@formatter:off
            if (sourceOptions.stream()
                             .flatMap(o -> o.getRestrictions().stream())
                             .filter(r -> r instanceof SourceRequiredRestriction)
                             .flatMap(r -> ((SourceRequiredRestriction) r).envVars.stream())
                             .anyMatch(e -> StringUtils.isNotBlank(Configurator.get(e)))) {
                return;
            }
            //@formatter:on


            throw new ParseOptionGroupException(
                    "At least one of the following options must be specified but none were found: %s", "event-sources",
                    sourceOptions, toOptionsList(sourceOptions));
        }

        // There are some parsed options but ONLY for this option
        // NB - Specifying a single source option multiple times is fine as the latter option just overwrites the
        //      earlier one
        if (!otherParsedOptions.isEmpty() && otherParsedOptions.size() == parsedOptions.size()) {
            return;
        }

        // Otherwise may need to error if multiple source options have been specified
        if (!parsedOptions.isEmpty() && otherParsedOptions.size() > parsedOptions.size()) {
            Collection<OptionMetadata> sourceOptions =
                    otherParsedOptions.stream().map(Pair::getKey).distinct().toList();
            throw new ParseOptionGroupException(
                    "Only one of the following options may be specified but %d were found: %s", "event-sources",
                    sourceOptions, otherParsedOptions.size(), toOptionsList(sourceOptions));
        }
    }

    static <T> Collection<OptionMetadata> getSourceOptions(ParseState<T> state) {
        List<OptionMetadata> options = state.getCommand() != null ? state.getCommand().getAllOptions() : null;
        if (options == null) {
            options = state.getGroup() != null ? state.getGroup().getOptions() : null;
        }
        if (options == null) {
            options = state.getGlobal() != null ? state.getGlobal().getOptions() :
                      Collections.emptyList();
        }
        return options.stream().filter(HAS_SOURCE_RESTRICTION).collect(Collectors.toList());
    }

    private static String toOptionsList(Iterable<OptionMetadata> options) {
        StringBuilder builder = new StringBuilder();
        Iterator<OptionMetadata> ops = options.iterator();
        while (ops.hasNext()) {
            OptionMetadata option = ops.next();

            Iterator<String> names = option.getOptions().iterator();
            while (names.hasNext()) {
                builder.append(names.next());
                if (names.hasNext() || ops.hasNext()) {
                    builder.append(", ");
                }
            }
        }
        return builder.toString();
    }

    @Override
    public String getPreamble() {
        return !this.envVars.isEmpty() ? String.format(
                "This option configures the %s event source, exactly one event source must be configured.  This event source can also be configured by setting one of the following environment variables: ",
                this.sourceName) : null;
    }

    @Override
    public HelpFormat getFormat() {
        return !this.envVars.isEmpty() ? HelpFormat.LIST : HelpFormat.PROSE;
    }

    @Override
    public int numContentBlocks() {
        return 1;
    }

    @Override
    public String[] getContentBlock(int blockNumber) {
        if (blockNumber != 0) {
            throw new IndexOutOfBoundsException();
        }
        if (!this.envVars.isEmpty()) {
            return this.envVars.toArray(new String[0]);
        } else {
            return new String[] {
                    String.format(
                            "This option configures the %s event source, exactly one event source must be configured.",
                            this.sourceName)
            };
        }
    }

    @Override
    public <T> void preValidate(ParseState<T> state, OptionMetadata option, String value) {
        // No pre-validation
    }

    @Override
    public <T> void postValidate(ParseState<T> state, OptionMetadata option, Object value) {
        // No post-validation
    }
}
