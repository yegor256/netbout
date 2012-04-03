/**
 * Copyright (c) 2009-2012, Netbout.com
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
package com.netbout.rest.jet;

import java.net.URI;
import javax.ws.rs.core.Response;

/**
 * Build JAX-RX response builder.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class JetBuilder {

    /**
     * The URI of the source.
     */
    private final transient URI source;

    /**
     * Public ctor.
     * @param src The source
     */
    public JetBuilder(final String src) {
        this.source = URI.create(src);
    }

    /**
     * Build the streaming output.
     * @return The output
     */
    public Response build() {
        final String scheme = this.source.getScheme();
        Jet jet;
        if ("http".equals(scheme)) {
            jet = new HttpJet();
        } else if ("s3".equals(scheme)) {
            jet = new S3Jet();
        } else {
            throw new IllegalArgumentException(
                String.format("unknown scheme '%s'", scheme)
            );
        }
        try {
            return jet.build(this.source);
        } catch (java.io.IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

}
