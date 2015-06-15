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
package com.netbout.cached;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.netbout.spi.Message;
import com.netbout.spi.Messages;
import com.netbout.spi.Pageable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Cached Messages.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.2
 */
@Immutable
@Loggable(Loggable.DEBUG)
@ToString(of = "origin")
@EqualsAndHashCode(of = "origin")
final class CdMessages implements Messages {

    /**
     * Original.
     */
    private final transient Messages origin;

    /**
     * Flag to use for caching.
     */
    private final transient CdMessages.Flag flag;

    /**
     * Public ctor.
     * @param org Origin
     */
    CdMessages(final Messages org) {
        this.origin = org;
        this.flag = new CdMessages.Flag(org);
    }

    @Override
    @Cacheable.FlushBefore
    public void post(final String text) throws IOException {
        this.origin.post(text);
    }

    @Override
    public long unread() throws IOException {
        return this.flag.unread();
    }

    @Override
    public Pageable<Message> jump(final long number) throws IOException {
        return new CdPageable<Message>(this.origin.jump(number));
    }

    @Override
    public Iterable<Message> iterate() throws IOException {
        this.flag.touch();
        return Iterables.transform(
            this.origin.iterate(),
            new Function<Message, Message>() {
                @Override
                public Message apply(final Message input) {
                    return new CdMessage(input);
                }
            }
        );
    }

    @Override
    public Iterable<Message> search(final String term) throws IOException {
        this.flag.touch();
        return Iterables.transform(
            this.origin.search(term),
            new Function<Message, Message>() {
                @Override
                public Message apply(final Message input) {
                    return new CdMessage(input);
                }
            }
        );
    }
    /**
     * Flag of read/unread.
     * @since 2.6
     */
    @Immutable
    @Loggable(Loggable.DEBUG)
    @ToString(of = "messages")
    @EqualsAndHashCode(of = "messages")
    private static final class Flag {
        /**
         * Original.
         */
        private final transient Messages messages;
        /**
         * Public ctor.
         * @param org Origin
         */
        Flag(final Messages org) {
            this.messages = org;
        }
        /**
         * How many unread.
         * @return Number
         * @throws IOException If fails
         */
        @Cacheable(lifetime = Tv.FIVE, unit = TimeUnit.MINUTES)
        public long unread() throws IOException {
            return this.messages.unread();
        }
        /**
         * I've seen them all.
         */
        @Cacheable.FlushBefore
        public void touch() {
            // nothing special
        }
    }

}
