/**
 * Copyright (c) 2009-2011, netBout.com
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
package com.netbout.rest;

import com.rexsl.core.Manifests;
import java.net.URI;
import java.util.Date;
import java.util.Locale;
import javax.ws.rs.core.NewCookie;

/**
 * Cookie builder.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 * @todo #254 Somehow we should specify PORT in the cookie. Without this param
 *  the site doesn't work in localhost:9099 in Chrome. Works fine in Safari,
 *  but not in Chrome. see http://stackoverflow.com/questions/1612177
 */
public final class CookieBuilder {

    /**
     * Name of cookie.
     */
    private transient String name = "netbout";

    /**
     * Value.
     */
    private transient String value = "";

    /**
     * Domain.
     */
    private final transient String domain;

    /**
     * Port.
     */
    private transient Integer port;

    /**
     * Path.
     */
    private transient String path;

    /**
     * When it should expire.
     */
    private transient Date expires = new Date(0);

    /**
     * Comment to set.
     */
    private transient String comment = "";

    /**
     * Public ctor.
     * @param uri The URI
     */
    public CookieBuilder(final URI uri) {
        this.domain = uri.getHost();
        this.port = uri.getPort();
        if (this.port <= 0) {
            // @checkstyle MagicNumber (1 line)
            this.port = 80;
        }
        this.path = uri.getPath();
    }

    /**
     * Named like this.
     * @param txt The name
     * @return This object
     */
    public CookieBuilder named(final String txt) {
        this.name = txt;
        return this;
    }

    /**
     * With value like this.
     * @param txt The value
     * @return This object
     */
    public CookieBuilder valued(final String txt) {
        this.value = txt;
        return this;
    }

    /**
     * Set path.
     * @param txt The path
     * @return This object
     */
    public CookieBuilder pathed(final String txt) {
        this.path = txt;
        return this;
    }

    /**
     * Make this cookie temporary, with certain pre-defined age.
     * @return This object
     */
    public CookieBuilder temporary() {
        this.expires = new Date(
            // @checkstyle MagicNumber (1 line)
            new Date().getTime() + 90 * 24 * 60 * 60 * 1000L
        );
        return this;
    }

    /**
     * Commented as this.
     * @param txt The comment
     * @return This object
     */
    public CookieBuilder commented(final String txt) {
        this.comment = txt;
        return this;
    }

    /**
     * Build cookie as string.
     * @return The cookie string to be used in "Set-cookie" header.
     */
    public NewCookie build() {
        return new NetboutCookie();
    }

    /**
     * The cookie.
     */
    private final class NetboutCookie extends NewCookie {
        /**
         * Public ctor.
         */
        public NetboutCookie() {
            super(CookieBuilder.this.name, CookieBuilder.this.value);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return String.format(
                Locale.ENGLISH,
                // @checkstyle LineLength (1 line)
                "%s=%s;Domain=%s;Port=%d;Path=%s;Expires=%ta, %6$td-%6$tb-%6$tY %6$tT GMT;Version=%d;Comment=\"%s\"",
                this.getName(),
                this.getValue(),
                CookieBuilder.this.domain,
                CookieBuilder.this.port,
                CookieBuilder.this.path,
                CookieBuilder.this.expires,
                Integer.valueOf(Manifests.read("Netbout-Revision")),
                CookieBuilder.this.comment.replace("\"", "\\\"")
            );
        }
    }

}
