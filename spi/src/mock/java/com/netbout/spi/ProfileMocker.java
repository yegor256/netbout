/**
 * Copyright (c) 2009-2011, NetBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the NetBout.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netbout.spi;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Mocker of {@link Profile}.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class ProfileMocker {

    /**
     * Mocked profile.
     */
    private final Profile profile = Mockito.mock(Profile.class);

    /**
     * Aliases (should be an array because we use #add(int,String) method.
     */
    private final List<String> aliases = new ArrayList<String>();

    /**
     * Public ctor.
     */
    public ProfileMocker() {
        this.withPhoto("http://localhost/set-by-ProfileMocker.png");
        this.withLocale(Locale.ENGLISH);
        Mockito.doAnswer(
            new Answer() {
                @Override
                public Object answer(final InvocationOnMock invocation) {
                    return new HashSet(ProfileMocker.this.aliases);
                }
            }
        ).when(this.profile).aliases();
    }

    /**
     * With this alias.
     * @param alias The alias
     * @return This object
     */
    public ProfileMocker withAlias(final String alias) {
        this.aliases.add(0, alias);
        return this;
    }

    /**
     * With this locale.
     * @param locale The locale
     * @return This object
     */
    public ProfileMocker withLocale(final Locale locale) {
        Mockito.doReturn(locale).when(this.profile).locale();
        return this;
    }

    /**
     * With this photo.
     * @param photo The photo
     * @return This object
     */
    public ProfileMocker withPhoto(final String photo) {
        try {
            Mockito.doReturn(new URL(photo)).when(this.profile).photo();
        } catch (java.net.MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
        return this;
    }

    /**
     * Mock it.
     * @return Mocked identity
     */
    public Profile mock() {
        if (this.aliases.isEmpty()) {
            this.withAlias("test identity alias set by ProfileMocker");
        }
        return this.profile;
    }

}
