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

import com.jcabi.manifests.Manifests;
import com.netbout.spi.Base;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import org.takes.Request;
import org.takes.facets.auth.XeIdentity;
import org.takes.facets.auth.XeLogoutLink;
import org.takes.facets.auth.social.XeFacebookLink;
import org.takes.facets.auth.social.XeGithubLink;
import org.takes.facets.auth.social.XeGoogleLink;
import org.takes.facets.flash.XeFlash;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeChain;
import org.takes.rs.xe.XeDate;
import org.takes.rs.xe.XeLinkHome;
import org.takes.rs.xe.XeLinkSelf;
import org.takes.rs.xe.XeLocalhost;
import org.takes.rs.xe.XeMillis;
import org.takes.rs.xe.XeSLA;
import org.takes.rs.xe.XeSource;
import org.takes.rs.xe.XeWrap;

/**
 * Index resource, front page of the website.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.14
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@EqualsAndHashCode(callSuper = true)
public final class XePage extends XeWrap {

    /**
     * Ctor.
     * @param base Base
     * @param req Request
     * @param src Source
     * @throws IOException If fails
     */
    public XePage(final Base base,
        final Request req, final XeSource... src) throws IOException {
        super(XePage.make(base, req, src));
    }

    /**
     * Make it.
     * @param base Base
     * @param req Request
     * @param src Source
     * @return Response
     * @throws IOException If fails
     */
    private static XeSource make(final Base base,
        final Request req, final XeSource... src) throws IOException {
        return new XeAppend(
            "page",
            new XeAppend(
                "documentation",
                Manifests.read("Netbout-Documentation")
            ),
            new XeMillis(false),
            new XeChain(src),
            new XeDate(),
            new XeSLA(),
            new XeLinkHome(req),
            new XeLinkSelf(req),
            new XeLocalhost(),
            new XeIdentity(req),
            new XeAlias(base, req),
            new XeFavicon(base, req),
            new XeFlash(req),
            new XeGithubLink(req, Manifests.read("Netbout-GithubId")),
            new XeFacebookLink(req, Manifests.read("Netbout-FbId")),
            new XeGoogleLink(
                req, Manifests.read("Netbout-GoogleId"),
                "http://www.netbout.com/?PsByFlag=PsGoogle"
            ),
            new XeLogoutLink(req),
            new XeAppend(
                "version",
                new XeAppend("name", Manifests.read("Netbout-Version")),
                new XeAppend("rev", Manifests.read("Netbout-Revision")),
                new XeAppend("date", Manifests.read("Netbout-Date"))
            ),
            new XeMillis(true)
        );
    }

}
