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

import com.netbout.data.IdentityEnt;
import com.netbout.data.UserEnt;
import com.netbout.engine.Identity;
import com.netbout.engine.User;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a User.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class DefaultUser implements User {

    /**
     * User entity.
     */
    private UserEnt user;

    /**
     * Public ctor, for unit testing.
     * @param ent The entity
     */
    public DefaultUser(final UserEnt ent) {
        this.user = ent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long number() {
        return this.user.number();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String secret() {
        return "secret-1";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Identity> identities() {
        final List<Identity> list = new ArrayList<Identity>();
        for (IdentityEnt ent : this.user.identities()) {
            list.add(new SimpleIdentity(ent));
        }
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity identity(final String text) {
        for (IdentityEnt ent : this.user.identities()) {
            if (ent.name().equals(text)) {
                return new SimpleIdentity(ent);
            }
        }
        throw new IllegalArgumentException("Identity not found: " + text);
    }

}
