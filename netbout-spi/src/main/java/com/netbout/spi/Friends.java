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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Bout friends talking.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.0
 */
@Immutable
public interface Friends {

    /**
     * Invite new friend.
     * @param friend Alias of the friend
     * @throws IOException If fails
     * @throws Friends.UnknownAliasException If alias doesn't exist
     * @checkstyle ThrowsCountCheck (2 lines)
     */
    void invite(String friend) throws IOException,
        Friends.UnknownAliasException;

    /**
     * Kick him off.
     * @param friend Alias of the friend
     * @throws Friends.UnknownAliasException If alias doesn't exist
     * @throws IOException If fails
     * @checkstyle ThrowsCountCheck (2 lines)
     */
    void kick(String friend) throws IOException, Friends.UnknownAliasException;

    /**
     * Iterate them all.
     * @return List of friends
     * @throws IOException If fails
     */
    Iterable<Friend> iterate() throws IOException;

    /**
     * Thowable when alias is unknown.
     */
    class UnknownAliasException extends IOException {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = 0x7526FA78EED21470L;
        /**
         * Public ctor.
         * @param cause Cause of the problem
         */
        public UnknownAliasException(final String cause) {
            super(cause);
        }
    }

    /**
     * Search of friends.
     */
    @Immutable
    @Loggable(Loggable.DEBUG)
    @ToString
    @EqualsAndHashCode(of = "origin")
    final class Search {
        /**
         * Origin friends.
         */
        private final transient Friends origin;
        /**
         * Ctor.
         * @param friends Origin friends
         */
        public Search(final Friends friends) {
            this.origin = friends;
        }
        /**
         * Friend with this alias exists?
         * @param alias Alias
         * @return TRUE if exists
         * @throws IOException If fails
         */
        public boolean exists(final String alias) throws IOException {
            return Iterables.any(
                this.origin.iterate(),
                new Predicate<Friend>() {
                    @Override
                    public boolean apply(final Friend friend) {
                        try {
                            return friend.alias().equals(alias);
                        } catch (final IOException ex) {
                            throw new IllegalStateException(ex);
                        }
                    }
                }
            );
        }
        /**
         * Get a friend with this alias (runtime exception if absent).
         * @param alias Alias
         * @return Friend found
         * @throws IOException If fails
         */
        public Friend find(final String alias) throws IOException {
            return Iterables.find(
                this.origin.iterate(),
                new Predicate<Friend>() {
                    @Override
                    public boolean apply(final Friend friend) {
                        try {
                            return friend.alias().equals(alias);
                        } catch (final IOException ex) {
                            throw new IllegalStateException(ex);
                        }
                    }
                }
            );
        }
    }

}
