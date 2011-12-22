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
import com.netbout.bus.BusMocker;
import com.netbout.spi.Identity;
import com.netbout.spi.IdentityMocker;
import com.netbout.spi.Urn;
import java.net.URL;
import org.mockito.Mockito;

/**
 * Mocker of {@link Hub}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class HubMocker {

    /**
     * The object.
     */
    private final transient Hub hub = Mockito.mock(Hub.class);

    /**
     * Public ctor.
     */
    public HubMocker() {
        this.withBus(new BusMocker().mock());
        this.withUrnResolver(new UrnResolverMocker().mock());
        this.withBoutMgr(new BoutMgrMocker().mock());
    }

    /**
     * With this bus.
     * @param bus The bus to use
     * @return This object
     */
    public HubMocker withBus(final Bus bus) {
        Mockito.doReturn(bus).when(this.hub).bus();
        return this;
    }

    /**
     * With this URN resolver.
     * @param resolver The resolver to use
     * @return This object
     */
    public HubMocker withUrnResolver(final UrnResolver resolver) {
        Mockito.doReturn(resolver).when(this.hub).resolver();
        return this;
    }

    /**
     * With this BoutMgr.
     * @param mgr The manager to use
     * @return This object
     */
    public HubMocker withBoutMgr(final BoutMgr mgr) {
        Mockito.doReturn(mgr).when(this.hub).manager();
        return this;
    }

    /**
     * With this identity on board.
     * @param name The name of it
     * @return This object
     */
    public HubMocker withIdentity(final String name) {
        return this.withIdentity(
            Urn.create(name),
            new IdentityMocker().namedAs(name).mock()
        );
    }

    /**
     * With this identity on board.
     * @param name The name of it
     * @param identity The identity
     * @return This object
     */
    public HubMocker withIdentity(final Urn name, final Identity identity) {
        try {
            Mockito.doReturn(identity).when(this.hub).identity(name);
        } catch (com.netbout.spi.UnreachableUrnException ex) {
            throw new IllegalArgumentException(ex);
        }
        return this;
    }

    /**
     * Build it.
     * @return The bout
     */
    public Hub mock() {
        return this.hub;
    }

}
