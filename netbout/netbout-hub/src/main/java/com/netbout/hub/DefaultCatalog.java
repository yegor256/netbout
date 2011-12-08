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
import com.netbout.hub.data.DefaultBoutMgr;
import com.netbout.spi.Helper;
import com.netbout.spi.Identity;
import com.netbout.spi.UnreachableIdentityException;
import com.ymock.util.Logger;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Catalog of all known identities.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class DefaultCatalog implements Catalog {

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
     * Identity finder.
     */
    private final transient IdentityFinder finder;

    /**
     * Public ctor.
     * @param ibus The bus
     */
    public DefaultCatalog(final Bus ibus) {
        this.bus = ibus;
        this.manager = new DefaultBoutMgr(this.bus);
        this.validator = new DefaultNameValidator(this.bus);
        this.finder = new DefaultIdentityFinder(
            this, this.bus, this.all, this.validator
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public Identity make(final String name)
        throws UnreachableIdentityException {
        Identity identity;
        if (this.all.containsKey(name)) {
            identity = this.all.get(name);
        } else {
            identity = new HubIdentityOrphan(
                this.bus,
                this,
                this.manager,
                name
            );
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
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public Identity make(final String name, final User user)
        throws UnreachableIdentityException {
        Identity identity;
        if (this.all.containsKey(name)) {
            identity = this.all.get(name);
            if (identity instanceof HubIdentityOrphan) {
                identity = new HubIdentity(identity, user);
                this.save(name, identity);
                Logger.debug(
                    this,
                    "#make('%s', '%s'): orphan found his home",
                    name,
                    user.name()
                );
            } else if (identity instanceof HubIdentity) {
                this.assignedTo((HubIdentity) identity, user);
            } else if (!(identity instanceof Helper)) {
                identity = new HubIdentity(identity, user);
                this.save(name, identity);
                Logger.debug(
                    this,
                    "#make('%s', '%s'): stranger was addopted",
                    name,
                    user.name()
                );
            }
        } else {
            identity = new HubIdentity(this.make(name), user);
            this.save(name, identity);
            Logger.debug(
                this,
                "#make('%s', '%s'): new child was born",
                name,
                user.name()
            );
        }
        return identity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void promote(final Identity identity, final Helper helper) {
        Logger.info(
            this,
            "#promote('%s', '%s'): replacing existing identity (%s)",
            identity.name(),
            helper.getClass().getName(),
            this.all.get(identity.name()).getClass().getName()
        );
        this.all.put(identity.name(), helper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Identity> findByKeyword(final String keyword) {
        return this.finder.find(keyword);
    }

    /**
     * Save identity to storage.
     * @param name The name
     * @param identity The identity
     * @throws UnreachableIdentityException If can't reach it by name
     * @checkstyle RedundantThrows (4 lines)
     */
    private void save(final String name, final Identity identity)
        throws UnreachableIdentityException {
        this.all.put(this.validator.validate(name), identity);
        this.bus.make("identity-mentioned")
            .synchronously()
            .arg(name)
            .asDefault(true)
            .exec();
    }

    /**
     * Validate that this identity is assigned to the given user and
     * throw exception if it's not true.
     * @param identity The identity
     * @param user The user
     */
    private void assignedTo(final HubIdentity identity, final User user) {
        assert identity != null;
        assert user != null;
        if (!identity.user().equals(user.name())) {
            throw new IllegalArgumentException(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "Identity '%s' is already taken by '%s' and can't be re-assigned to '%s'",
                    identity.name(),
                    identity.user(),
                    user.name()
                )
            );
        }
    }

}
