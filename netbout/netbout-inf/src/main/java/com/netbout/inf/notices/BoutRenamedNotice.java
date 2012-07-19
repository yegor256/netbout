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

import com.netbout.spi.Bout;
import com.netbout.spi.Urn;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;

/**
 * Bout was just renamed.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public interface BoutRenamedNotice extends BoutNotice {

    /**
     * Serializer.
     */
    class Serial implements Serializer<BoutRenamedNotice> {
        /**
         * {@inheritDoc}
         */
        @Override
        public String nameOf(final BoutRenamedNotice notice) {
            return String.format(
                "%s title:%s",
                new BoutNotice.Serial().nameOf(notice),
                notice.bout().title()
            );
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Set<Urn> deps(final BoutRenamedNotice notice) {
            return new BoutNotice.Serial().deps(notice);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void write(final BoutRenamedNotice notice,
            final DataOutputStream stream) throws IOException {
            new BoutNotice.Serial().write(notice, stream);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public BoutRenamedNotice read(final DataInputStream stream)
            throws IOException {
            final BoutNotice bnotice = new BoutNotice.Serial().read(stream);
            return new BoutRenamedNotice() {
                @Override
                public Bout bout() {
                    return bnotice.bout();
                }
            };
        }
    }

}
