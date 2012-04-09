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
package com.netbout.spi.cpa;

import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import com.netbout.spi.Participant;
import java.util.Collection;
import java.util.Date;

/**
 * Safe bout.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
final class SafeBout implements Bout {

    /**
     * Parent identity.
     */
    private final transient Identity identity;

    /**
     * Parent bout.
     */
    private final transient Bout bout;

    /**
     * Public ctor.
     * @param idnt The identity
     * @param bot Parent bout
     */
    public SafeBout(final Identity idnt, final Bout bot) {
        this.identity = idnt;
        this.bout = bot;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Bout bot) {
        return this.bout.compareTo(bot);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object bot) {
        return bot instanceof Bout
            && this.number().equals(((Bout) bot).number());
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
    public Long number() {
        return this.bout.number();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date date() {
        return this.bout.date();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String title() {
        return this.bout.title();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rename(final String text) {
        this.bout.rename(text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Participant> participants() {
        return this.bout.participants();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Participant invite(final Identity idnt)
        throws com.netbout.spi.DuplicateInvitationException {
        return this.bout.invite(idnt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Message> messages(final String query) {
        new Bump(this.identity).pause();
        return this.bout.messages(query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message message(final Long number)
        throws com.netbout.spi.MessageNotFoundException {
        return this.bout.message(number);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void confirm() {
        this.bout.confirm();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void leave() {
        this.bout.leave();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message post(final String text)
        throws com.netbout.spi.MessagePostException {
        return this.bout.post(text);
    }

}
