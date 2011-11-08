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

import com.ymock.util.Logger;
import com.netbout.hub.data.BoutData;
import com.netbout.spi.Entry;
import com.netbout.spi.User;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Entry point to Hub.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class HubEntry implements Entry {

    /**
     * All users registered in the system.
     */
    private final Collection<HubUser> users = new ArrayList<HubUser>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(final String name, final String secret)
        throws DuplicateUserException {
        for (HubUser user : this.users) {
            if (user.getName().equals(name)) {
                throw new DuplicateUserException(
                    "User '%s' is already registered", name
                );
            }
        }
        this.users.add(new HubUser(this, name, secret));
        Logger.info(
            this,
            "#register('%s', '%s'): registered",
            name,
            secret
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User authenticate(final String name, final String secret) {
        for (HubUser user : this.users) {
            if (user.authenticated(name, secret)) {
                Logger.info(
                    this,
                    "#authenticate('%s', '%s'): completed",
                    name,
                    secret
                );
                return user;
            }
        }
        throw new AuthenticationException(
            "User '%s' not found or password is not correct", name
        );
    }

}
