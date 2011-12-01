/**
 * Copyright (c) 2009-2011, netBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code occasionally and without intent to use it, please report this
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
import com.netbout.spi.BoutNotFoundException;
import com.netbout.spi.Helper;
import com.netbout.spi.Identity;
import com.netbout.spi.UnreachableIdentityException;
import com.ymock.util.Logger;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Identity.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class HubIdentity implements Identity {

    /**
     * Orphan identity.
     */
    private final transient Identity orphan;

    /**
     * User of this identity.
     */
    private final transient User iuser;

    /**
     * Public ctor.
     * @param orph Parent object
     * @param user The user
     */
    public HubIdentity(final Identity orph, final User user) {
        this.orphan = orph;
        this.iuser = user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        return this.orphan.equals(obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.orphan.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String user() {
        return this.iuser.name();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return this.orphan.name();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout start() {
        return this.orphan.start();
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public Bout bout(final Long number) throws BoutNotFoundException {
        return this.orphan.bout(number);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Bout> inbox(final String query) {
        return this.orphan.inbox(query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL photo() {
        return this.orphan.photo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPhoto(final URL pic) {
        this.orphan.setPhoto(pic);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity friend(final String name)
        throws UnreachableIdentityException {
        return this.orphan.friend(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Identity> friends(final String keyword) {
        return this.orphan.friends(keyword);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> aliases() {
        return this.orphan.aliases();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void alias(final String alias) {
        this.orphan.alias(alias);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invited(final Bout bout) {
        this.orphan.invited(bout);
    }

    /**
     * Does this identity belongs to the specified user?
     * @param user The user
     * @return Yes or no?
     */
    protected boolean belongsTo(final User user) {
        return this.iuser.equals(user);
    }

}
