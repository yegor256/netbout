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
import com.netbout.spi.Helper;
import com.netbout.spi.Identity;
import com.ymock.util.Logger;
import java.net.URL;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Entry point to Hub.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class DefaultHub implements Hub {

    /**
     * The bus.
     */
    private final transient Bus bus;

    /**
     * All identities known for us at the moment, and their objects.
     */
    private final transient ConcurrentMap<Urn, Identity> all =
        new ConcurrentHashMap<Urn, Identity>();

    /**
     * Manager of bouts.
     */
    private final transient BoutMgr manager;

    /**
     * Identity finder.
     */
    private final transient IdentityFinder finder;

    /**
     * Public ctor.
     * @param ibus The bus
     */
    public DefaultHub(final Bus ibus) {
        this.bus = ibus;
        this.manager = new DefaultBoutMgr(this.bus);
        this.finder = new DefaultIdentityFinder(
            this, this.bus, this.all, this.validator
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity identity(final Urn name) {
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
     */
    @Override
    public Element stats(final Document doc) {
        final Element root = doc.createElement("hub");
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
     */
    @Override
    public void promote(final Identity identity, final Helper helper) {
        this.bus.register(helper);
        final Identity existing = this.all.get(identity.name());
        try {
            this.save(identity.name(), helper);
        } catch (com.netbout.spi.UnreachableIdentityException ex) {
            throw new IllegalStateException(ex);
        }
        Logger.info(
            this,
            "#promote('%s', '%s'): replaced existing identity (%s)",
            identity.name(),
            helper.getClass().getName(),
            existing.getClass().getName()
        );
        this.bus.make("identity-promoted")
            .synchronously()
            .arg(identity.name())
            .arg(helper.location().toString())
            .asDefault(true)
            .exec();
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

}
