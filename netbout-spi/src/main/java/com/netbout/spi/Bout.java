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
package com.netbout.spi;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import java.io.IOException;
import java.util.Date;
import lombok.ToString;

/**
 * Bout.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.0
 */
@Immutable
@SuppressWarnings("PMD.TooManyMethods")
public interface Bout {

    /**
     * Get its unique number.
     * @return The number of the bout
     * @throws IOException If fails
     */
    long number() throws IOException;

    /**
     * When it was created.
     * @return The date of creation
     * @throws IOException If fails
     */
    Date date() throws IOException;

    /**
     * When it was updated.
     * @return The date of update
     * @throws IOException If fails
     */
    Date updated() throws IOException;

    /**
     * Get its title.
     * @return The title of the bout
     * @throws IOException If fails
     */
    String title() throws IOException;

    /**
     * Set its title.
     * @param text The title of the bout
     * @throws IOException If fails
     */
    void rename(String text) throws IOException;

    /**
     * Get subscription.
     * @return Subscription status
     * @throws IOException If fails
     */
    boolean subscription() throws IOException;

    /**
     * Get subscription by alias.
     * @param alias Alias to get subscription for
     * @return Subscription status
     * @throws IOException If fails
     */
    boolean subscription(String alias) throws IOException;

    /**
     * Set subscription.
     * @param subs The subscription type of the bout
     * @throws IOException If fails
     */
    void subscribe(boolean subs) throws IOException;

    /**
     * Get bout messages.
     * @return Messages
     * @throws IOException If fails
     */
    Messages messages() throws IOException;

    /**
     * Get friends.
     * @return Friends talking in this bout
     * @throws IOException If fails
     */
    Friends friends() throws IOException;

    /**
     * Attachments.
     * @return Attachments
     * @throws IOException If fails
     */
    Attachments attachments() throws IOException;

    /**
     * Read only.
     */
    @Immutable
    @Loggable(Loggable.DEBUG)
    @ToString
    final class ReadOnly implements Bout {
        /**
         * Original bout.
         */
        private final transient Bout origin;
        /**
         * Ctor.
         * @param bout Bout
         */
        public ReadOnly(final Bout bout) {
            this.origin = bout;
        }
        @Override
        public boolean equals(final Object obj) {
            return this.origin.equals(obj);
        }
        @Override
        public int hashCode() {
            return this.origin.hashCode();
        }
        @Override
        public long number() throws IOException {
            return this.origin.number();
        }
        @Override
        public Date date() throws IOException {
            return this.origin.date();
        }
        @Override
        public Date updated() throws IOException {
            return this.origin.updated();
        }
        @Override
        public String title() throws IOException {
            return this.origin.title();
        }
        @Override
        public void rename(final String text) {
            throw new UnsupportedOperationException("#rename()");
        }
        @Override
        public void subscribe(final boolean subs) throws IOException {
            throw new UnsupportedOperationException("#subscribe()");
        }
        @Override
        public Messages messages() throws IOException {
            return this.origin.messages();
        }
        @Override
        public Friends friends() throws IOException {
            return this.origin.friends();
        }
        @Override
        public Attachments attachments() throws IOException {
            return this.origin.attachments();
        }
        @Override
        public boolean subscription() throws IOException {
            return this.origin.subscription();
        }
        @Override
        public boolean subscription(final String alias) throws IOException {
            return this.origin.subscription(alias);
        }
    }

}
