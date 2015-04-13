/**
 * Copyright (c) 2009-2015, netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netbout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code accidentally and without intent to use it, please report this
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
package com.netbout.email;

import com.google.common.base.Joiner;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.email.Envelope;
import com.jcabi.email.Postman;
import com.jcabi.email.enclosure.EnHTML;
import com.jcabi.email.stamp.StRecipient;
import com.jcabi.email.stamp.StSubject;
import com.netbout.rest.Markdown;
import com.netbout.spi.Bout;
import com.netbout.spi.Friend;
import com.netbout.spi.Message;
import com.netbout.spi.Messages;
import com.netbout.spi.Pageable;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Email Messages.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.2
 */
@Immutable
@Loggable(Loggable.DEBUG)
@ToString(of = "origin")
@EqualsAndHashCode(of = "origin")
final class EmMessages implements Messages {

    /**
     * Original.
     */
    private final transient Messages origin;

    /**
     * My own alias.
     */
    private final transient String self;

    /**
     * Postman.
     */
    private final transient Postman postman;

    /**
     * Bout.
     */
    private final transient Bout bout;

    /**
     * Public ctor.
     * @param org Origin
     * @param slf Self alias
     * @param pst Postman
     * @param bot Bout we're in
     * @checkstyle ParameterNumberCheck (4 lines)
     */
    EmMessages(final Messages org, final String slf,
        final Postman pst, final Bout bot) {
        this.origin = org;
        this.self = slf;
        this.postman = pst;
        this.bout = bot;
    }

    @Override
    public void post(final String text) throws IOException {
        this.origin.post(text);
        for (final Friend friend : this.bout.friends().iterate()) {
            if (friend.email().isEmpty() || this.self == friend.alias()) {
                continue;
            }
            this.email(friend, text);
        }
    }

    @Override
    public long unread() throws IOException {
        return this.origin.unread();
    }

    @Override
    public Pageable<Message> jump(final long num) throws IOException {
        return new EmPageable<Message>(
            this.origin.jump(num),
            this.self,
            this.postman
        );
    }

    @Override
    public Iterable<Message> iterate() throws IOException {
        return this.origin.iterate();
    }

    /**
     * Send an email.
     * @param friend Friend to send to
     * @param text The text of the new message
     * @throws IOException If fails
     */
    private void email(final Friend friend, final String text)
        throws IOException {
        this.postman.send(
            new Envelope.MIME()
                .with(new StRecipient(friend.alias(), friend.email()))
                .with(
                    new StSubject(
                        String.format(
                            "#%d: %s",
                            this.bout.number(),
                            this.bout.title()
                        )
                    )
                )
                .with(
                    new EnHTML(
                        Joiner.on('\n').join(
                            new Markdown(text).html(),
                            "<p>--<br/>to reply click here: ",
                            String.format(
                                "http://www.netbout.com/b/%d</p>",
                                this.bout.number()
                            ),
                            "</p>"
                        )
                    )
                )
        );
    }

}
