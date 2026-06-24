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
package io.telicent.smart.cache.server.jaxrs.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.spi.FilterReply;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestRepeatedMessageFilter {

    private static final String AUTH_FILTER_LOGGER = "io.telicent.servlet.auth.jwt.AbstractJwtAuthFilter";
    private static final String AUTHZ_FILTER_LOGGER =
            "io.telicent.smart.cache.server.jaxrs.filters.TelicentAuthorizationFilter";

    private RepeatedMessageFilter createUnstartedFilter() {
        RepeatedMessageFilter filter = new RepeatedMessageFilter();
        filter.setContext(new LoggerContext());
        return filter;
    }

    private RepeatedMessageFilter createFilter(int repeatInterval) {
        RepeatedMessageFilter filter = createUnstartedFilter();
        filter.setLoggerNameSuffixes("AbstractJwtAuthFilter,TelicentAuthorizationFilter");
        filter.setLevels("WARN");
        filter.setMessageSubstrings("excluded from JWT Authentication filtering,excluded from Authorization");
        filter.setRepeatInterval(repeatInterval);
        filter.setMaxCacheSize(16);
        filter.start();
        return filter;
    }

    @Test
    public void givenRepeatedTrackedWarning_whenDeciding_thenOnlyFirstAndNthPass() {
        RepeatedMessageFilter filter = createFilter(3);
        Logger logger = new LoggerContext().getLogger(AUTH_FILTER_LOGGER);
        String format = "Request to path {} is excluded from JWT Authentication filtering by filter configuration";
        Object[] params = new Object[]{"/healthz"};

        Assert.assertEquals(filter.decide(null, logger, Level.WARN, format, params, null), FilterReply.NEUTRAL);
        Assert.assertEquals(filter.decide(null, logger, Level.WARN, format, params, null), FilterReply.DENY);
        Assert.assertEquals(filter.decide(null, logger, Level.WARN, format, params, null), FilterReply.NEUTRAL);
        Assert.assertEquals(filter.decide(null, logger, Level.WARN, format, params, null), FilterReply.DENY);
    }

    @Test
    public void givenTrackedWarning_whenMessageChanges_thenChangedMessagePassesImmediately() {
        RepeatedMessageFilter filter = createFilter(10);
        Logger logger = new LoggerContext().getLogger(AUTHZ_FILTER_LOGGER);
        String format = "Request to path {} is excluded from Authorization: {}";
        Object[] first = new Object[]{"http://host/healthz",
                "Authorization only applies to resources that require authentication"};
        Object[] changed = new Object[]{"http://host/version-info",
                "Authorization only applies to resources that require authentication"};

        Assert.assertEquals(filter.decide(null, logger, Level.WARN, format, first, null), FilterReply.NEUTRAL);
        Assert.assertEquals(filter.decide(null, logger, Level.WARN, format, first, null), FilterReply.DENY);
        Assert.assertEquals(filter.decide(null, logger, Level.WARN, format, changed, null), FilterReply.NEUTRAL);
    }

    @Test
    public void givenPathSubstrings_whenPathMatches_thenFirstOnlyAppliesPerDistinctMessage() {
        RepeatedMessageFilter filter = createUnstartedFilter();
        filter.setLoggerNameSuffixes("AbstractJwtAuthFilter,TelicentAuthorizationFilter");
        filter.setLevels("WARN");
        filter.setMessageSubstrings("excluded from JWT Authentication filtering,excluded from Authorization");
        filter.setPathSubstrings("/healthz,/version-info");
        filter.setMaxLoggedOccurrences(1);
        filter.setRepeatInterval(25);
        filter.setMaxCacheSize(16);
        filter.start();

        Logger authLogger = new LoggerContext().getLogger(AUTH_FILTER_LOGGER);
        Logger authzLogger = new LoggerContext().getLogger(AUTHZ_FILTER_LOGGER);

        Assert.assertEquals(filter.decide(null, authLogger, Level.WARN,
                                          "Request to path {} is excluded from JWT Authentication filtering by filter configuration",
                                          new Object[]{"/healthz"}, null), FilterReply.NEUTRAL);
        Assert.assertEquals(filter.decide(null, authzLogger, Level.WARN,
                                          "Request to path {} is excluded from Authorization: {}",
                                          new Object[]{"http://host/healthz",
                                                  "Authorization only applies to resources that require authentication"},
                                          null), FilterReply.NEUTRAL);
        Assert.assertEquals(filter.decide(null, authLogger, Level.WARN,
                                          "Request to path {} is excluded from JWT Authentication filtering by filter configuration",
                                          new Object[]{"/version-info"}, null), FilterReply.NEUTRAL);
        Assert.assertEquals(filter.decide(null, authzLogger, Level.WARN,
                                          "Request to path {} is excluded from Authorization: {}",
                                          new Object[]{"http://host/version-info",
                                                  "Authorization only applies to resources that require authentication"},
                                          null), FilterReply.NEUTRAL);

        Assert.assertEquals(filter.decide(null, authLogger, Level.WARN,
                                          "Request to path {} is excluded from JWT Authentication filtering by filter configuration",
                                          new Object[]{"/healthz"}, null), FilterReply.DENY);
        Assert.assertEquals(filter.decide(null, authzLogger, Level.WARN,
                                          "Request to path {} is excluded from Authorization: {}",
                                          new Object[]{"http://host/version-info",
                                                  "Authorization only applies to resources that require authentication"},
                                          null), FilterReply.DENY);
    }

    @Test
    public void givenPathSubstrings_whenPathDoesNotMatch_thenEventIsNotSuppressed() {
        RepeatedMessageFilter filter = createUnstartedFilter();
        filter.setLoggerNameSuffixes("AbstractJwtAuthFilter");
        filter.setLevels("WARN");
        filter.setMessageSubstrings("excluded from JWT Authentication filtering");
        filter.setPathSubstrings("/healthz,/version-info");
        filter.setMaxLoggedOccurrences(1);
        filter.setRepeatInterval(25);
        filter.setMaxCacheSize(16);
        filter.start();

        Logger logger = new LoggerContext().getLogger(AUTH_FILTER_LOGGER);
        String format = "Request to path {} is excluded from JWT Authentication filtering by filter configuration";
        Object[] params = new Object[]{"/metrics"};

        Assert.assertEquals(filter.decide(null, logger, Level.WARN, format, params, null), FilterReply.NEUTRAL);
        Assert.assertEquals(filter.decide(null, logger, Level.WARN, format, params, null), FilterReply.NEUTRAL);
    }

    @Test
    public void givenMaxLoggedOccurrencesOne_whenWarningRepeats_thenOnlyFirstPasses() {
        RepeatedMessageFilter filter = createUnstartedFilter();
        filter.setLoggerNameSuffixes("AbstractJwtAuthFilter");
        filter.setLevels("WARN");
        filter.setMessageSubstrings("excluded from JWT Authentication filtering");
        filter.setMaxLoggedOccurrences(1);
        filter.setRepeatInterval(25);
        filter.setMaxCacheSize(16);
        filter.start();

        Logger logger = new LoggerContext().getLogger(AUTH_FILTER_LOGGER);
        String format = "Request to path {} is excluded from JWT Authentication filtering by filter configuration";
        Object[] params = new Object[]{"/healthz"};

        Assert.assertEquals(filter.decide(null, logger, Level.WARN, format, params, null), FilterReply.NEUTRAL);
        Assert.assertEquals(filter.decide(null, logger, Level.WARN, format, params, null), FilterReply.DENY);
        Assert.assertEquals(filter.decide(null, logger, Level.WARN, format, params, null), FilterReply.DENY);
    }

    @Test
    public void givenMaxLoggedOccurrencesTwo_whenWarningRepeats_thenOnlyFirstTwoPass() {
        RepeatedMessageFilter filter = createUnstartedFilter();
        filter.setLoggerNameSuffixes("AbstractJwtAuthFilter");
        filter.setLevels("WARN");
        filter.setMessageSubstrings("excluded from JWT Authentication filtering");
        filter.setMaxLoggedOccurrences(2);
        filter.setRepeatInterval(25);
        filter.setMaxCacheSize(16);
        filter.start();

        Logger logger = new LoggerContext().getLogger(AUTH_FILTER_LOGGER);
        String format = "Request to path {} is excluded from JWT Authentication filtering by filter configuration";
        Object[] params = new Object[]{"/healthz"};

        Assert.assertEquals(filter.decide(null, logger, Level.WARN, format, params, null), FilterReply.NEUTRAL);
        Assert.assertEquals(filter.decide(null, logger, Level.WARN, format, params, null), FilterReply.NEUTRAL);
        Assert.assertEquals(filter.decide(null, logger, Level.WARN, format, params, null), FilterReply.DENY);
    }

    @Test
    public void givenTrackedErrorLevel_whenConfigured_thenItIsSuppressed() {
        RepeatedMessageFilter filter = createUnstartedFilter();
        filter.setLoggerNameSuffixes("SearchUnhandledExceptionMapper");
        filter.setLevels("ERROR");
        filter.setMessageSubstrings("Unhandled request failure in Search API");
        filter.setRepeatInterval(4);
        filter.setMaxCacheSize(4);
        filter.start();

        Logger logger = new LoggerContext().getLogger(
                "io.telicent.smart.cache.search.server.errors.SearchUnhandledExceptionMapper");

        Assert.assertEquals(filter.decide(null, logger, Level.ERROR, "Unhandled request failure in Search API", null,
                                          null), FilterReply.NEUTRAL);
        Assert.assertEquals(filter.decide(null, logger, Level.ERROR, "Unhandled request failure in Search API", null,
                                          null), FilterReply.DENY);
    }

    @Test
    public void givenTrackedMessageWithThrowable_whenThrowableChanges_thenChangedMessagePassesImmediately() {
        RepeatedMessageFilter filter = createUnstartedFilter();
        filter.setLoggerNameSuffixes("SearchUnhandledExceptionMapper");
        filter.setLevels("ERROR");
        filter.setMessageSubstrings("Unhandled request failure in Search API");
        filter.setRepeatInterval(10);
        filter.setMaxCacheSize(4);
        filter.start();

        Logger logger = new LoggerContext().getLogger(
                "io.telicent.smart.cache.search.server.errors.SearchUnhandledExceptionMapper");

        Assert.assertEquals(filter.decide(null, logger, Level.ERROR, "Unhandled request failure in Search API", null,
                                          new IllegalStateException("first")), FilterReply.NEUTRAL);
        Assert.assertEquals(filter.decide(null, logger, Level.ERROR, "Unhandled request failure in Search API", null,
                                          new IllegalStateException("first")), FilterReply.DENY);
        Assert.assertEquals(filter.decide(null, logger, Level.ERROR, "Unhandled request failure in Search API", null,
                                          new IllegalStateException("second")), FilterReply.NEUTRAL);
    }

    @Test
    public void givenUntrackedEvent_whenDeciding_thenItIsNotSuppressed() {
        RepeatedMessageFilter filter = createFilter(3);
        Logger logger = new LoggerContext().getLogger("io.telicent.smart.cache.server.jaxrs.OtherLogger");

        Assert.assertEquals(filter.decide(null, logger, Level.WARN, "ordinary warning", null, null),
                            FilterReply.NEUTRAL);
        Assert.assertEquals(filter.decide(null, logger, Level.ERROR,
                                          "Request to path /healthz is excluded from JWT Authentication filtering by filter configuration",
                                          null, null), FilterReply.NEUTRAL);
    }

    @Test
    public void givenTrackedLoggerWithDifferentMessage_whenDeciding_thenItIsNotSuppressed() {
        RepeatedMessageFilter filter = createFilter(3);
        Logger logger = new LoggerContext().getLogger(AUTH_FILTER_LOGGER);

        Assert.assertEquals(filter.decide(null, logger, Level.WARN, "ordinary warning", null, null),
                            FilterReply.NEUTRAL);
    }

    @Test
    public void givenUnstartedFilter_whenDeciding_thenNeutralReturned() {
        RepeatedMessageFilter filter = createUnstartedFilter();
        Logger logger = new LoggerContext().getLogger(AUTH_FILTER_LOGGER);
        String format = "Request to path {} is excluded from JWT Authentication filtering by filter configuration";
        Object[] params = new Object[]{"/healthz"};

        Assert.assertEquals(filter.decide(null, logger, Level.WARN, format, params, null), FilterReply.NEUTRAL);
        Assert.assertEquals(filter.decide(null, null, Level.WARN, format, params, null), FilterReply.NEUTRAL);
        Assert.assertEquals(filter.decide(null, logger, Level.WARN, null, null, null), FilterReply.NEUTRAL);
    }

    @Test
    public void givenInvalidRepeatInterval_whenStarting_thenFilterDoesNotStart() {
        RepeatedMessageFilter filter = createUnstartedFilter();
        filter.setLoggerNameSuffixes("AbstractJwtAuthFilter");
        filter.setLevels("WARN");
        filter.setMessageSubstrings("excluded from JWT Authentication filtering");
        filter.setRepeatInterval(1);

        filter.start();

        Assert.assertFalse(filter.isStarted());
    }

    @Test
    public void givenInvalidCacheSize_whenStarting_thenFilterDoesNotStart() {
        RepeatedMessageFilter filter = createUnstartedFilter();
        filter.setLoggerNameSuffixes("AbstractJwtAuthFilter");
        filter.setLevels("WARN");
        filter.setMessageSubstrings("excluded from JWT Authentication filtering");
        filter.setMaxCacheSize(0);

        filter.start();

        Assert.assertFalse(filter.isStarted());
    }

    @Test
    public void givenInvalidMaxLoggedOccurrences_whenStarting_thenFilterDoesNotStart() {
        RepeatedMessageFilter filter = createUnstartedFilter();
        filter.setLoggerNameSuffixes("AbstractJwtAuthFilter");
        filter.setLevels("WARN");
        filter.setMessageSubstrings("excluded from JWT Authentication filtering");
        filter.setMaxLoggedOccurrences(-1);

        filter.start();

        Assert.assertFalse(filter.isStarted());
    }

    @Test
    public void givenMissingLoggerSuffixes_whenStarting_thenFilterDoesNotStart() {
        RepeatedMessageFilter filter = createUnstartedFilter();
        filter.setLoggerNameSuffixes(null);
        filter.setLevels("WARN");
        filter.setMessageSubstrings("excluded from JWT Authentication filtering");

        filter.start();

        Assert.assertFalse(filter.isStarted());
    }

    @Test
    public void givenMissingLevels_whenStarting_thenFilterDoesNotStart() {
        RepeatedMessageFilter filter = createUnstartedFilter();
        filter.setLoggerNameSuffixes("AbstractJwtAuthFilter");
        filter.setLevels("   ");
        filter.setMessageSubstrings("excluded from JWT Authentication filtering");

        filter.start();

        Assert.assertFalse(filter.isStarted());
    }

    @Test
    public void givenMissingMessageSubstrings_whenStarting_thenFilterDoesNotStart() {
        RepeatedMessageFilter filter = createUnstartedFilter();
        filter.setLoggerNameSuffixes("AbstractJwtAuthFilter");
        filter.setLevels("WARN");
        filter.setMessageSubstrings("   ");

        filter.start();

        Assert.assertFalse(filter.isStarted());
    }

    @Test
    public void givenCsvWithBlankEntries_whenStarting_thenFilterStillStarts() {
        RepeatedMessageFilter filter = createUnstartedFilter();
        filter.setLoggerNameSuffixes("AbstractJwtAuthFilter, ,TelicentAuthorizationFilter");
        filter.setLevels("WARN, ,INFO");
        filter.setMessageSubstrings("excluded from JWT Authentication filtering, ,excluded from Authorization");
        filter.setPathSubstrings("/healthz, ,/version-info");

        filter.start();

        Assert.assertTrue(filter.isStarted());
    }
}
