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
import com.netbout.spi.Bout;
import com.netbout.spi.Friend;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import com.netbout.spi.Participant;
import com.netbout.spi.Query;
import java.util.Collection;
import java.util.Date;

/**
 * Lite implementation of {@link Bout}.
 *
 * <p>The class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id: Bout.java 3447 2012-10-12 19:48:25Z yegor@tpc2.com $
 */
@SuppressWarnings("PMD.TooManyMethods")
final class LiteBout implements Bout {

    /**
     * Holder of messages.
     */
    private final transient Messages msgs;

    /**
     * Who is viewing this bout.
     */
    private final transient Identity identity;

    /**
     * Unique ID of the bout.
     */
    private final transient long num;

    /**
     * Public ctor.
     * @param messages Messages
     * @param who Identity/viewer
     * @param number The number of it
     */
    public LiteBout(final Messages messages, final Identity who,
        final long number) {
        this.msgs = messages;
        this.identity = who;
        this.num = number;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Bout bout) {
        return new Bout.Smart(this).updated()
            .compareTo(new Bout.Smart(bout).updated());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object bout) {
        return bout == this || (bout instanceof Bout
            && this.number().equals(Bout.class.cast(bout).number()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.number().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("#%d", this.number());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long number() {
        return this.num;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date date() {
        return new Date();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String title() {
        return String.format("title of bout #%d", this.number());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rename(final String text) {
        Logger.info(this, "#rename('%s'): ignored", text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Participant> participants() {
        return this.msgs.participants();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Participant invite(final Friend friend) {
        return this.msgs.invite(friend);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Message> messages(final Query query) {
        return this.msgs.query(query);
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public Message message(final Long number)
        throws Bout.MessageNotFoundException {
        return this.msgs.get(number);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void confirm() {
        Logger.info(this, "#confirm(): ignored in bout #%d", this.number());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void leave() {
        this.msgs.kickOff(this.identity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message post(final String text) {
        return this.msgs.post(text, this.identity);
    }

}
