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

import com.netbout.spi.BoutNotFoundException;
import com.netbout.spi.Helper;
import com.netbout.spi.Identity;
import com.netbout.spi.PromotionException;
import com.netbout.spi.User;
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
    private final HubEntry entry;

    /**
     * The name.
     */
    private final String name;

    /**
     * The secret.
     */
    private final String secret;

    /**
     * Collection of identities that belong to this user.
     */
    private final Collection<HubIdentity> identities =
        new ArrayList<HubIdentity>();

    /**
     * Public ctor.
     * @param ent The entry
     * @param nme The name of it
     * @param scrt The secret of it
     * @see InMemoryEntry#register(String,String)
     */
    public HubUser(final HubEntry ent, final String nme, final String scrt) {
        this.entry = ent;
        this.name = nme;
        this.secret = scrt;
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
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public Identity identity(final String label)
        throws UnknownIdentityException {
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
        throw new UnknownIdentityException(
            "Identity '%s' not found for user '%s'",
            label,
            this.name
        );
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public void identify(final String label, final URL photo)
        throws DuplicateIdentityException {
        for (HubIdentity identity : this.identities) {
            if (identity.name().equals(label)) {
                throw new DuplicateIdentityException(
                    "Identity '%s' is already attached to '%s' user",
                    label,
                    this.name
                );
            }
        }
        this.identities.add(new HubIdentity(this, label, photo));
        Logger.info(
            this,
            "#identify('%s', '%s'): done",
            label,
            photo
        );
    }

    /**
     * The user can be authenticated with provided params?
     * @param nam The name
     * @param scrt The secret
     * @return Is it me?
     */
    protected boolean authenticated(final String nam, final String scrt) {
        return this.name.equals(nam) && this.secret.equals(scrt);
    }

}
