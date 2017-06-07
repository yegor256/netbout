/**
 * Copyright (c) 2009-2017, netbout.com
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
 * Attachments.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 2.0
 */
@Immutable
public interface Attachments {

    /**
     * How many unseen attachments are there?
     * @return Number of them
     * @throws IOException If fails
     */
    int unseen() throws IOException;

    /**
     * Create attachment.
     * @param name Attachment name
     * @throws IOException If fails
     */
    void create(String name) throws IOException;

    /**
     * Delete attachment.
     * @param name Attachment name
     * @throws IOException If fails
     */
    void delete(String name) throws IOException;

    /**
     * Get attachment by name.
     * @param name Attachment name
     * @return Attachment
     * @throws IOException If fails
     */
    Attachment get(String name) throws IOException;

    /**
     * Iterate them all.
     * @return All of them
     * @throws IOException If fails
     */
    Iterable<Attachment> iterate() throws IOException;

    /**
     * You already have too many attachments.
     */
    final class TooManyException extends IOException {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = -6379382683897037014L;
        /**
         * Ctor.
         * @param cause Cause of the problem
         */
        public TooManyException(final String cause) {
            super(cause);
        }
    }

    /**
     * Attachment not found.
     */
    final class NotFoundException extends IOException {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = -6379382689897037014L;
        /**
         * Ctor.
         * @param cause Cause of the problem
         */
        public NotFoundException(final String cause) {
            super(cause);
        }
    }

    /**
     * Invalid attachment name.
     */
    final class InvalidNameException extends IOException {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = -6379342683897037014L;
        /**
         * Ctor.
         * @param cause Cause of the problem
         */
        public InvalidNameException(final String cause) {
            super(cause);
        }
    }

    /**
     * Search.
     */
    @Immutable
    @Loggable(Loggable.DEBUG)
    @ToString(of = "attachments")
    @EqualsAndHashCode(of = "attachments")
    final class Search {
        /**
         * Origin.
         */
        private final transient Attachments attachments;
        /**
         * Ctor.
         * @param att Attachments
         */
        public Search(final Attachments att) {
            this.attachments = att;
        }
        /**
         * Exists with this name?
         * @param name Name of it
         * @return TRUE if exists
         * @throws IOException If fails
         */
        public boolean exists(final String name) throws IOException {
            return Iterables.any(
                this.attachments.iterate(),
                new Predicate<Attachment>() {
                    @Override
                    public boolean apply(final Attachment input) {
                        try {
                            return input.name().equals(name);
                        } catch (final IOException ex) {
                            throw new IllegalStateException(ex);
                        }
                    }
                }
            );
        }
    }

}
