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
import java.io.IOException;
import java.util.Date;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * Message.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.0
 */
@Immutable
public interface Message {

    /**
     * Get its unique number.
     * @return The number of the message
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
     * Get its text.
     * @return The text of the message
     * @throws IOException If fails
     */
    String text() throws IOException;

    /**
     * Author of it.
     * @return The author
     * @throws IOException If fails
     */
    String author() throws IOException;

    /**
     * Matcher of its text.
     */
    final class HasText extends BaseMatcher<Message> {
        /**
         * Matcher of the alias.
         */
        private final transient Matcher<String> matcher;
        /**
         * Ctor.
         * @param mtchr Matcher of the alias
         */
        public HasText(final Matcher<String> mtchr) {
            super();
            this.matcher = mtchr;
        }
        @Override
        public boolean matches(final Object obj) {
            try {
                return this.matcher.matches(
                    Message.class.cast(obj).text()
                );
            } catch (final IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        @Override
        public void describeTo(final Description description) {
            description.appendText("message with text ");
            this.matcher.describeTo(description);
        }
    }

    /**
     * Matcher of its author.
     */
    final class HasAuthor extends BaseMatcher<Message> {
        /**
         * Matcher of the alias.
         */
        private final transient Matcher<String> matcher;
        /**
         * Ctor.
         * @param mtchr Matcher of the alias
         */
        public HasAuthor(final Matcher<String> mtchr) {
            super();
            this.matcher = mtchr;
        }
        @Override
        public boolean matches(final Object obj) {
            try {
                return this.matcher.matches(
                    Message.class.cast(obj).text()
                );
            } catch (final IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        @Override
        public void describeTo(final Description description) {
            description.appendText("message with author ");
            this.matcher.describeTo(description);
        }
    }

}
