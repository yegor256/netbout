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

import com.netbout.spi.Entry;
import com.netbout.spi.Identity;
import com.netbout.spi.User;
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.Collection;

/**
 * User.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class HubUser implements User {

    /**
     * The entry.
     */
    private final Entry entry;

    /**
     * The name.
     */
    private final String name;

    /**
     * Collection of identities that belong to this user.
     */
    private final Collection<HubIdentity> identities =
        new ArrayList<HubIdentity>();

    /**
     * Public ctor.
     * @param ent The entry
     * @param nme The name of it
     * @see InMemoryEntry#user(String)
     */
    public HubUser(final Entry ent, final String nme) {
        this.entry = ent;
        this.name = nme;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Entry entry() {
        return this.entry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity identity(final String label) {
        for (HubIdentity identity : this.identities) {
            if (identity.name().equals(label)) {
                Logger.info(
                    this,
                    "#identity('%s'): found",
                    label
                );
                return identity;
            }
        }
        final HubIdentity identity = new HubIdentity(this, label);
        this.identities.add(identity);
        Logger.info(
            this,
            "#identity('%s'): created new",
            label
        );
        return identity;
    }

    /**
     * User has this identity?
     * @param label The name of the identity to find
     * @return It has?
     */
    protected boolean hasIdentity(final String label) {
        for (HubIdentity identity : this.identities) {
            if (identity.name().equals(label)) {
                return true;
            }
        }
        return false;
    }

}
