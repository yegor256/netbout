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

import com.netbout.bus.Bus;
import com.netbout.spi.Identity;
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * Finds identities by keyword.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class DefaultIdentityFinder implements IdentityFinder {

    /**
     * Catalog of identities.
     */
    private final transient Catalog catalog;

    /**
     * All identities known for us at the moment, and their objects.
     */
    private final transient ConcurrentMap<String, Identity> all;

    /**
     * Bus to work with.
     */
    private final transient Bus bus;

    /**
     * Name validator.
     */
    private final transient NameValidator validator;

    /**
     * Public ctor.
     * @param ctlg The catalog
     * @param ibus The bus
     * @param existing Existing set of identities
     * @param vld Validator of names
     * @checkstyle ParameterNumber (4 lines)
     */
    public DefaultIdentityFinder(final Catalog ctlg, final Bus ibus,
        final ConcurrentMap<String, Identity> existing,
        final NameValidator vld) {
        this.catalog = ctlg;
        this.bus = ibus;
        this.all = existing;
        this.validator = vld;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Identity> find(final String keyword) {
        final Set<Identity> found = new HashSet<Identity>();
        for (Identity identity : this.all.values()) {
            if (this.matches(keyword, identity)) {
                found.add(identity);
            }
        }
        final List<String> external = this.bus
            .make("find-identities-by-keyword")
            .synchronously()
            .arg(keyword)
            .asDefault(new ArrayList<String>())
            .exec();
        for (String name : external) {
            try {
                found.add(this.catalog.make(name));
            } catch (com.netbout.spi.UnreachableIdentityException ex) {
                Logger.warn(
                    this,
                    // @checkstyle LineLength (1 line)
                    "#find('%s'): some helper returned '%s' identity that is not reachable",
                    keyword,
                    name
                );
            }
        }
        return this.withExact(found, keyword);
    }

    /**
     * Does this identity matches a keyword?
     * @param keyword The keyword
     * @param identity The identity
     * @return Yes or no?
     */
    private boolean matches(final String keyword,
        final Identity identity) {
        boolean matches = identity.name().contains(keyword);
        final Pattern pattern = Pattern.compile(
            Pattern.quote(keyword),
            Pattern.CASE_INSENSITIVE
        );
        for (String alias : identity.aliases()) {
            matches |= pattern.matcher(alias).find();
        }
        return matches;
    }

    /**
     * Add identity to the set which name is equal to the one provided.
     * @param found Set of found identities
     * @param name Name of the new one to add
     * @return New set of identities
     */
    public Set<Identity> withExact(final Set<Identity> found,
        final String name) {
        boolean exists = false;
        for (Identity identity : found) {
            if (identity.name().equals(name)) {
                exists = true;
            }
        }
        if (!exists && this.validator.isValid(name)) {
            try {
                found.add(
                    this.catalog.make(
                        name,
                        new HubUser(this.catalog, name)
                    )
                );
            } catch (com.netbout.spi.UnreachableIdentityException ex) {
                Logger.warn(
                    this,
                    "#addExact(%d, '%s'): strange, but it's unreachable",
                    found.size(),
                    name
                );
            }
        }
        return found;
    }

}
