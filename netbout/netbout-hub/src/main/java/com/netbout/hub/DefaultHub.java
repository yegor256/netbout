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
import com.netbout.bus.TxBuilder;
import com.netbout.hub.data.DefaultBoutMgr;
import com.netbout.hub.hh.StatsFarm;
import com.netbout.spi.Helper;
import com.netbout.spi.Identity;
import com.netbout.spi.UnreachableUrnException;
import com.netbout.spi.Urn;
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

/**
 * Entry point to Hub.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.TooManyMethods")
@XmlType(name = "hub")
@XmlAccessorType(XmlAccessType.NONE)
public final class DefaultHub implements Hub {

    /**
     * The bus.
     */
    private final transient Bus ibus;

    /**
     * Manager of bouts.
     */
    private final transient BoutMgr imanager;

    /**
     * Resolver.
     */
    private final transient UrnResolver iresolver;

    /**
     * All identities known for us at the moment.
     */
    private final transient NavigableSet all =
        new ConcurrentSkipListSet<Identity>();

    /**
     * Public ctor, for JAXB.
     */
    public DefaultHub() {
        throw new IllegalStateException("illegal call");
    }

    /**
     * Public ctor.
     * @param bus The bus
     */
    public DefaultHub(final Bus bus) {
        this.ibus = bus;
        this.imanager = new DefaultBoutMgr(this);
        this.iresolver = new DefaultUrnResolver(this);
        StatsFarm.addStats(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UrnResolver resolver() {
        return this.iresolver;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TxBuilder make(final String mnemo) {
        return this.ibus.make(mnemo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BoutMgr manager() {
        return this.imanager;
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (3 lines)
     */
    @Override
    public Identity identity(final Urn name) throws UnreachableUrnException {
        this.resolver().authority(name);
        final DefaultHub.Token token = new DefaultHub.Token(name);
        Identity identity;
        if (this.all.contains(token)) {
            identity = (Identity) this.all.floor(token);
        } else {
            identity = new HubIdentity(this, name);
            this.save(identity);
            Logger.debug(
                this,
                "#identity('%s'): created new (%d total)",
                name,
                this.all.size()
            );
        }
        return identity;
    }

    /**
     * Get list of identities.
     * @return The list
     */
    @XmlElement(name = "identity")
    @XmlElementWrapper(name = "identities")
    public Collection<String> getIdentities() {
        final Collection<String> identities = new ArrayList<String>();
        for (Object object : this.all) {
            identities.add(((Identity) object).name().toString());
        }
        return identities;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void promote(final Identity identity, final Helper helper) {
        if (!(identity instanceof HubIdentity)) {
            throw new IllegalArgumentException(
                String.format(
                    "Can't promote '%s' since it's not from Hub",
                    identity.name()
                )
            );
        }
        this.ibus.register(identity, helper);
        Identity existing;
        try {
            existing = this.identity(identity.name());
        } catch (com.netbout.spi.UnreachableUrnException ex) {
            throw new IllegalArgumentException(ex);
        }
        this.all.remove(existing);
        this.save(new HelperIdentity((HubIdentity) identity, helper));
        Logger.info(
            this,
            "#promote('%s', '%[type]s'): replaced existing identity (%[type]s)",
            identity.name(),
            helper,
            existing
        );
        this.make("identity-promoted")
            .synchronously()
            .arg(identity.name())
            .arg(helper.location())
            .asDefault(true)
            .exec();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Identity> findByKeyword(final String keyword) {
        final Set<Identity> found = new HashSet<Identity>();
        final List<Urn> names = this
            .make("find-identities-by-keyword")
            .synchronously()
            .arg(keyword)
            .asDefault(new ArrayList<Urn>())
            .exec();
        for (Urn name : names) {
            try {
                found.add(this.identity(name));
            } catch (com.netbout.spi.UnreachableUrnException ex) {
                Logger.warn(
                    this,
                    // @checkstyle LineLength (1 line)
                    "#findByKeyword('%s'): some helper returned '%s' identity that is not reachable:\n%[exception]s",
                    keyword,
                    name,
                    ex
                );
            }
        }
        return found;
    }

    /**
     * Save identity to storage.
     * @param identity The identity
     */
    private void save(final Identity identity) {
        this.all.add(identity);
        this.make("identity-mentioned")
            .synchronously()
            .arg(identity.name())
            .asDefault(true)
            .exec();
    }

    /**
     * Token for searching of identities in storage.
     */
    private static final class Token implements Comparable<Identity> {
        /**
         * Name of identity.
         */
        private final transient Urn name;
        /**
         * Public ctor.
         * @param urn The name of identity
         */
        public Token(final Urn urn) {
            this.name = urn;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(final Identity identity) {
            return this.name.compareTo(identity.name());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(final Object obj) {
            return obj.hashCode() == this.hashCode();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return this.name.hashCode();
        }
    }

}
