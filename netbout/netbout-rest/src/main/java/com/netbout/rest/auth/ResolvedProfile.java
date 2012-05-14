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

import com.netbout.spi.Profile;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Resolved profile, instantiated in {@link ResolvedIdentity}.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class ResolvedProfile implements Profile {

    /**
     * Locale.
     */
    private transient Locale ilocale = Locale.ENGLISH;

    /**
     * Photo.
     */
    private transient URL iphoto;

    /**
     * Aliases.
     */
    private final transient Set<String> ialiases = new HashSet<String>();

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
    public Locale locale() {
        return this.ilocale;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> aliases() {
        return Collections.unmodifiableSet(this.ialiases);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLocale(final Locale locale) {
        this.ilocale = locale;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPhoto(final URL pic) {
        this.iphoto = pic;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void alias(final String alias) {
        if (!alias.isEmpty()) {
            this.ialiases.add(alias);
        }
    }

}
