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
package com.netbout.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Mocker of {@link Bout}.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class BoutMocker {

    /**
     * Mocked bout.
     */
    private final Bout bout = Mockito.mock(Bout.class);

    /**
     * List of participants.
     */
    private final Collection<Participant> participants =
        new ArrayList<Participant>();

    /**
     * List of messages.
     */
    private final List<Message> messages = new ArrayList<Message>();

    /**
     * Public ctor.
     */
    public BoutMocker() {
        Mockito.doReturn(this.messages).when(this.bout)
            .messages(Mockito.anyString());
        Mockito.doReturn(this.participants).when(this.bout).participants();
        try {
            Mockito.doAnswer(
                new Answer() {
                    public Object answer(final InvocationOnMock invocation) {
                        Long num = (Long) invocation.getArguments()[0];
                        return BoutMocker.this.messages.get(num.intValue());
                    }
                }
            ).when(this.bout).message(Mockito.anyLong());
        } catch (com.netbout.spi.MessageNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
        this.titledAs("some random text");
        this.withNumber(Math.abs(new Random().nextLong()));
    }

    /**
     * This is the title of bout.
     * @param The title of it
     * @return This object
     */
    public BoutMocker titledAs(final String title) {
        Mockito.doReturn(title).when(this.bout).title();
        return this;
    }

    /**
     * With this number.
     * @param The number
     * @return This object
     */
    public BoutMocker withNumber(final Long num) {
        Mockito.doReturn(num).when(this.bout).number();
        return this;
    }

    /**
     * With this message.
     * @param The text
     * @return This object
     */
    public BoutMocker withMessage(final String text) {
        this.messages.add(
            new MessageMocker()
                .inBout(this.bout)
                .withText(text)
                .mock()
        );
        return this;
    }

    /**
     * With this participant, by its name.
     * @param The name of it
     * @return This object
     */
    public BoutMocker withParticipant(final String name) {
        return this.withParticipant(new IdentityMocker().namedAs(name).mock());
    }

    /**
     * With this participant, by its name.
     * @param The name of it
     * @return This object
     */
    public BoutMocker withParticipant(final Urn name) {
        return this.withParticipant(new IdentityMocker().namedAs(name).mock());
    }

    /**
     * With this participant.
     * @param The identity
     * @return This object
     */
    public BoutMocker withParticipant(final Identity part) {
        this.participants.add(
            new ParticipantMocker()
                .inBout(this.bout)
                .withIdentity(part)
                .mock()
        );
        return this;
    }

    /**
     * Mock it.
     * @return Mocked bout
     */
    public Bout mock() {
        if (this.messages.isEmpty()) {
            this.withMessage("\u043F\u0440\u0438\u0432\u0435\u0442");
        }
        return this.bout;
    }

}
