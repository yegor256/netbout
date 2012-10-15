/**
 * Copyright (c) 2009-2012, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code accidentally and without intent to use it, please report this
 * incident to the author by email.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.netbout.hub;

import com.netbout.spi.Bout;
import com.netbout.spi.Friend;
import com.netbout.spi.Helper;
import com.netbout.spi.Identity;
import com.netbout.spi.Profile;
import com.netbout.spi.Query;
import com.netbout.spi.Token;
import com.netbout.spi.Urn;
import java.net.URL;
import java.util.Set;

/**
 * Identity and Helper, together.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
final class HelperIdentity implements Identity, Helper {

    /**
     * The identity.
     */
    private final transient HubIdentity identity;

    /**
     * The helper.
     */
    private final transient Helper helper;

    /**
     * Public ctor.
     * @param idnt The identity
     * @param hlp The helper
     */
    public HelperIdentity(final HubIdentity idnt, final Helper hlp) {
        this.identity = idnt;
        this.helper = hlp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.identity.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Friend friend) {
        return this.identity.compareTo(friend);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        return obj == this || this.identity.equals(obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.identity.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long eta() {
        return this.identity.eta();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL authority() {
        return this.identity.authority();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Urn name() {
        return this.identity.name();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout start() {
        return this.identity.start();
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public Bout bout(final Long number) throws Identity.BoutNotFoundException {
        return this.identity.bout(number);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Bout> inbox(final Query query) {
        return this.identity.inbox(query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Profile profile() {
        return this.identity.profile();
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public Friend friend(final Urn name)
        throws Identity.UnreachableUrnException {
        return this.identity.friend(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Friend> friends(final String keyword) {
        return this.identity.friends(keyword);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL location() {
        return this.helper.location();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> supports() {
        return this.helper.supports();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws java.io.IOException {
        this.helper.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final Token token) {
        this.helper.execute(token);
    }

}
