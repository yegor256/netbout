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
package com.netbout.inf.notices;

import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import com.netbout.spi.Urn;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Message was just seen.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public interface MessageSeenNotice extends MessageNotice, IdentityNotice {

    /**
     * Serializer.
     */
    class Serial implements Serializer<MessageSeenNotice> {
        /**
         * {@inheritDoc}
         */
        @Override
        public String nameOf(final MessageSeenNotice notice) {
            return String.format(
                "%s %s",
                new IdentityNotice.Serial().nameOf(notice),
                new MessageNotice.Serial().nameOf(notice)
            );
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Set<Urn> deps(final MessageSeenNotice notice) {
            final Set<Urn> deps = new HashSet<Urn>();
            deps.addAll(new IdentityNotice.Serial().deps(notice));
            deps.addAll(new MessageNotice.Serial().deps(notice));
            return deps;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void write(final MessageSeenNotice notice,
            final DataOutputStream stream) throws IOException {
            new MessageNotice.Serial().write(notice, stream);
            new IdentityNotice.Serial().write(notice, stream);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public MessageSeenNotice read(final DataInputStream stream)
            throws IOException {
            final MessageNotice mnotice =
                new MessageNotice.Serial().read(stream);
            final IdentityNotice inotice =
                new IdentityNotice.Serial().read(stream);
            return new MessageSeenNotice() {
                @Override
                public Message message() {
                    return mnotice.message();
                }
                @Override
                public Identity identity() {
                    return inotice.identity();
                }
            };
        }
    }

}
