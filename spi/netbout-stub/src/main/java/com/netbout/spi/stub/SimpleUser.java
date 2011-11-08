/**
 * Copyright (c) 2009-2011, NetBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the NetBout.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netbout.spi.stub;

import com.netbout.spi.Entry;
import com.netbout.spi.Identity;
import com.netbout.spi.User;
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Simple implementation of a {@link User}.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class SimpleUser implements User {

    /**
     * The entry.
     */
    private final InMemoryEntry entry;

    /**
     * The name.
     */
    private final String name;

    /**
     * Collection of identities.
     */
    private final Collection<SimpleIdentity> identities =
        new ArrayList<SimpleIdentity>();

    /**
     * Public ctor.
     * @param ent The entry
     * @param nme The name of it
     * @see InMemoryEntry#register(String,String)
     */
    public SimpleUser(final InMemoryEntry ent, final String nme) {
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
        for (SimpleIdentity identity : this.identities) {
            if (identity.name().equals(label)) {
                Logger.info(
                    this,
                    "#identity('%s'): found",
                    label
                );
                return identity;
            }
        }
        final SimpleIdentity identity = new SimpleIdentity(this, label);
        this.identities.add(identity);
        Logger.info(
            this,
            "#identity('%s'): created",
            label
        );
        return identity;
    }

    /**
     * Get full list of his identities.
     * @return The list
     */
    public Collection<SimpleIdentity> getIdentities() {
        return this.identities;
    }

}
