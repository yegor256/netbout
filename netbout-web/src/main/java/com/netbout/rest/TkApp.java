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
import com.netbout.rest.account.TkAccount;
import com.netbout.rest.bout.TkBout;
import com.netbout.rest.login.TkLogin;
import com.netbout.spi.Base;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Pattern;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.auth.PsByFlag;
import org.takes.facets.auth.PsChain;
import org.takes.facets.auth.PsCookie;
import org.takes.facets.auth.PsFake;
import org.takes.facets.auth.PsLogout;
import org.takes.facets.auth.TkAuth;
import org.takes.facets.auth.codecs.CcCompact;
import org.takes.facets.auth.codecs.CcHex;
import org.takes.facets.auth.codecs.CcSafe;
import org.takes.facets.auth.codecs.CcSalted;
import org.takes.facets.auth.codecs.CcXOR;
import org.takes.facets.auth.social.PsGithub;
import org.takes.facets.fallback.Fallback;
import org.takes.facets.fallback.RqFallback;
import org.takes.facets.fallback.TkFallback;
import org.takes.facets.flash.TkFlash;
import org.takes.facets.fork.FkParams;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.TkFork;
import org.takes.facets.forward.TkForward;
import org.takes.rs.RsVelocity;
import org.takes.rs.RsWithStatus;
import org.takes.rs.RsWithType;
import org.takes.tk.TkClasspath;
import org.takes.tk.TkGzip;
import org.takes.tk.TkMeasured;
import org.takes.tk.TkRedirect;
import org.takes.tk.TkVersioned;
import org.takes.tk.TkWithHeaders;
import org.takes.tk.TkWithType;
import org.takes.tk.TkWrap;

/**
 * Web app.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.14
 */
public final class TkApp extends TkWrap {

    /**
     * Revision of netbout.
     */
    private static final String REV = Manifests.read("Netbout-Revision");

    /**
     * Ctor.
     * @param base Base
     */
    public TkApp(final Base base) {
        super(TkApp.make(base));
    }

    /**
     * Ctor.
     * @param base Base
     * @return Take
     */
    private static Take make(final Base base) {
        if (!"UTF-8".equals(Charset.defaultCharset().name())) {
            throw new IllegalStateException(
                String.format(
                    "default encoding is %s", Charset.defaultCharset()
                )
            );
        }
        final Take take = new TkGzip(
            TkApp.fallback(
                new TkFlash(
                    TkApp.auth(
                        new TkForward(TkApp.regex(base))
                    )
                )
            )
        );
        return new TkWithHeaders(
            new TkVersioned(new TkMeasured(take)),
            String.format("X-Rultor-Revision: %s", TkApp.REV),
            "Vary: Cookie"
        );
    }

    /**
     * Authenticated.
     * @param takes Take
     * @return Authenticated takes
     */
    private static Take fallback(final Take takes) {
        return new TkFallback(
            takes,
            // @checkstyle AnonInnerLengthCheck (50 lines)
            new Fallback() {
                @Override
                public Iterator<Response> route(final RqFallback req)
                    throws IOException {
                    final String err = ExceptionUtils.getStackTrace(
                        req.throwable()
                    );
                    return Collections.<Response>singleton(
                        new RsWithStatus(
                            new RsWithType(
                                new RsVelocity(
                                    this.getClass().getResource(
                                        "error.html.vm"
                                    ),
                                    new RsVelocity.Pair("err", err),
                                    new RsVelocity.Pair("rev", TkApp.REV)
                                ),
                                "text/html"
                            ),
                            HttpURLConnection.HTTP_INTERNAL_ERROR
                        )
                    ).iterator();
                }
            }
        );
    }

    /**
     * Authenticated.
     * @param takes Take
     * @return Authenticated takes
     */
    private static Take auth(final Take takes) {
        return new TkAuth(
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
     * @return Take
     */
    private static Take regex(final Base base) {
        return new TkFork(
            new FkParams(
                PsByFlag.class.getSimpleName(),
                Pattern.compile(".+"),
                new TkRedirect()
            ),
            new FkRegex("/robots.txt", ""),
            new FkRegex(
                "/xsl/[a-z]+\\.xsl",
                new TkWithType(new TkClasspath(), "text/xsl")
            ),
            new FkRegex(
                "/js/[a-z]+\\.js",
                new TkWithType(new TkClasspath(), "text/javascript")
            ),
            new FkRegex(
                "/css/[a-z]+\\.css",
                new TkWithType(new TkClasspath(), "text/css")
            ),
            new FkRegex(
                "/lang/[a-z]+\\.xml",
                new TkWithType(new TkClasspath(), "text/xml")
            ),
            new FkRegex("/", new TkInbox(base)),
            new FkRegex("/start", new TkStart(base)),
            new FkRegex("/f/([a-zA-Z0-9]+)\\.png", new TkFriend(base)),
            new FkRegex("/favicon.ico", new TkFavicon()),
            new FkRegex("/login/.*", new TkLogin(base)),
            new FkRegex("/b/.*", new TkBout(base)),
            new FkRegex("/acc/.*", new TkAccount(base))
        );
    }

}
