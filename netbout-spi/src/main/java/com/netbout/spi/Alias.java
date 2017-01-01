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

import com.jcabi.aspects.Immutable;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * Alias.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.0
 */
@Immutable
@SuppressWarnings("PMD.TooManyMethods")
public interface Alias {

    /**
     * Anonymous photo.
     */
    URI BLANK = URI.create("http://img.netbout.com/unknown.png");

    /**
     * Get its name.
     * @return Name of the alias
     * @throws IOException If fails
     */
    String name() throws IOException;

    /**
     * URI of his photo.
     * @return URI
     * @throws IOException If fails
     */
    URI photo() throws IOException;

    /**
     * Get its locale.
     * @return Locale of the alias
     * @throws IOException If fails
     */
    Locale locale() throws IOException;

    /**
     * Set photo.
     * @param uri URI of photo
     * @throws IOException If fails
     */
    void photo(URI uri) throws IOException;

    /**
     * Get email.
     * @return Email
     * @throws IOException If fails
     */
    String email() throws IOException;

    /**
     * Save email.
     * @param email Email to save
     * @throws IOException If fails
     */
    void email(String email) throws IOException;

    /**
     * Save email and invite to join on Netbout by email.
     * @param email Email
     * @param urn Urn
     * @param bout Bout
     * @throws IOException if fails
     */
    void email(String email, String urn, Bout bout) throws IOException;

    /**
     * Save email and send verification link.
     * @param email Email to save
     * @param link Verification link
     * @throws IOException If fails
     */
    void email(String email, String link) throws IOException;

    /**
     * Get inbox.
     * @return Inbox
     * @throws IOException If fails
     */
    Inbox inbox() throws IOException;

    /**
     * Matcher of its name.
     */
    final class HasName extends BaseMatcher<Alias> {
        /**
         * Matcher of the alias.
         */
        private final transient Matcher<String> matcher;
        /**
         * Ctor.
         * @param mtchr Matcher of the alias
         */
        public HasName(final Matcher<String> mtchr) {
            super();
            this.matcher = mtchr;
        }
        @Override
        public boolean matches(final Object obj) {
            try {
                return this.matcher.matches(
                    Alias.class.cast(obj).name()
                );
            } catch (final IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        @Override
        public void describeTo(final Description description) {
            description.appendText("alias with name ");
            this.matcher.describeTo(description);
        }
    }

    /**
     * Matcher of its photo.
     */
    final class HasPhoto extends BaseMatcher<Alias> {
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
            try {
                return this.matcher.matches(
                    Alias.class.cast(obj).photo()
                );
            } catch (final IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        @Override
        public void describeTo(final Description description) {
            description.appendText("alias with photo ");
            this.matcher.describeTo(description);
        }
    }

    /**
     * Thowable when email is wrong.
     * @see Alias#email(String)
     */
    class InvalidEmailException extends IOException {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = 0x7529FA78EED21470L;
        /**
         * Public ctor.
         * @param email The number of bout not found
         */
        public InvalidEmailException(final String email) {
            super(String.format("Email '%s' is wrong", email));
        }
    }

}
