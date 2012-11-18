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
package com.netbout.spi;

import com.jcabi.urn.URN;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;
import org.hamcrest.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Mocker of {@link Bout}.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class BoutMocker {

    /**
     * Mocked bout.
     */
    private final transient Bout bout = Mockito.mock(Bout.class);

    /**
     * List of participants.
     */
    private final transient Collection<Participant> participants =
        new ArrayList<Participant>();

    /**
     * List of messages.
     */
    private final transient List<Message> messages = new ArrayList<Message>();

    /**
     * Public ctor.
     */
    public BoutMocker() {
        Mockito.doReturn(this.messages).when(this.bout)
            .messages(Mockito.any(Query.class));
        Mockito.doReturn(this.participants).when(this.bout).participants();
        try {
            Mockito.doAnswer(
                new Answer<Object>() {
                    public Object answer(final InvocationOnMock invocation) {
                        final Long num = (Long) invocation.getArguments()[0];
                        return BoutMocker.this.messages.get(num.intValue());
                    }
                }
            ).when(this.bout).message(Mockito.anyLong());
        } catch (Bout.MessageNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
        this.titledAs("some random text");
        // @checkstyle MagicNumber (1 line)
        this.withNumber(new Random().nextInt(1024) + 128L);
        this.withDate(new Date());
    }

    /**
     * This is the title of bout.
     * @param title The title of it
     * @return This object
     */
    public BoutMocker titledAs(final String title) {
        Mockito.doReturn(title).when(this.bout).title();
        return this;
    }

    /**
     * With this number.
     * @param num The number
     * @return This object
     */
    public BoutMocker withNumber(final Long num) {
        Mockito.doReturn(num).when(this.bout).number();
        return this;
    }

    /**
     * With this date.
     * @param date The date
     * @return This object
     */
    public BoutMocker withDate(final Date date) {
        Mockito.doReturn(date).when(this.bout).date();
        return this;
    }

    /**
     * With this message.
     * @param text The text
     * @return This object
     */
    public BoutMocker withMessage(final String text) {
        return this.withMessage(
            new MessageMocker()
                .withText(text)
                .mock()
        );
    }

    /**
     * With this message.
     * @param msg The message
     * @return This object
     */
    public BoutMocker withMessage(final Message msg) {
        this.messages.add(msg);
        return this;
    }

    /**
     * With this message on this string in request.
     * @param mask The mask to find in query
     * @param text The text
     * @return This object
     */
    public BoutMocker messageOn(final String mask, final String text) {
        Mockito.doAnswer(
            new Answer<Object>() {
                public Object answer(final InvocationOnMock invocation) {
                    final List<Message> msgs = new ArrayList<Message>();
                    msgs.add(new MessageMocker().withText(text).mock());
                    return msgs;
                }
            }
        )
            .when(this.bout)
            .messages(
                Mockito.<Query>argThat(
                    Matchers.<Query>hasToString(Matchers.containsString(mask))
                )
            );
        return this;
    }

    /**
     * With this participant, by its name.
     * @param name The name of it
     * @return This object
     */
    public BoutMocker withParticipant(final String name) {
        return this.withParticipant(URN.create(name));
    }

    /**
     * With this participant.
     * @param dude The participant
     * @return This object
     */
    public BoutMocker withParticipant(final Participant dude) {
        this.participants.add(dude);
        return this;
    }

    /**
     * With this participant.
     * @param name Name of participant
     * @return This object
     */
    public BoutMocker withParticipant(final URN name) {
        return this.withParticipant(
            new ParticipantMocker()
                .withName(name)
                .mock()
        );
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
