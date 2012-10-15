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
package com.netbout.spi.cpa;

import com.netbout.spi.Bout;
import com.netbout.spi.Friend;
import com.netbout.spi.Identity;
import com.netbout.spi.Profile;
import com.netbout.spi.Query;
import com.netbout.spi.Urn;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

/**
 * Safe identity.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
final class SafeIdentity implements Identity {

    /**
     * Parent identity.
     */
    private final transient Identity identity;

    /**
     * Public ctor.
     * @param idnt Raw identity
     */
    public SafeIdentity(final Identity idnt) {
        this.identity = idnt;
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
    public boolean equals(final Object idnt) {
        return idnt == this || (idnt instanceof Identity
            && this.name().equals(((Identity) idnt).name()));
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
        return new SafeBout(this.identity, this.identity.start());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Bout> inbox(final Query query) {
        new Bump(this.identity).pause();
        final Iterable<Bout> bouts = this.identity.inbox(query);
        final Iterator<Bout> source = bouts.iterator();
        final Iterator<Bout> iterator = new Iterator<Bout>() {
            @Override
            public boolean hasNext() {
                return source.hasNext();
            }
            @Override
            public Bout next() {
                return new SafeBout(SafeIdentity.this.identity, source.next());
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException("#remove()");
            }
        };
        return new Iterable<Bout>() {
            @Override
            public Iterator<Bout> iterator() {
                return iterator;
            }
        };
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public Bout bout(final Long num)
        throws Identity.BoutNotFoundException {
        return this.identity.bout(num);
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
    public Set<Friend> friends(final String mask) {
        return this.identity.friends(mask);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Profile profile() {
        return this.identity.profile();
    }

}
