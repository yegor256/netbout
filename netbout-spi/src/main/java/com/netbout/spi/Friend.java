/**
 * Copyright (c) 2009-2014, netbout.com
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
import java.net.URI;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * Friend.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 2.0
 */
@Immutable
public interface Friend {

    /**
     * Get its alias.
     * @return Alias of this identity
     */
    String alias();

    /**
     * URI of his photo.
     * @return URI
     */
    URI photo();

    /**
     * Matcher of its alias.
     */
    final class HasAlias extends BaseMatcher<Friend> {
        /**
         * Matcher of the alias.
         */
        private final transient Matcher<String> matcher;
        /**
         * Ctor.
         * @param mtchr Matcher of the alias
         */
        public HasAlias(final Matcher<String> mtchr) {
            super();
            this.matcher = mtchr;
        }
        @Override
        public boolean matches(final Object obj) {
            return this.matcher.matches(
                Friend.class.cast(obj).alias()
            );
        }
        @Override
        public void describeTo(final Description description) {
            description.appendText("friend with alias ");
            this.matcher.describeTo(description);
        }
    }

    /**
     * Matcher of its photo.
     */
    final class HasPhoto extends BaseMatcher<Friend> {
        /**
         * Matcher of the alias.
         */
        private final transient Matcher<URI> matcher;
        /**
         * Ctor.
         * @param mtchr Matcher of the alias
         */
        public HasPhoto(final Matcher<URI> mtchr) {
            super();
            this.matcher = mtchr;
        }
        @Override
        public boolean matches(final Object obj) {
            return this.matcher.matches(
                Friend.class.cast(obj).photo()
            );
        }
        @Override
        public void describeTo(final Description description) {
            description.appendText("friend with photo ");
            this.matcher.describeTo(description);
        }
    }

}
