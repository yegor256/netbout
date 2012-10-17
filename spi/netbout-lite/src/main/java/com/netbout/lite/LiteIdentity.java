/**
 * Copyright (c) 2009-2012, Netbout.com
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
package com.netbout.lite;

import com.netbout.spi.Bout;
import com.netbout.spi.Friend;
import com.netbout.spi.Identity;
import com.netbout.spi.OwnProfile;
import com.netbout.spi.Query;
import com.netbout.spi.Urn;
import java.net.URL;
import java.util.Set;

/**
 * Lite implementation of {@link Identity}.
 *
 * <p>The class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id: Bout.java 3447 2012-10-12 19:48:25Z yegor@tpc2.com $
 */
@SuppressWarnings("PMD.TooManyMethods")
final class LiteIdentity implements Identity {

    /**
     * Holder of bouts.
     */
    private final transient Bouts bouts;

    /**
     * Name of identity.
     */
    private final transient Urn urn;

    /**
     * Lite.
     */
    private final transient NetboutLite lite;

    /**
     * Public ctor.
     * @param bts Bouts
     * @param name Name of it
     * @param lte Lite netbout
     */
    public LiteIdentity(final Bouts bts, final Urn name,
        final NetboutLite lte) {
        this.bouts = bts;
        this.urn = name;
        this.lite = lte;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Friend friend) {
        return this.name().compareTo(friend.name());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        return obj == this || ((obj instanceof Identity)
            && this.name().equals(Identity.class.cast(obj).name()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.name().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.name().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long eta() {
        return 0L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL authority() {
        try {
            return new URL("http://lite.netbout.com/");
        } catch (java.net.MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Urn name() {
        return this.urn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout start() {
        return this.bouts.start(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Bout> inbox(final Query query) {
        return this.bouts.query(query, this);
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public Bout bout(final Long num) throws Identity.BoutNotFoundException {
        return this.bouts.get(num, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Friend friend(final Urn name) {
        return this.lite.login(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Friend> friends(final String mask) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OwnProfile profile() {
        throw new UnsupportedOperationException();
    }

}
