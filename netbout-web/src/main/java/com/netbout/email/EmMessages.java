/**
 * Copyright (c) 2009-2016, netbout.com
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
import com.jcabi.email.Postman;
import com.netbout.spi.Bout;
import com.netbout.spi.Friend;
import com.netbout.spi.Message;
import com.netbout.spi.Messages;
import com.netbout.spi.Pageable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.takes.facets.forward.RsFailure;

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
     * Postman.
     */
    private final transient Postman postman;

    /**
     * Bout.
     */
    private final transient Bout bout;

    /**
     * Self alias.
     */
    private final transient String self;

    /**
     * EmSender.
     */
    private final transient EmCourier courier;

    /**
     * Public ctor.
     * @param org Origin
     * @param pst Postman
     * @param bot Bout we're in
     * @param slf Self alias
     * @checkstyle ParameterNumberCheck (4 lines)
     */
    EmMessages(final Messages org, final Postman pst,
        final Bout bot, final String slf) {
        this.origin = org;
        this.postman = pst;
        this.bout = bot;
        this.self = slf;
        this.courier = new EmCourier(pst, bot);
    }

    @Override
    public void post(final String text) throws IOException {
        this.origin.post(text);
        final Collection<String> failed = new ArrayList<String>(16);
        for (final Friend friend : this.bout.friends().iterate()) {
            if (friend.email().isEmpty()
                || friend.alias().equals(this.self)
                || !this.bout.subscription(friend.alias())) {
                continue;
            }
            try {
                this.courier.email(this.self, friend, text);
            } catch (final IOException exception) {
                failed.add(friend.alias());
            }
        }
        if (!failed.isEmpty()) {
            final String message = String.format(
                "Sorry, we were not able to send the notification email to %s.",
                Joiner.on(", ").join(failed)
            );
            throw new RsFailure(new EmailDeliveryException(message));
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
            this.postman, this.self
        );
    }

    @Override
    public Iterable<Message> iterate() throws IOException {
        return this.origin.iterate();
    }

    @Override
    public Iterable<Message> search(final String term) throws IOException {
        return this.origin.search(term);
    }

    /**
     * Thowable when the email could not be delivered.
     * @see EmMessages#post(String)
     */
    final class EmailDeliveryException extends IOException {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = 0x7529FA78EED21470L;
        /**
         * Public ctor.
         * @param message The error message
         */
        public EmailDeliveryException(final String message) {
            super(message);
        }
    }
}
