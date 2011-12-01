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

import com.netbout.spi.Identity;
import com.ymock.util.Logger;

/**
 * User.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class User {

    /**
     * Catalog.
     */
    private final transient Catalog catalog;

    /**
     * The name of it.
     */
    private final transient String uname;

    /**
     * Public ctor.
     * @param ctlr The catalog with identities
     * @param name The name of it
     * @see DefaultHub#user(String)
     */
    protected User(final Catalog ctlg, final String name) {
        this.catalog = ctlg;
        this.uname = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        return (obj instanceof User)
            && this.name().equals(((User) obj).name());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.uname.hashCode();
    }

    /**
     * Get its name.
     * @return The name of it
     */
    public String name() {
        return this.uname;
    }

    /**
     * Find identity by name.
     * @param name The name of it
     * @return The identity found
     * @throws com.netbout.spi.UnreachableIdentityException If can't..
     */
    public Identity identity(final String name)
        throws com.netbout.spi.UnreachableIdentityException {
        final Identity identity = this.catalog.make(name, this);
        Logger.debug(
            this,
            "#identity('%s'): found",
            name
        );
        return identity;
    }

}
