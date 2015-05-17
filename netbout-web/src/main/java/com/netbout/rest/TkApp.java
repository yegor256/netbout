/**
 * Copyright (c) 2009-2015, netbout.com
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

import com.jcabi.log.VerboseProcess;
import com.jcabi.manifests.Manifests;
import com.netbout.rest.account.TkAccount;
import com.netbout.rest.bout.TkBout;
import com.netbout.rest.login.TkLogin;
import com.netbout.spi.Base;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Pattern;
import org.takes.Take;
import org.takes.facets.auth.PsByFlag;
import org.takes.facets.flash.TkFlash;
import org.takes.facets.fork.FkAnonymous;
import org.takes.facets.fork.FkAuthenticated;
import org.takes.facets.fork.FkFixed;
import org.takes.facets.fork.FkHitRefresh;
import org.takes.facets.fork.FkParams;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.TkFork;
import org.takes.facets.forward.TkForward;
import org.takes.rs.RsRedirect;
import org.takes.tk.TkClasspath;
import org.takes.tk.TkFiles;
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
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 * @checkstyle ClassFanOutComplexityCheck (500 lines)
 */
@SuppressWarnings("PMD.ExcessiveImports")
public final class TkApp extends TkWrap {

    /**
     * Version of the system, to show in header.
     */
    private static final String VERSION = String.format(
        "%s %s %s",
        // @checkstyle MultipleStringLiterals (3 lines)
        Manifests.read("Netbout-Version"),
        Manifests.read("Netbout-Revision"),
        Manifests.read("Netbout-Date")
    );

    /**
     * Ctor.
     * @param base Base
     * @throws IOException If fails
     */
    public TkApp(final Base base) throws IOException {
        super(TkApp.make(base));
    }

    /**
     * Ctor.
     * @param base Base
     * @return Take
     * @throws IOException If fails
     */
    private static Take make(final Base base) throws IOException {
        if (!"UTF-8".equals(Charset.defaultCharset().name())) {
            throw new IllegalStateException(
                String.format(
                    "default encoding is %s", Charset.defaultCharset()
                )
            );
        }
        return new TkWithHeaders(
            new TkVersioned(
                new TkMeasured(
                    new TkFlash(
                        new TkAppFallback(
                            new TkForward(
                                new TkAppAuth(
                                    TkApp.regex(base)
                                )
                            )
                        )
                    )
                )
            ),
            String.format("X-Netbout-Version: %s", TkApp.VERSION),
            "Vary: Cookie"
        );
    }

    /**
     * Regex takes.
     * @param base Base
     * @return Take
     * @throws IOException If fails
     */
    private static Take regex(final Base base) throws IOException {
        return new TkFork(
            new FkParams(
                PsByFlag.class.getSimpleName(),
                Pattern.compile(".+"),
                new TkRedirect()
            ),
            new FkRegex("/robots.txt", ""),
            new FkRegex(
                "/xsl/[a-z\\-]+\\.xsl",
                new TkWithType(
                    TkApp.refresh("./netbout-web/src/main/xsl"),
                    "text/xsl"
                )
            ),
            new FkRegex(
                "/js/[a-z]+\\.js",
                new TkWithType(
                    TkApp.refresh("./netbout-web/src/main/js"),
                    "text/javascript"
                )
            ),
            new FkRegex(
                "/css/[a-z]+\\.css",
                new TkWithType(
                    TkApp.refresh("./netbout-web/src/main/scss"),
                    "text/css"
                )
            ),
            new FkRegex(
                "/lang/[a-z]+\\.xml",
                new TkWithType(
                    TkApp.refresh("./netbout-web/src/main/resources/lang"),
                    "text/xml"
                )
            ),
            new FkRegex("/favicon.ico", new TkFavicon()),
            new FkAnonymous(
                new TkFork(
                    new FkRegex("/", new TkHome(base))
                )
            ),
            new FkAuthenticated(
                new TkFork(
                    new FkRegistered(
                        base,
                        new TkFork(
                            new FkRegex("/", new TkInbox(base)),
                            new FkRegex("/start", new TkStart(base)),
                            new FkRegex("/b/.*", new TkBout(base)),
                            new FkRegex("/acc/.*", new TkAccount(base)),
                            new FkRegex(
                                "/f/([a-zA-Z0-9]+)\\.png",
                                new TkFriend(base)
                            )
                        )
                    ),
                    new FkRegex("/", new RsRedirect("/login/start")),
                    new FkRegex("/login/.*", new TkLogin(base))
                )
            )
        );
    }

    /**
     * Hit refresh fork.
     * @param path Path of files
     * @return Fork
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.DoNotUseThreads")
    private static Take refresh(final String path) throws IOException {
        return new TkFork(
            new FkHitRefresh(
                new File(path),
                new Runnable() {
                    @Override
                    public void run() {
                        new VerboseProcess(
                            new ProcessBuilder(
                                "mvn",
                                "generate-resources"
                            )
                        ).stdout();
                    }
                },
                new TkFiles("./netbout-web/target/classes")
            ),
            new FkFixed(new TkClasspath())
        );
    }

}
