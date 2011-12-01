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
import java.util.Set;
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
    private final Bus bus;

    /**
     * Catalog of identities.
     */
    private final Catalog catalog;

    /**
     * Public ctor.
     * @param ibus The bus
     */
    public DefaultHub(final Bus ibus) {
        this.bus = ibus;
        this.catalog = new DefaultCatalog(this.bus);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User user(final String name) {
        final User user = new User(this.catalog, name);
        Logger.debug(
            this,
            "#user('%s'): instantiated",
            name
        );
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Element stats(final Document doc) {
        return this.catalog.stats(doc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void promote(final Identity identity, final Helper helper) {
        assert identity.equals(helper);
        this.bus.register(helper);
        this.catalog.promote(identity, helper);
    }

}
