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
package io.telicent.smart.caches.configuration.auth;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TestCachingUserInfoLookup {

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".*lookup.*cannot be null")
    public void givenNoDelegate_whenCreatingCachingLookup_thenNPE() {
        // Given, When and Then
        new CachingUserInfoLookup(null, 0, null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*must be positive")
    public void givenZeroCacheSize_whenCreatingCachingLookup_thenNPE() {
        // Given, When and Then
        new CachingUserInfoLookup(mock(UserInfoLookup.class), 0, null);
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".*cannot be null")
    public void givenNoDuration_whenCreatingCachingLookup_thenNPE() {
        // Given, When and Then
        new CachingUserInfoLookup(mock(UserInfoLookup.class), 10, null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*must be positive")
    public void givenNegativeDuration_whenCreatingCachingLookup_thenIllegalArgument() {
        // Given, When and Then
        new CachingUserInfoLookup(mock(UserInfoLookup.class), 10, Duration.ofSeconds(-1));
    }

    @Test
    public void givenCachingLookup_whenLookingUpTwice_thenCacheIsUsed() throws UserInfoLookupException {
        // Given
        UserInfo info = UserInfo.builder().preferredName("Mr T. Test").build();
        UserInfoLookup actual = mock(UserInfoLookup.class);
        when(actual.lookup(any())).thenReturn(info);
        UserInfoLookup lookup = new CachingUserInfoLookup(actual, 10, Duration.ofSeconds(10));

        // When
        UserInfo firstLookup = lookup.lookup("token");
        UserInfo secondLookup = lookup.lookup("token");

        // Then
        Assert.assertEquals(firstLookup, info);
        Assert.assertEquals(secondLookup, info);
        Assert.assertSame(firstLookup, secondLookup);
        verify(actual, times(1)).lookup(any());
    }

    @Test
    public void givenCachingLookup_whenLookingUpForManyUniqueUsers_thenCacheIsNotUsed() throws UserInfoLookupException {
        // Given
        UserInfo info = UserInfo.builder().preferredName("Mr T. Test").build();
        UserInfoLookup actual = mock(UserInfoLookup.class);
        when(actual.lookup(any())).thenReturn(info);
        UserInfoLookup lookup = new CachingUserInfoLookup(actual, 10, Duration.ofMillis(250));

        // When
        lookupUniqueUsers(100, lookup);

        // Then
        verify(actual, times(100)).lookup(any());
    }

    private static void lookupUniqueUsers(int x, UserInfoLookup lookup) throws UserInfoLookupException {
        for (int i = 1; i <= x; i++) {
            lookup.lookup("token-" + i);
        }
    }

    @Test
    public void givenCachingLookup_whenLookingUpForSmallPoolOfUsers_thenCacheIsNotUsed_andEntriesExpireOnSubsequentLookups() throws UserInfoLookupException {
        // Given
        UserInfo info = UserInfo.builder().preferredName("Mr T. Test").build();
        UserInfoLookup actual = mock(UserInfoLookup.class);
        when(actual.lookup(any())).thenReturn(info);
        UserInfoLookup lookup = new CachingUserInfoLookup(actual, 10, Duration.ofMillis(250));

        // When
        lookupUniqueUsers(25, lookup);

        // Then
        verify(actual, times(25)).lookup(any());

        // And
        // On subsequent lookups some cache entries should be expired and force us to call the underlying lookup again
        lookupUniqueUsers(25, lookup);
        verify(actual, atLeast(26)).lookup(any());
    }
}
