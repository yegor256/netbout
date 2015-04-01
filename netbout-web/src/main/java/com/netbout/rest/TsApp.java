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
package com.netbout.rest;

import com.jcabi.manifests.Manifests;
import com.jcabi.urn.URN;
import com.netbout.spi.Base;
import com.netbout.spi.User;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.regex.Pattern;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.takes.Request;
import org.takes.Take;
import org.takes.Takes;
import org.takes.facets.auth.PsByFlag;
import org.takes.facets.auth.PsChain;
import org.takes.facets.auth.PsCookie;
import org.takes.facets.auth.PsFake;
import org.takes.facets.auth.PsLogout;
import org.takes.facets.auth.RqAuth;
import org.takes.facets.auth.TsAuth;
import org.takes.facets.auth.codecs.CcCompact;
import org.takes.facets.auth.codecs.CcHex;
import org.takes.facets.auth.codecs.CcSafe;
import org.takes.facets.auth.codecs.CcSalted;
import org.takes.facets.auth.codecs.CcXOR;
import org.takes.facets.auth.social.PsGithub;
import org.takes.facets.fallback.Fallback;
import org.takes.facets.fallback.RqFallback;
import org.takes.facets.fallback.TsFallback;
import org.takes.facets.flash.TsFlash;
import org.takes.facets.fork.FkParams;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.Target;
import org.takes.facets.fork.TsFork;
import org.takes.facets.forward.TsForward;
import org.takes.rs.RsVelocity;
import org.takes.rs.RsWithStatus;
import org.takes.rs.RsWithType;
import org.takes.tk.TkFixed;
import org.takes.tk.TkRedirect;
import org.takes.ts.TsClasspath;
import org.takes.ts.TsGzip;
import org.takes.ts.TsMeasured;
import org.takes.ts.TsVersioned;
import org.takes.ts.TsWithHeaders;
import org.takes.ts.TsWithType;
import org.takes.ts.TsWrap;

/**
 * Web app.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 2.14
 */
public final class TsApp extends TsWrap {

    /**
     * Revision of netbout.
     */
    private static final String REV = Manifests.read("Netbout-Revision");

    /**
     * Ctor.
     * @param base Base
     */
    public TsApp(final Base base) {
        super(TsApp.make(base));
    }

    /**
     * Ctor.
     * @param base Base
     * @return Takes
     */
    private static Takes make(final Base base) {
        if (!"UTF-8".equals(Charset.defaultCharset().name())) {
            throw new IllegalStateException(
                String.format(
                    "default encoding is %s", Charset.defaultCharset()
                )
            );
        }
        final Takes takes = new TsGzip(
            TsApp.fallback(
                new TsFlash(
                    TsApp.auth(
                        new TsForward(TsApp.regex(base))
                    )
                )
            )
        );
        return new TsWithHeaders(
            new TsVersioned(new TsMeasured(takes)),
            String.format("X-Rultor-Revision: %s", TsApp.REV),
            "Vary: Cookie"
        );
    }

    /**
     * Authenticated.
     * @param takes Takes
     * @return Authenticated takes
     */
    private static Takes fallback(final Takes takes) {
        return new TsFallback(
            takes,
            // @checkstyle AnonInnerLengthCheck (50 lines)
            new Fallback() {
                @Override
                public Take take(final RqFallback req) throws IOException {
                    final String err = ExceptionUtils.getStackTrace(
                        req.throwable()
                    );
                    return new TkFixed(
                        new RsWithStatus(
                            new RsWithType(
                                new RsVelocity(
                                    this.getClass().getResource(
                                        "error.html.vm"
                                    ),
                                    new RsVelocity.Pair("err", err),
                                    new RsVelocity.Pair("rev", TsApp.REV)
                                ),
                                "text/html"
                            ),
                            HttpURLConnection.HTTP_INTERNAL_ERROR
                        )
                    );
                }
            }
        );
    }

    /**
     * Authenticated.
     * @param takes Takes
     * @return Authenticated takes
     */
    private static Takes auth(final Takes takes) {
        return new TsAuth(
            takes,
            new PsChain(
                new PsFake(
                    Manifests.read("Netbout-DynamoKey").startsWith("AAAA")
                ),
                new PsByFlag(
                    new PsByFlag.Pair(
                        PsGithub.class.getSimpleName(),
                        new PsGithub(
                            Manifests.read("Netbout-GithubId"),
                            Manifests.read("Netbout-GithubSecret")
                        )
                    ),
                    new PsByFlag.Pair(
                        PsLogout.class.getSimpleName(),
                        new PsLogout()
                    )
                ),
                new PsCookie(
                    new CcSafe(
                        new CcHex(
                            new CcXOR(
                                new CcSalted(new CcCompact()),
                                Manifests.read("Netbout-SecurityKey")
                            )
                        )
                    )
                )
            )
        );
    }

    /**
     * Regex takes.
     * @param base Base
     * @return Takes
     */
    private static Takes regex(final Base base) {
        return new TsFork(
            new FkParams(
                PsByFlag.class.getSimpleName(),
                Pattern.compile(".+"),
                new TkRedirect()
            ),
            new FkRegex("/robots.txt", ""),
            new FkRegex(
                "/xsl/.*",
                new TsWithType(new TsClasspath(), "text/xsl")
            ),
            new FkRegex(
                "/js/.*",
                new TsWithType(new TsClasspath(), "text/javascript")
            ),
            new FkRegex(
                "/css/.*",
                new TsWithType(new TsClasspath(), "text/css")
            ),
            new FkRegex(
                "/",
                new Target<RqRegex>() {
                    @Override
                    public Take route(final RqRegex req) {
                        return new TkHome(talks, toggles, req);
                    }
                }
            ),
            new FkRegex(
                "/f/([a-zA-Z0-9]+)\\.png",
                new Target<RqRegex>() {
                    @Override
                    public Take route(final RqRegex req) throws IOException {
                        return new TkFriend(
                            TsApp.user(base, req),
                            req.matcher().group(1)
                        );
                    }
                }
            )
        );
    }

    /**
     * Get authenticated user.
     * @param base Base
     * @param req Request
     * @return User
     * @throws IOException If fails
     */
    private static User user(final Base base, final Request req)
        throws IOException {
        base.user(
            URN.create(new RqAuth(req).identity().urn())
        )
    }

}
