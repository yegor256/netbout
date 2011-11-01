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
package com.netbout.stub;

import com.netbout.spi.Entry;
import com.netbout.spi.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory entry.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class InMemoryEntry implements Entry {

    /**
     * Collection of users.
     */
    private final Collection<SimpleUser> users = new ArrayList<SimpleUser>();

    /**
     * Collection of bouts.
     */
    private final Map<Integer, BoutData> bouts =
        new HashMap<Integer, BoutData>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(final String name, final String secret) {
        for (SimpleUser user : this.users) {
            if (user.getName().equals(name)) {
                throw new IllegalArgumentException(
                    "User with this name already registered"
                );
            }
        }
        this.users.add(new SimpleUser(this, name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User authenticate(final String name, final String secret) {
        for (SimpleUser user : this.users) {
            if (user.getName().equals(name)) {
                return user;
            }
        }
        throw new IllegalArgumentException(
            "User with this name is not found"
        );
    }

    /**
     * Add new bout to storage.
     * @param bout The bout to add
     * @return It's number (unique)
     */
    public Integer add(final BoutData bout) {
        Integer max = 1;
        for (Integer num : this.bouts.keySet()) {
            if (num >= max) {
                max = num + 1;
            }
        }
        this.bouts.put(max, bout);
        return max;
    }

    /**
     * Find and return bout from collection.
     * @param num Number of the bout
     * @return The bout found
     */
    public BoutData get(final Integer num) {
        return this.bouts.get(num);
    }

    /**
     * Return all bouts in storage.
     * @return All bouts
     */
    public Collection<BoutData> bouts() {
        return this.bouts.values();
    }

}
