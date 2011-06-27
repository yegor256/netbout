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
 * incident to the author by email: privacy@netbout.com.
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
package com.netbout.engine.impl;

// data access from com.netbout:netbout-data
import com.netbout.data.BoutEnt;
import com.netbout.data.BoutManager;
import com.netbout.data.jpa.JpaBoutManager;

// API
import com.netbout.engine.Bout;
import com.netbout.engine.BoutFactory;
import com.netbout.engine.Identity;

// JDK
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the default factory.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class DefaultBoutFactory implements BoutFactory {

    /**
     * Manager of data entities.
     */
    private final BoutManager manager;

    /**
     * Public ctor.
     */
    public DefaultBoutFactory() {
        this.manager = new JpaBoutManager();
    }

    /**
     * Protected ctor, for unit testing.
     * @param mgr The manager
     */
    public DefaultBoutFactory(final BoutManager mgr) {
        this.manager = mgr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout create(final Identity creator, final String title) {
        return new DefaultBout(
            this.manager.create(creator.name(), title)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout find(final Long boutId) {
        return new DefaultBout(this.manager.find(boutId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Bout> list(final String query) {
        final List<Bout> list = new ArrayList<Bout>();
        for (BoutEnt ent : this.manager.list(query)) {
            list.add(new DefaultBout(ent));
        }
        return list;
    }

}
