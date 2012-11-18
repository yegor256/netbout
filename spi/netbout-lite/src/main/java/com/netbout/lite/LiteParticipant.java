/**
 * Copyright (c) 2009-2012, Netbout.com
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
package com.netbout.lite;

import com.jcabi.log.Logger;
import com.jcabi.urn.URN;
import com.netbout.spi.Friend;
import com.netbout.spi.Participant;
import com.netbout.spi.Profile;

/**
 * Lite implementation of {@link Participant}.
 *
 * <p>The class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class LiteParticipant implements Participant {

    /**
     * Name of identity.
     */
    private final transient URN urn;

    /**
     * Messages.
     */
    private final transient Messages messages;

    /**
     * Public ctor.
     * @param name Name of it
     * @param msgs Messages
     */
    public LiteParticipant(final URN name, final Messages msgs) {
        this.urn = name;
        this.messages = msgs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Friend friend) {
        return this.name().compareTo(friend.name());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.name().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URN name() {
        return this.urn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void kickOff() {
        this.messages.kickOff(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean confirmed() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean leader() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void consign() {
        Logger.info(this, "#consign(): ignored");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Profile profile() {
        throw new UnsupportedOperationException();
    }

}
