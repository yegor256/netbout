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

import com.netbout.spi.Identity;
import com.netbout.spi.Urn;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;

/**
 * New alias was added to identity.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public interface AliasAddedNotice extends IdentityNotice {

    /**
     * The identity where alias was added.
     * @return The identity
     */
    Identity identity();

    /**
     * The alias.
     * @return The alias
     */
    String alias();

    /**
     * Serializer.
     */
    class Serial implements Serializer<AliasAddedNotice> {
        /**
         * {@inheritDoc}
         */
        @Override
        public String nameOf(final AliasAddedNotice notice) {
            return String.format(
                "%s alias:%s",
                new IdentityNotice.Serial().nameOf(notice),
                notice.alias()
            );
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Set<Urn> deps(final AliasAddedNotice notice) {
            return new IdentityNotice.Serial().deps(notice);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void write(final AliasAddedNotice notice,
            final DataOutputStream stream) throws IOException {
            new IdentityNotice.Serial().write(notice, stream);
            stream.writeUTF(notice.alias());
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public AliasAddedNotice read(final DataInputStream stream)
            throws IOException {
            final IdentityNotice inotice =
                new IdentityNotice.Serial().read(stream);
            final String alias = stream.readUTF();
            return new AliasAddedNotice() {
                @Override
                public Identity identity() {
                    return inotice.identity();
                }
                @Override
                public String alias() {
                    return alias;
                }
            };
        }
    }

}
