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
import com.netbout.spi.UnknownIdentityException;
import com.netbout.spi.User;
import com.ymock.util.Logger;
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
     * Static instance.
     */
    public static final HubEntry INSTANCE = new HubEntry();

    /**
     * All users registered in the system.
     */
    private final Collection<HubUser> users = new ArrayList<HubUser>();

    /**
     * {@inheritDoc}
     */
    @Override
    public User user(final String name) {
        for (User existing : this.users) {
            if (existing.name().equals(name)) {
                Logger.info(
                    this,
                    "#user('%s'): user found",
                    name
                );
                return existing;
            }
        }
        final HubUser user = new HubUser(this, name);
        this.users.add(user);
        Logger.info(
            this,
            "#user('%s'): new user registered",
            name
        );
        return user;
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public Identity identity(final String name)
        throws UnknownIdentityException {
        for (HubUser user : this.users) {
            if (user.hasIdentity(name)) {
                Logger.info(
                    this,
                    "#identity('%s'): identity found",
                    name
                );
                return user.identity(name);
            }
        }
        throw new UnknownIdentityException("Identity '%s' not found", name);
    }

}
