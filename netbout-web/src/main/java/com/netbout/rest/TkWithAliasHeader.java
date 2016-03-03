/**
 * Copyright (c) 2009-2016, netbout.com
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
package com.netbout.rest;

import com.jcabi.urn.URN;
import com.netbout.spi.Base;
import java.io.IOException;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsWithHeader;

/**
 * Adds alias to header.
 *
 * @author Kujtim Hoxha (kujtimii.j@gail.com)
 * @version $Id$
 * @since 2.22.11
 */
public class TkWithAliasHeader implements Take {
    /**
     * Base.
     */
    private final Base base;
    /**
     * Take.
     */
    private final Take take;
    /**
     * Header.
     */
    private final String header;

    /**
     * Ctr.
     * @param base Base.
     * @param take Take.
     */
    public TkWithAliasHeader(final Base base, final Take take) {
        this.base = base;
        this.take = take;
        this.header =
            String.format("%s: %s", "X-Netbout-Alias", "%s");
    }

    @Override
    public final Response act(final Request req)
        throws IOException {
        final Iterable<String> hdit = req.head();
        String aliashd = "";
        for (final String hdr:hdit) {
            if (hdr.contains("TkAuth")) {
                aliashd = this.base.user(
                    URN.create(hdr.replace("TkAuth: ", "")
                        .replaceAll("%3A", ":")
                    )
                ).aliases().iterate().iterator().next().name();
            }
        }
        return new RsWithHeader(
            this.take.act(req), String.format(this.header, aliashd)
        );
    }
}
