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
 * this code occasionally and without intent to use it, please report this
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
package com.netbout.inf;

import com.jcabi.log.Logger;
import com.netbout.inf.notices.AliasAddedNotice;
import com.netbout.inf.notices.BoutNotice;
import com.netbout.inf.notices.BoutRenamedNotice;
import com.netbout.inf.notices.IdentityNotice;
import com.netbout.inf.notices.JoinNotice;
import com.netbout.inf.notices.KickOffNotice;
import com.netbout.inf.notices.MessageNotice;
import com.netbout.inf.notices.MessagePostedNotice;
import com.netbout.inf.notices.MessageSeenNotice;
import com.netbout.spi.Bout;
import com.netbout.spi.Participant;
import com.netbout.spi.Urn;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Notice for infinity.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public interface Notice {

    /**
     * Serializable notice.
     */
    class SerializableNotice {
        /**
         * Original notice.
         */
        private final transient Notice origin;
        /**
         * Public ctor.
         * @param notice Original notice
         */
        public SerializableNotice(final Notice notice) {
            this.origin = notice;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            String txt;
            if (this.origin instanceof MessagePostedNotice) {
                txt = new MessagePostedNotice.Serial().nameOf(
                    MessagePostedNotice.class.cast(this.origin)
                );
            } else if (this.origin instanceof MessageSeenNotice) {
                txt = new MessageSeenNotice.Serial().nameOf(
                    MessageSeenNotice.class.cast(this.origin)
                );
            } else if (this.origin instanceof AliasAddedNotice) {
                txt = new AliasAddedNotice.Serial().nameOf(
                    AliasAddedNotice.class.cast(this.origin)
                );
            } else if (this.origin instanceof BoutRenamedNotice) {
                txt = new BoutRenamedNotice.Serial().nameOf(
                    BoutRenamedNotice.class.cast(this.origin)
                );
            } else if (this.origin instanceof KickOffNotice) {
                txt = new KickOffNotice.Serial().nameOf(
                    KickOffNotice.class.cast(this.origin)
                );
            } else if (this.origin instanceof JoinNotice) {
                txt = new JoinNotice.Serial().nameOf(
                    JoinNotice.class.cast(this.origin)
                );
            } else {
                throw new IllegalStateException(
                    Logger.format(
                        "unknown type '%[type]s' stored",
                        this.origin
                    )
                );
            }
            return new StringBuilder().append(this.nameOf())
                .append(" ")
                .append(txt)
                .toString();
        }
        /**
         * Dependencies of this notice.
         * @return Set of names
         */
        public Set<Urn> deps() {
            Set<Urn> deps;
            if (this.origin instanceof MessagePostedNotice) {
                deps = new MessagePostedNotice.Serial().deps(
                    MessagePostedNotice.class.cast(this.origin)
                );
            } else if (this.origin instanceof MessageSeenNotice) {
                deps = new MessageSeenNotice.Serial().deps(
                    MessageSeenNotice.class.cast(this.origin)
                );
            } else if (this.origin instanceof AliasAddedNotice) {
                deps = new AliasAddedNotice.Serial().deps(
                    AliasAddedNotice.class.cast(this.origin)
                );
            } else if (this.origin instanceof BoutRenamedNotice) {
                deps = new BoutRenamedNotice.Serial().deps(
                    BoutRenamedNotice.class.cast(this.origin)
                );
            } else if (this.origin instanceof KickOffNotice) {
                deps = new KickOffNotice.Serial().deps(
                    KickOffNotice.class.cast(this.origin)
                );
            } else if (this.origin instanceof JoinNotice) {
                deps = new JoinNotice.Serial().deps(
                    JoinNotice.class.cast(this.origin)
                );
            } else {
                throw new IllegalStateException(
                    Logger.format(
                        "unknown type '%[type]s' for deps searching",
                        this.origin
                    )
                );
            }
            if (deps.isEmpty()) {
                throw new IllegalArgumentException(
                    Logger.format(
                        "empty list of deps in %[type]s",
                        this.origin
                    )
                );
            }
            return deps;
        }
        /**
         * Convert it to bytearray.
         * @return The array
         * @throws IOException If some error
         */
        public byte[] serialize() throws IOException {
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            final DataOutputStream data = new DataOutputStream(stream);
            data.writeUTF(this.nameOf());
            if (this.origin instanceof MessagePostedNotice) {
                new MessagePostedNotice.Serial().write(
                    MessagePostedNotice.class.cast(this.origin),
                    data
                );
            } else if (this.origin instanceof MessageSeenNotice) {
                new MessageSeenNotice.Serial().write(
                    MessageSeenNotice.class.cast(this.origin),
                    data
                );
            } else if (this.origin instanceof AliasAddedNotice) {
                new AliasAddedNotice.Serial().write(
                    AliasAddedNotice.class.cast(this.origin),
                    data
                );
            } else if (this.origin instanceof BoutRenamedNotice) {
                new BoutRenamedNotice.Serial().write(
                    BoutRenamedNotice.class.cast(this.origin),
                    data
                );
            } else if (this.origin instanceof KickOffNotice) {
                new KickOffNotice.Serial().write(
                    KickOffNotice.class.cast(this.origin),
                    data
                );
            } else if (this.origin instanceof JoinNotice) {
                new JoinNotice.Serial().write(
                    JoinNotice.class.cast(this.origin),
                    data
                );
            } else {
                throw new IllegalStateException(
                    Logger.format(
                        "unknown type '%[type]s' serialized",
                        this.origin
                    )
                );
            }
            data.flush();
            return stream.toByteArray();
        }
        /**
         * Convert bytearray to Notice.
         * @param bytes The data
         * @return The notice
         * @throws IOException If some IO error
         */
        public static Notice deserialize(final byte[] bytes)
            throws IOException {
            final DataInputStream data = new DataInputStream(
                new ByteArrayInputStream(bytes)
            );
            final Class<? extends Notice> type =
                Notice.SerializableNotice.reverse(data.readUTF());
            Notice notice;
            if (type.equals(MessagePostedNotice.class)) {
                notice = new MessagePostedNotice.Serial().read(data);
            } else if (type.equals(MessageSeenNotice.class)) {
                notice = new MessageSeenNotice.Serial().read(data);
            } else if (type.equals(AliasAddedNotice.class)) {
                notice = new AliasAddedNotice.Serial().read(data);
            } else if (type.equals(BoutRenamedNotice.class)) {
                notice = new BoutRenamedNotice.Serial().read(data);
            } else if (type.equals(KickOffNotice.class)) {
                notice = new KickOffNotice.Serial().read(data);
            } else if (type.equals(JoinNotice.class)) {
                notice = new JoinNotice.Serial().read(data);
            } else {
                throw new IllegalStateException(
                    Logger.format(
                        "unknown type '%s' deserialized",
                        type.getName()
                    )
                );
            }
            return notice;
        }
        /**
         * Get name of notice.
         * @return The name
         */
        private String nameOf() {
            String name;
            if (this.origin instanceof MessagePostedNotice) {
                name = "message posted";
            } else if (this.origin instanceof MessageSeenNotice) {
                name = "message seen";
            } else if (this.origin instanceof AliasAddedNotice) {
                name = "alias added";
            } else if (this.origin instanceof BoutRenamedNotice) {
                name = "bout renamed";
            } else if (this.origin instanceof KickOffNotice) {
                name = "kicked off";
            } else if (this.origin instanceof JoinNotice) {
                name = "participation confirmed";
            } else {
                throw new IllegalStateException("unknown type of notice");
            }
            return name;
        }
        /**
         * Reverse name to notice type.
         * @param name The name
         * @return The type
         */
        private static Class<? extends Notice> reverse(final String name) {
            Class<? extends Notice> type;
            if ("message posted".equals(name)) {
                type = MessagePostedNotice.class;
            } else if ("message seen".equals(name)) {
                type = MessageSeenNotice.class;
            } else if ("alias added".equals(name)) {
                type = AliasAddedNotice.class;
            } else if ("bout renamed".equals(name)) {
                type = BoutRenamedNotice.class;
            } else if ("kicked off".equals(name)) {
                type = KickOffNotice.class;
            } else if ("participation confirmed".equals(name)) {
                type = JoinNotice.class;
            } else {
                throw new IllegalStateException("unknown name of notice");
            }
            return type;
        }
    }

}
