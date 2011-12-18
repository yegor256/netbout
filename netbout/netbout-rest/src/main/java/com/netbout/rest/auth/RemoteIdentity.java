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

import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Urn;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Remote identity.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@XmlType(name = "identity")
@SuppressWarnings("PMD.TooManyMethods")
public final class RemoteIdentity implements Identity {

    /**
     * Authority.
     */
    private transient URL iauthority;

    /**
     * Identity name.
     */
    private transient Urn iname;

    /**
     * Photo of identity.
     */
    private transient URL iphoto;

    /**
     * Aliases.
     */
    private final transient Set<String> ialiases = new HashSet<String>();

    /**
     * Set authority.
     * @param url The name of it
     */
    @XmlElement
    public void setAuthority(final String url) {
        try {
            this.iauthority = new URL(url);
        } catch (java.net.MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Set identity name.
     * @param name The name of it
     */
    @XmlElement
    public void setName(final String name) {
        this.iname = Urn.create(name);
    }

    /**
     * Set photo.
     * @param url The URL
     */
    @XmlElement(name = "photo")
    public void setJaxbPhoto(final String url) {
        try {
            this.iphoto = new URL(url);
        } catch (java.net.MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Set aliases.
     * @param names List of them
     */
    @XmlElement
    public void setAliases(final List<String> names) {
        this.ialiases.addAll(names);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL authority() {
        if (this.iauthority == null) {
            throw new IllegalStateException("/page/identity/user missed");
        }
        return this.iauthority;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Urn name() {
        if (this.iname == null) {
            throw new IllegalStateException("/page/identity/name missed");
        }
        return this.iname;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL photo() {
        if (this.iphoto == null) {
            throw new IllegalStateException("/page/identity/photo missed");
        }
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
