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
package com.netbout.hub;

import com.netbout.spi.Identity;
import com.netbout.spi.Profile;
import com.ymock.util.Logger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Pattern;

/**
 * Profile.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class HubProfile implements Profile {

    /**
     * Default photo of identity.
     */
    private static final String DEFAULT_PHOTO =
        "http://cdn.netbout.com/unknown.png";

    /**
     * The hub.
     */
    private final transient PowerHub hub;

    /**
     * The identity.
     */
    private final transient Identity identity;

    /**
     * The locale of identity.
     */
    private transient Locale ilocale;

    /**
     * The photo.
     */
    private transient URL iphoto;

    /**
     * List of aliases.
     */
    private transient Set<String> ialiases;

    /**
     * Public ctor.
     * @param ihub The hub
     * @param owner The owner
     */
    public HubProfile(final PowerHub ihub, final Identity owner) {
        this.hub = ihub;
        this.identity = owner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Locale locale() {
        synchronized (this.identity) {
            if (this.ilocale == null) {
                final String lang = this.hub.make("get-locale-of-identity")
                    .synchronously()
                    .arg(this.identity.name())
                    .asDefault(Locale.ENGLISH.toString())
                    .exec();
                this.ilocale = new Locale(lang);
            }
            return this.ilocale;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLocale(final Locale locale) {
        final Locale previous = this.ilocale;
        synchronized (this.identity) {
            this.ilocale = locale;
        }
        if (previous == null || !previous.equals(locale)) {
            this.hub.make("set-identity-locale")
                .synchronously()
                .arg(this.identity.name())
                .arg(this.ilocale.toString())
                .asDefault(true)
                .exec();
            Logger.info(
                this,
                "Locale set to '%s' for '%s'",
                this.ilocale,
                this.identity.name()
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL photo() {
        synchronized (this.identity) {
            if (this.iphoto == null) {
                final URL url = this.hub.make("get-identity-photo")
                    .synchronously()
                    .arg(this.identity.name())
                    .asDefault(this.DEFAULT_PHOTO)
                    .exec();
                this.iphoto = new PhotoProxy(this.DEFAULT_PHOTO).normalize(url);
            }
            return this.iphoto;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPhoto(final URL url) {
        final URL previous = this.iphoto;
        synchronized (this.identity) {
            this.iphoto = new PhotoProxy(this.DEFAULT_PHOTO).normalize(url);
        }
        this.hub.make("identity-mentioned")
            .synchronously()
            .arg(this.identity.name())
            .asDefault(true)
            .exec();
        if (previous == null || !previous.equals(url)) {
            this.hub.make("changed-identity-photo")
                .synchronously()
                .arg(this.identity.name())
                .arg(this.iphoto)
                .asDefault(true)
                .exec();
            Logger.info(
                this,
                "Photo changed to '%s' for '%s'",
                this.iphoto,
                this.identity.name()
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> aliases() {
        final Set<String> list = new TreeSet<String>(
            new Comparator<String>() {
                private final transient Pattern pattern =
                    Pattern.compile("[a-zA-Z ]+");
                @Override
                public int compare(final String left, final String right) {
                    int result;
                    if (this.pattern.matcher(left).matches()) {
                        result = -1;
                    } else {
                        result = 1;
                    }
                    return result;
                }
            }
        );
        list.addAll(this.myAliases());
        Logger.debug(
            this,
            "#aliases(): %d returned",
            list.size()
        );
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void alias(final String alias) {
        if (alias == null || alias.isEmpty()) {
            throw new IllegalArgumentException("alias can't be empty");
        }
        synchronized (this.identity) {
            if (this.myAliases().contains(alias)) {
                Logger.debug(
                    this,
                    "#alias('%s'): it's already set for '%s'",
                    alias,
                    this.identity.name()
                );
            } else {
                this.hub.make("added-identity-alias")
                    .asap()
                    .arg(this.identity.name())
                    .arg(alias)
                    .asDefault(true)
                    .exec();
                Logger.info(
                    this,
                    "Alias '%s' added for '%s'",
                    alias,
                    this.identity.name()
                );
                this.myAliases().add(alias);
            }
        }
    }

    /**
     * Returns a link to the list of aliases.
     * @return The link to the list of them
     */
    private Set<String> myAliases() {
        synchronized (this.identity) {
            if (this.ialiases == null) {
                this.ialiases = new CopyOnWriteArraySet<String>(
                    (List<String>) this.hub
                        .make("get-aliases-of-identity")
                        .synchronously()
                        .arg(this.identity.name())
                        .asDefault(new ArrayList<String>())
                        .exec()
                );
            }
        }
        return this.ialiases;
    }

}
