/**
 * Copyright (c) 2009-2012, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the NetBout.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netbout.spi;

import java.net.URL;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Profile of identity.
 *
 * <p>Instances of this interface are thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public interface Profile {

    /**
     * Language.
     * @return The language we speak with this identity
     */
    Locale locale();

    /**
     * Get a photo of this identity.
     * @return The URL of the photo
     */
    URL photo();

    /**
     * Get all aliases.
     * @return List of all aliases
     */
    Set<String> aliases();

    /**
     * Profile with implemented conventions.
     *
     * <p>To avoid runtime exceptions and unexpected situations you're
     * encouraged to use this class every time you're accessing a profile
     * of an identity.
     */
    class Conventional implements Profile {
        /**
         * Original identity.
         */
        private final transient Friend origin;
        /**
         * Public ctor.
         * @param friend Original identity
         */
        public Conventional(final Friend friend) {
            if (friend.profile() == null) {
                throw new IllegalStateException(
                    String.format(
                        "Profile is NULL in '%s' (%s)",
                        friend,
                        friend.name()
                    )
                );
            }
            this.origin = friend;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Locale locale() {
            Locale locale = this.origin.profile().locale();
            if (locale == null) {
                locale = Locale.ENGLISH;
            }
            return locale;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public URL photo() {
            URL photo = this.origin.profile().photo();
            if (photo == null) {
                try {
                    photo = new URL("http://cdn.netbout.com/unknown.png");
                } catch (java.net.MalformedURLException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            return photo;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Set<String> aliases() {
            final Set<String> aliases = new HashSet<String>(
                this.origin.profile().aliases()
            );
            if (aliases == null) {
                throw new IllegalStateException(
                    String.format(
                        "Set of aliases in the profile is NULL for '%s' (%s)",
                        this.origin,
                        this.origin.name()
                    )
                );
            }
            if (aliases.isEmpty()) {
                aliases.add(this.origin.name().toString());
            }
            return aliases;
        }
    }

}
