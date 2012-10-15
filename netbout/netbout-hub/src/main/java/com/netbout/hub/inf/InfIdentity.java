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
package com.netbout.hub.inf;

import com.netbout.spi.Bout;
import com.netbout.spi.Friend;
import com.netbout.spi.Identity;
import com.netbout.spi.Profile;
import com.netbout.spi.Urn;
import java.net.URL;
import java.util.Set;

/**
 * Identity to be seen by INF.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class InfIdentity implements Identity {

    /**
     * The name.
     */
    private final transient Urn iname;

    /**
     * Public ctor.
     * @param name The identity's name
     */
    public InfIdentity(final Urn name) {
        this.iname = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        throw new UnsupportedOperationException("#toString()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Friend friend) {
        throw new UnsupportedOperationException("#compareTo()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        throw new UnsupportedOperationException("#equals()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("#hashCode()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long eta() {
        throw new UnsupportedOperationException("#eta()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL authority() {
        throw new UnsupportedOperationException("#authority()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Urn name() {
        return this.iname;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout start() {
        throw new UnsupportedOperationException("#start()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout bout(final Long number) {
        throw new UnsupportedOperationException("#bout()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Bout> inbox(final String query) {
        throw new UnsupportedOperationException("#inbox()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Profile profile() {
        throw new UnsupportedOperationException("#profile()");
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public Friend friend(final Urn name) throws UnreachableUrnException {
        throw new UnsupportedOperationException("#friend()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Friend> friends(final String keyword) {
        throw new UnsupportedOperationException("#friends()");
    }

}
