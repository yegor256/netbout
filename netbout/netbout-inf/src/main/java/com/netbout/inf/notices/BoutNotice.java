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

import com.jcabi.urn.URN;
import com.netbout.inf.Notice;
import com.netbout.spi.Bout;
import com.netbout.spi.Friend;
import com.netbout.spi.Message;
import com.netbout.spi.Participant;
import com.netbout.spi.Profile;
import com.netbout.spi.Query;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Bout-related notice.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
public interface BoutNotice extends Notice {

    /**
     * Link to the bout.
     * @return The bout
     */
    Bout bout();

    /**
     * Serializer.
     */
    class Serial implements Serializer<BoutNotice> {
        /**
         * {@inheritDoc}
         */
        @Override
        public String nameOf(final BoutNotice notice) {
            return String.format(
                "bout:%d",
                notice.bout().number()
            );
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Set<URN> deps(final BoutNotice notice) {
            final Set<URN> deps = new HashSet<URN>();
            for (Participant dude : notice.bout().participants()) {
                deps.add(dude.name());
            }
            if (deps.isEmpty()) {
                throw new IllegalArgumentException(
                    String.format(
                        "empty list of participants in %s",
                        notice.bout()
                    )
                );
            }
            return deps;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void write(final BoutNotice notice,
            final DataOutputStream stream) throws IOException {
            stream.writeLong(notice.bout().number());
            stream.writeLong(notice.bout().date().getTime());
            stream.writeUTF(notice.bout().title());
            stream.writeInt(notice.bout().participants().size());
            for (Participant dude : notice.bout().participants()) {
                stream.writeUTF(dude.name().toString());
                stream.writeBoolean(dude.leader());
                stream.writeBoolean(dude.confirmed());
            }
        }
        /**
         * {@inheritDoc}
         * @checkstyle JavaNCSS (200 lines)
         */
        @Override
        @SuppressWarnings("PMD.ExcessiveMethodLength")
        public BoutNotice read(final DataInputStream stream)
            throws IOException {
            final long number = stream.readLong();
            final Date date = new Date(stream.readLong());
            final String title = stream.readUTF();
            final int total = stream.readInt();
            final Set<Participant> dudes = new HashSet<Participant>();
            final AtomicReference<Bout> bout = new AtomicReference<Bout>();
            for (int num = 0; num < total; ++num) {
                final URN name = URN.create(stream.readUTF());
                final boolean leader = stream.readBoolean();
                final boolean confirmed = stream.readBoolean();
                dudes.add(
                    // @checkstyle AnonInnerLength (50 lines)
                    new Participant() {
                        @Override
                        public URN name() {
                            return name;
                        }
                        @Override
                        public boolean leader() {
                            return leader;
                        }
                        @Override
                        public boolean confirmed() {
                            return confirmed;
                        }
                        @Override
                        public int compareTo(final Friend friend) {
                            throw new UnsupportedOperationException();
                        }
                        @Override
                        public void kickOff() {
                            throw new UnsupportedOperationException();
                        }
                        @Override
                        public void consign() {
                            throw new UnsupportedOperationException();
                        }
                        @Override
                        public Profile profile() {
                            throw new UnsupportedOperationException();
                        }
                    }
                );
            }
            bout.set(
                // @checkstyle AnonInnerLength (100 lines)
                new Bout() {
                    @Override
                    public int hashCode() {
                        return this.number().hashCode();
                    }
                    @Override
                    public boolean equals(final Object bout) {
                        return this == bout || (bout instanceof Bout
                            && this.number()
                                .equals(Bout.class.cast(bout).number())
                            );
                    }
                    @Override
                    public Long number() {
                        return number;
                    }
                    @Override
                    public Date date() {
                        return date;
                    }
                    @Override
                    public String title() {
                        return title;
                    }
                    @Override
                    public Collection<Participant> participants() {
                        return dudes;
                    }
                    @Override
                    public int compareTo(final Bout bout) {
                        return this.number().compareTo(bout.number());
                    }
                    @Override
                    public Message post(final String msg) {
                        throw new UnsupportedOperationException();
                    }
                    @Override
                    public void rename(final String txt) {
                        throw new UnsupportedOperationException();
                    }
                    @Override
                    public Message message(final Long number) {
                        throw new UnsupportedOperationException();
                    }
                    @Override
                    public Iterable<Message> messages(final Query query) {
                        throw new UnsupportedOperationException();
                    }
                    @Override
                    public Participant invite(final Friend friend) {
                        throw new UnsupportedOperationException();
                    }
                    @Override
                    public void leave() {
                        throw new UnsupportedOperationException();
                    }
                    @Override
                    public void confirm() {
                        throw new UnsupportedOperationException();
                    }
                }
            );
            return new BoutNotice() {
                @Override
                public Bout bout() {
                    return bout.get();
                }
            };
        }
    }

}
