/**
 * Copyright (c) 2009-2012, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
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
package com.netbout.inf.notices;

import com.netbout.inf.Notice;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import com.netbout.spi.Urn;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Message-related notice.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
public interface MessageNotice extends Notice {

    /**
     * Link to the message.
     * @return The message
     */
    Message message();

    /**
     * Serializer.
     */
    class Serial implements Serializer<MessageNotice> {
        /**
         * {@inheritDoc}
         */
        @Override
        public String nameOf(final MessageNotice notice) {
            return String.format(
                "message:%d",
                notice.message().number()
            );
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Set<Urn> deps(final MessageNotice notice) {
            final Set<Urn> deps = new HashSet<Urn>();
            deps.add(notice.message().author().name());
            return deps;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void write(final MessageNotice notice,
            final DataOutputStream stream) throws IOException {
            stream.writeLong(notice.message().number());
            stream.writeUTF(notice.message().author().name().toString());
            new BigText(notice.message().text()).write(stream);
            stream.writeLong(notice.message().date().getTime());
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public MessageNotice read(final DataInputStream stream)
            throws IOException {
            final long number = stream.readLong();
            final Urn author = Urn.create(stream.readUTF());
            final String text = BigText.read(stream).toString();
            final Date date = new Date(stream.readLong());
            // @checkstyle AnonInnerLength (100 lines)
            return new MessageNotice() {
                @Override
                public Message message() {
                    return new Message() {
                        @Override
                        public int hashCode() {
                            return this.number().hashCode();
                        }
                        @Override
                        public boolean equals(final Object msg) {
                            return this == msg || (msg instanceof Message
                                && this.number()
                                    .equals(Message.class.cast(msg).number())
                                );
                        }
                        @Override
                        public Long number() {
                            return number;
                        }
                        @Override
                        public String text() {
                            return text;
                        }
                        @Override
                        public Identity author() {
                            return IdentityNotice.Serial.toIdentity(author);
                        }
                        @Override
                        public Boolean seen() {
                            throw new UnsupportedOperationException();
                        }
                        @Override
                        public Date date() {
                            return date;
                        }
                        @Override
                        public int compareTo(final Message msg) {
                            return this.number().compareTo(msg.number());
                        }
                    };
                }
            };
        }
    }

}
