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
package com.netbout.rest.auth;

import com.netbout.rest.AbstractRs;
import com.netbout.rest.page.PageBuilder;
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Urn;
import com.netbout.utils.Cipher;
import com.ymock.util.Logger;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * Resolved identity.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class ResolvedIdentity implements Identity {

    /**
     * Authority.
     */
    private final transient URL iauthority;

    /**
     * The name of it.
     */
    private final transient Urn iname;

    /**
     * Photo.
     */
    private final transient URL iphoto;

    /**
     * Aliases.
     */
    private final transient Set<String> ialiases = new HashSet<String>();

    /**
     * Public ctor.
     * @param name The name
     */
    public ResolvedIdentity(final URL authority, final Urn name,
        final URL photo) {
        this.iauthority = authority;
        this.iname = name;
        this.iphoto = photo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL authority() {
        return this.iauthority;
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
    public URL photo() {
        return this.iphoto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> aliases() {
        return this.ialiases;
    }

    /**
     * Add new alias.
     * @param alias The alias to add
     * @return This object
     */
    protected ResolvedIdentity addAlias(final String alias) {
        this.ialiases.add(alias);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Identity identity) {
        throw new UnsupportedOperationException("#compareTo()");
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
    public List<Bout> inbox(final String query) {
        throw new UnsupportedOperationException("#inbox()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPhoto(final URL pic) {
        throw new UnsupportedOperationException("#setPhoto()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity friend(final Urn name) {
        throw new UnsupportedOperationException("#friend()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Identity> friends(final String keyword) {
        throw new UnsupportedOperationException("#friends()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void alias(final String alias) {
        throw new UnsupportedOperationException("#alias()");
    }

}
