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
import com.netbout.hub.data.BoutMgr;
import com.netbout.spi.Helper;
import com.netbout.spi.Identity;
import com.netbout.spi.UnreachableIdentityException;
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Catalog of all known identities.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class Catalog {

    /**
     * All identities known for us at the moment, and their objects.
     */
    private final transient ConcurrentMap<String, Identity> all =
        new ConcurrentHashMap<String, Identity>();

    /**
     * Bus to work with.
     */
    private final transient Bus bus;

    /**
     * Manager of bouts.
     */
    private final transient BoutMgr manager;

    /**
     * Name validator.
     */
    private final transient NameValidator validator;

    /**
     * Public ctor.
     * @param ibus The bus
     */
    public Catalog(final Bus ibus) {
        this.bus = ibus;
        this.manager = new BoutMgr(this.bus);
        this.validator = new NameValidator(this.bus);
    }

    /**
     * Create statistics in the given XML document and return their element.
     * @param doc The document to work in
     * @return The element just created
     */
    public Element stats(final Document doc) {
        final Element root = doc.createElement("catalog");
        final Element identities = doc.createElement("identities");
        root.appendChild(identities);
        for (String name : this.all.keySet()) {
            final Element identity = doc.createElement("identity");
            identities.appendChild(identity);
            identity.appendChild(doc.createTextNode(name));
        }
        root.appendChild(this.manager.stats(doc));
        return root;
    }

    /**
     * Make new identity for the specified user, or find existing one and
     * assign to this user.
     * @param name The name of identity
     * @param user Name of the user
     * @return Identity found or created
     * @throws UnreachableIdentityException If can't reach it by name
     */
    public Identity make(final String name, final User user)
        throws UnreachableIdentityException {
        Identity identity;
        if (this.all.containsKey(name)) {
            identity = this.all.get(name);
            if (identity instanceof HubIdentityOrphan) {
                identity = new HubIdentity(identity, user);
                this.save(name, identity);
            } else if (!((HubIdentity) identity).belongsTo(user)) {
                throw new IllegalArgumentException(
                    String.format(
                        "Identity '%s' is already taken by '%s'",
                        name,
                        identity.user()
                    )
                );
            }
        } else {
            identity = new HubIdentity(this.make(name), user);
            this.save(name, identity);
        }
        return identity;
    }

    /**
     * Make new identity or find existing one.
     * @param name The name of identity
     * @return Identity found
     * @throws UnreachableIdentityException If can't reach it by name
     */
    public Identity make(final String name)
        throws UnreachableIdentityException {
        Identity identity;
        if (this.all.containsKey(name)) {
            identity = this.all.get(name);
        } else {
            identity =
                new HubIdentityOrphan(this.bus, this, this.manager, name);
            this.save(name, identity);
            Logger.debug(
                this,
                "#make('%s'): created just by name (%d total)",
                name,
                this.all.size()
            );
        }
        return identity;
    }

    /**
     * Promote existing identity to the helper.
     * @param identity The identity to promote
     * @param helper The helper to use
     */
    public void promote(final Identity identity, final Helper helper) {
        this.all.put(identity.name(), helper);
    }

    /**
     * Find identities by name (including aliases).
     * @param keyword The keyword to find by
     * @return Identities found
     */
    public Set<Identity> findByKeyword(final String keyword) {
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
                found.add(this.make(name));
            } catch (com.netbout.spi.UnreachableIdentityException ex) {
                Logger.warn(
                    this,
                    // @checkstyle LineLength (1 line)
                    "#findByKeyword('%s'): some helper returned '%s' identity that is not reachable",
                    keyword,
                    name
                );
            }
        }
        return found;
    }

    /**
     * Save identity to storage.
     * @param name The name
     * @param identity The identity
     * @throws UnreachableIdentityException If can't reach it by name
     */
    private void save(final String name, final Identity identity)
        throws UnreachableIdentityException {
        this.all.put(this.validator.ifValid(name), identity);
        this.bus.make("identity-mentioned")
            .synchronously()
            .arg(name)
            .asDefault(true)
            .exec();
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

}
