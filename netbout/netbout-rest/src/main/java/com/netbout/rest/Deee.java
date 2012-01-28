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

import java.net.URLDecoder;
import org.apache.commons.lang.CharEncoding;

/**
 * This is a stupid decoder for incoming QueryParams, FormParams.
 *
 * <p>That's why this stupid name of the class. It has to be removed from
 * the project as soon as possible. It's just a temporary workaround.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @todo #158 There is a bug in Jersey, it doesn't decode incoming
 *  params automatically (http://java.net/jira/browse/JERSEY-739). That's
 *  why I should do it here manually, damn it. Remove this class at all
 *  once the problem is solved in Jersey, and upgrate its version.
 */
public final class Deee {

    /**
     * The decoded value.
     */
    private transient String decoded;

    /**
     * Public ctor, to be called by Jersey to instantiate this class.
     * @param value The value, not decoded
     */
    public Deee(final String value) {
        try {
            this.decoded = URLDecoder.decode(value, CharEncoding.UTF_8);
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Create its instance from a plain text (not encoded).
     * @param txt The text
     * @return The object
     */
    public static Deee plain(final Object txt) {
        final Deee deee = new Deee("");
        deee.decoded = txt.toString();
        return deee;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.txt();
    }

    /**
     * Get it's value.
     * @return The value
     */
    public String txt() {
        return this.decoded;
    }

}
