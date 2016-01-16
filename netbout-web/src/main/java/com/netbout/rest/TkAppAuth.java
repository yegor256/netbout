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
import java.io.IOException;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.auth.Identity;
import org.takes.facets.auth.Pass;
import org.takes.facets.auth.PsBasic;
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
import org.takes.facets.auth.social.PsFacebook;
import org.takes.facets.auth.social.PsGithub;
import org.takes.facets.auth.social.PsGoogle;
import org.takes.misc.Opt;
import org.takes.rq.RqHref;
import org.takes.tk.TkWrap;

/**
 * Authenticated app.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.14
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class TkAppAuth extends TkWrap {

    /**
     * Testing mode is ON?
     */
    private static final boolean TESTING =
        Manifests.read("Netbout-DynamoKey").startsWith("AAAA");

    /**
     * BasicAuth is ON?
     */
    private static final boolean BASICAUTH =
        "true".equals(Manifests.read("Netbout-Basic-On"));

    /**
     * Ctor.
     * @param take Take
     */
    TkAppAuth(final Take take) {
        this(take, new PsFake(TkAppAuth.TESTING));
    }

    /**
     * Ctor.
     * @param take Take
     * @param pass Last Pass on Chain
     */
    TkAppAuth(final Take take, final Pass pass) {
        super(TkAppAuth.make(take, pass, TkAppAuth.BASICAUTH));
    }
    /**
     * Ctor.
     * @param take Take
     * @param pass Last Pass on Chain
     * @param basic Use Basic Auth?
     */
    TkAppAuth(final Take take, final Pass pass, final boolean basic) {
        super(TkAppAuth.make(take, pass, basic));
    }

    /**
     * Authenticated.
     * @param take Take
     * @param pass Last Pass on Chain
     * @param basic Use Basic Auth?
     * @return Authenticated take
     */
    private static Take make(final Take take, final Pass pass,
        final boolean basic) {
        final Pass auth;
        if (basic) {
            auth = new PsBasic(
                Manifests.read("Netbout-Basic-Realm"),
                new PsBasic.Default(
                    String.format(
                        "%s %s urn:foo:z",
                        Manifests.read("Netbout-Basic-User"),
                        Manifests.read("Netbout-Basic-Pwd")
                    )
                )
            );
        } else {
            auth = new PsFake(true);
        }
        return new TkAuth(
            take,
            new PsTwice(
                auth,
                new PsChain(
                    new PsByFlag(
                        new PsByFlag.Pair(
                            PsGithub.class.getSimpleName(),
                            new PsGithub(
                                Manifests.read("Netbout-GithubId"),
                                Manifests.read("Netbout-GithubSecret")
                            )
                        ),
                        new PsByFlag.Pair(
                            PsFacebook.class.getSimpleName(),
                            new PsFacebook(
                                Manifests.read("Netbout-FbId"),
                                Manifests.read("Netbout-FbSecret")
                            )
                        ),
                        new PsByFlag.Pair(
                            PsGoogle.class.getSimpleName(),
                            new PsGoogle(
                                Manifests.read("Netbout-GoogleId"),
                                Manifests.read("Netbout-GoogleSecret"),
                                "http://www.netbout.com/?PsByFlag=PsGoogle"
                            )
                        ),
                        new PsByFlag.Pair(
                            "fake-user",
                            new TkAppAuth.FakePass()
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
                    ),
                    pass
                )
            )
        );
    }

    /**
     * Fake pass.
     */
    private static final class FakePass implements Pass {
        @Override
        public Opt<Identity> enter(final Request req) throws IOException {
            final Opt<Identity> identity;
            if (TkAppAuth.TESTING) {
                identity = new Opt.Single<Identity>(
                    new Identity.Simple(
                        new RqHref.Smart(new RqHref.Base(req)).single("urn")
                    )
                );
            } else {
                identity = new Opt.Empty<>();
            }
            return identity;
        }

        @Override
        public Response exit(final Response response, final Identity identity) {
            return response;
        }
    }
}
