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
package com.netbout.spi;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;

/**
 * Universal resource locator (URN).
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @see <a href="http://tools.ietf.org/html/rfc2141">RFC2141</a>
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class Urn implements Comparable {

    /**
     * Marker of an empty URN.
     */
    private static final String EMPTY = "void";

    /**
     * The prefix.
     */
    private static final String PREFIX = "urn";

    /**
     * The separator.
     */
    private static final String SEP = ":";

    /**
     * Validating regular expr.
     */
    private static final String REGEX =
        "^urn:[a-z]{1,31}:([\\w()+,\\-.:=@;$_!*']|%[0-9a-fA-F]{2})*$";

    /**
     * The URI.
     */
    private final transient URI uri;

    /**
     * Public ctor, for JAXB mostly.
     */
    public Urn() {
        this(Urn.EMPTY, "");
    }

    /**
     * Public ctor.
     * @param text The text of the URN
     * @throws URISyntaxException If syntax is not correct
     */
    public Urn(final String text) throws URISyntaxException {
        if (text == null) {
            throw new IllegalArgumentException("Text can't be NULL");
        }
        if (!text.matches(this.REGEX)) {
            throw new URISyntaxException(text, "Invalid format of URN");
        }
        this.uri = new URI(text);
        this.validate();
    }

    /**
     * Public ctor.
     * @param nid The namespace ID
     * @param nss The namespace specific string
     */
    public Urn(final String nid, final String nss) {
        if (nid == null) {
            throw new IllegalArgumentException("NID can't be NULL");
        }
        if (nss == null) {
            throw new IllegalArgumentException("NSS can't be NULL");
        }
        try {
            this.uri = URI.create(
                String.format(
                    "%s%s%s%2$s%s",
                    this.PREFIX,
                    this.SEP,
                    nid,
                    URLEncoder.encode(nss, CharEncoding.UTF_8)
                )
            );
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
        try {
            this.validate();
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Static ctor.
     * @param text The text of the URN
     * @return The URN
     */
    public static Urn create(final String text) {
        try {
            return new Urn(text);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.uri.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Object obj) {
        return this.uri.compareTo(((Urn) obj).uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        boolean equals = false;
        if (obj instanceof Urn) {
            equals = this.uri.equals(((Urn) obj).uri);
        } else if (obj instanceof String) {
            equals = this.uri.toString().equals((String) obj);
        } else if (obj instanceof URI) {
            equals = this.uri.equals((URI) obj);
        }
        return equals;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.uri.hashCode();
    }

    /**
     * Is it URN?
     * @param text The text to validate
     * @return Yes of no
     */
    public static boolean isValid(final String text) {
        boolean valid = true;
        try {
            new Urn(text);
        } catch (URISyntaxException ex) {
            valid = false;
        }
        return valid;
    }

    /**
     * Does it match the pattern?
     * @param pattern The pattern to match
     * @return Yes of no
     */
    public boolean matches(final Urn pattern) {
        boolean matches = false;
        if (this.equals(pattern)) {
            matches = true;
        } else if (pattern.toString().endsWith("*")) {
            final String body = pattern.toString().substring(
                0,  pattern.toString().length() - 1
            );
            matches = this.uri.toString().startsWith(body);
        }
        return matches;
    }

    /**
     * Does it match the pattern?
     * @param pattern The pattern to match
     * @return Yes of no
     */
    public boolean matches(final String pattern) {
        return this.matches(Urn.create(pattern));
    }

    /**
     * Is it empty?
     * @return Yes of no
     */
    public boolean isEmpty() {
        return this.EMPTY.equals(this.nid());
    }

    /**
     * Convert it to URI.
     * @return The URI
     */
    public URI toURI() {
        return URI.create(this.uri.toString());
    }

    /**
     * Get namespace ID.
     * @return Namespace ID
     */
    public String nid() {
        return this.segment(1);
    }

    /**
     * Get namespace specific string.
     * @return Namespace specific string
     */
    public String nss() {
        try {
            return URLDecoder.decode(this.segment(2), CharEncoding.UTF_8);
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Get segment by position.
     * @param pos Its position
     * @return The segment
     */
    private String segment(final int pos) {
        return StringUtils.splitPreserveAllTokens(
            this.uri.toString(),
            this.SEP,
            // @checkstyle MagicNumber (1 line)
            3
        )[pos];
    }

    /**
     * Validate URN.
     * @throws URISyntaxException If it's not valid
     */
    private void validate() throws URISyntaxException {
        if (this.isEmpty() && !this.nss().isEmpty()) {
            throw new URISyntaxException(
                this.toString(),
                "Empty URN can't have NSS"
            );
        }
        if (!this.nid().matches("^[a-z]{1,31}$")) {
            throw new IllegalArgumentException(
                String.format(
                    "NID '%s' can contain up to 31 low case letters",
                    this.nid()
                )
            );
        }
    }

}
