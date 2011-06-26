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

// API
import com.netbout.engine.Bout;

/**
 * Implementation of a Bout.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class LazyBout implements Bout {

    /**
     * Manager of data entities.
     */
    private final BoutManager manager;

    /**
     * ID of the bout.
     */
    private final Long boutId;

    /**
     * Bout entity.
     */
    private BoutEnt bout;

    /**
     * Public ctor, for unit testing.
     * @param mgr The manager
     * @param bid Bout ID
     */
    public LazyBout(final BoutManager mgr, final Long bid) {
        this.manager = mgr;
        this.boutId = bid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long number() {
        return this.entity().number();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String title() {
        return this.entity().title();
    }

    /**
     * Load entity from manager.
     * @return The entity loaded
     */
    private BoutEnt entity() {
        synchronized (this) {
            if (this.bout == null) {
                this.bout = this.manager.find(this.boutId);
            }
            return this.bout;
        }
    }

}
