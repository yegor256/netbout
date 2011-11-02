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

import com.netbout.spi.Bout;
import com.netbout.spi.BoutNotFoundException;
import com.netbout.spi.Helper;
import com.netbout.spi.Identity;
import com.netbout.spi.User;
import com.ymock.util.Logger;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple implementation of a {@link Identity}.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class SimpleIdentity implements Identity {

    /**
     * The user.
     */
    private final SimpleUser user;

    /**
     * The name.
     */
    private final String name;

    /**
     * The helper, if exists.
     */
    private Helper helper;

    /**
     * Public ctor.
     * @param usr The user of this identity
     * @param nam The identity's name
     * @see SimpleUser#identity(String)
     */
    public SimpleIdentity(final SimpleUser usr, final String nam) {
        this.user = usr;
        this.name = nam;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User user() {
        return this.user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout start() {
        final Long num = ((InMemoryEntry) this.user.entry()).createBout();
        BoutData data;
        try {
            data = ((InMemoryEntry) this.user.entry()).findBout(num);
        } catch (BoutNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
        data.addParticipant(new ParticipantData(this, true));
        Logger.info(
            this,
            "#start(): bout started"
        );
        return new SimpleBout(this, data);
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public Bout bout(final Long number) throws BoutNotFoundException {
        return new SimpleBout(
            this,
            ((InMemoryEntry) this.user.entry()).findBout(number)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Bout> inbox(final String query) {
        final List<Bout> list = new ArrayList<Bout>();
        for (BoutData data
            : ((InMemoryEntry) this.user.entry()).getAllBouts()) {
            for (ParticipantData dude : data.getParticipants()) {
                if (this.equals(dude.getIdentity())) {
                    list.add(new SimpleBout(this, data));
                    break;
                }
            }
        }
        Logger.info(
            this,
            "#inbox('%s'): %d bouts found",
            query,
            list.size()
        );
        return list;
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
    public URL photo() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void promote(final Helper hlp) {
        this.helper = hlp;
        Logger.info(
            this,
            "#promote(%s): '%s' promoted",
            hlp.getClass().getName(),
            this.name()
        );
    }

    /**
     * Get helper, if it's set (NULL otherwise).
     * @return The helper
     */
    public Helper getHelper() {
        return this.helper;
    }

}
