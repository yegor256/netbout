/**
 * Copyright (c) 2009-2011, NetBout.com
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
package com.netbout.spi.client;

import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.ymock.util.Logger;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;

/**
 * Builder of URI from REST resources.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class RestUriBuilder {

    /**
     * It's a utility class.
     */
    private RestUriBuilder() {
        // empty
    }

    /**
     * Builds UriBuilder form provided bout.
     * @param bout The bout
     * @return The builder
     */
    public static UriBuilder from(final Bout bout) {
        if (!(bout instanceof RestBout)) {
            throw new IllegalArgumentException(
                Logger.format(
                    // @checkstyle LineLength (1 line)
                    "RestUriBuilder#from(Bout) accepts only bouts from RestSession, while '%[type]s' provided",
                    bout
                )
            );
        }
        final URI uri = ((RestBout) bout).uri();
        Logger.debug(
            RestUriBuilder.class,
            "#from(%[type]s): Bout URI '%s' found",
            bout,
            uri
        );
        return UriBuilder.fromUri(uri);
    }

    /**
     * Builds UriBuilder form provided identity.
     * @param identity The identity
     * @return The builder
     */
    public static UriBuilder from(final Identity identity) {
        if (!(identity instanceof RestIdentity)) {
            throw new IllegalArgumentException(
                Logger.format(
                    // @checkstyle LineLength (1 line)
                    "RestUriBuilder#from(Identity) accepts only identities from RestSession, while '%[type]s' provided",
                    identity
                )
            );
        }
        final URI uri = ((RestIdentity) identity).uri();
        Logger.debug(
            RestUriBuilder.class,
            "#from(%[type]s): Identity URI '%s' found",
            identity,
            uri
        );
        return UriBuilder.fromUri(uri);
    }

}
