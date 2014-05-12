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

import com.google.common.collect.Iterables;
import com.jcabi.manifests.Manifests;
import com.jcabi.urn.URN;
import com.netbout.spi.Alias;
import com.netbout.spi.Base;
import com.netbout.spi.User;
import com.rexsl.page.BasePage;
import com.rexsl.page.BaseResource;
import com.rexsl.page.Inset;
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.Link;
import com.rexsl.page.Resource;
import com.rexsl.page.auth.AuthInset;
import com.rexsl.page.auth.Facebook;
import com.rexsl.page.auth.Github;
import com.rexsl.page.auth.Google;
import com.rexsl.page.auth.Identity;
import com.rexsl.page.auth.Provider;
import com.rexsl.page.inset.FlashInset;
import com.rexsl.page.inset.LinksInset;
import com.rexsl.page.inset.VersionInset;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.Validate;

/**
 * Base RESTful resource.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@Resource.Forwarded
@Inset.Default({ LinksInset.class, FlashInset.class })
@SuppressWarnings("PMD.TooManyMethods")
public class BaseRs extends BaseResource {

    /**
     * Version of the system, to show in header.
     */
    private static final String VERSION_LABEL = String.format(
        "%s/%s built on %s",
        // @checkstyle MultipleStringLiterals (3 lines)
        Manifests.read("Netbout-Version"),
        Manifests.read("Netbout-Revision"),
        Manifests.read("Netbout-Date")
    );

    /**
     * Test authentication provider.
     */
    private static final Provider TESTER = new Provider() {
        @Override
        public Identity identity() {
            final Identity identity;
            if ("1234567".equals(Manifests.read("Netbout-Revision"))) {
                identity = new Identity.Simple(
                    URN.create("urn:test:123456"),
                    "localhost",
                    URI.create("//img.netbout.com/localhost.png")
                );
            } else {
                identity = Identity.ANONYMOUS;
            }
            return identity;
        }
    };

    /**
     * Supplementary inset.
     * @return The inset
     */
    @Inset.Runtime
    public final Inset supplementary() {
        return new Inset() {
            @Override
            public void render(final BasePage<?, ?> page,
                final Response.ResponseBuilder builder) {
                builder.header("X-Netbout-Version", BaseRs.VERSION_LABEL);
                builder.type(MediaType.TEXT_XML);
                builder.header(HttpHeaders.VARY, "Cookie");
            }
        };
    }

    /**
     * Version inset.
     * @return The inset
     */
    @Inset.Runtime
    public final Inset version() {
        return new VersionInset(
            Manifests.read("Netbout-Version"),
            Manifests.read("Netbout-Revision"),
            Manifests.read("Netbout-Date")
        );
    }

    /**
     * Alias inset.
     * @return The inset
     */
    @Inset.Runtime
    public final Inset aliasInset() {
        // @checkstyle AnonInnerLengthCheck (50 lines)
        return new Inset() {
            @Override
            public void render(final BasePage<?, ?> page,
                final Response.ResponseBuilder builder) {
                if (BaseRs.this.identified()) {
                    try {
                        final Alias alias = BaseRs.this.alias();
                        page.append(
                            new JaxbBundle("alias")
                                .add("name", alias.name())
                                .up()
                                .add("locale", alias.locale().toString())
                                .up()
                                .add("photo", alias.photo().toString())
                                .up()
                        );
                    } catch (final IOException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            }
        };
    }

    /**
     * Alias links.
     * @return The inset
     */
    @Inset.Runtime
    public final Inset aliasLinks() {
        return new Inset() {
            @Override
            public void render(final BasePage<?, ?> page,
                final Response.ResponseBuilder builder) {
                if (BaseRs.this.identified()) {
                    page.link(new Link("start", "/start"));
                }
            }
        };
    }

    /**
     * Authentication inset.
     * @return The inset
     */
    @Inset.Runtime
    public final AuthInset auth() {
        // @checkstyle LineLength (4 lines)
        return new AuthInset(this, Manifests.read("Netbout-SecurityKey"))
            .with(new Facebook(this, Manifests.read("Netbout-FbId"), Manifests.read("Netbout-FbSecret")))
            .with(new Google(this, Manifests.read("Netbout-GoogleId"), Manifests.read("Netbout-GoogleSecret")))
            .with(new Github(this, Manifests.read("Netbout-GithubId"), Manifests.read("Netbout-GithubSecret")))
            .with(BaseRs.TESTER);
    }

    /**
     * Get current user.
     * @return User
     */
    protected final User user() {
        final Identity identity = this.auth().identity();
        if (identity.equals(Identity.ANONYMOUS)) {
            throw FlashInset.forward(
                this.uriInfo().getBaseUriBuilder().clone()
                    .path(LoginRs.class)
                    .build(),
                "please login first",
                Level.SEVERE
            );
        }
        return this.base().user(identity.urn());
    }

    /**
     * Get current alias.
     * @return Alias
     * @throws IOException If fails
     */
    protected final Alias alias() throws IOException {
        if (!this.identified()) {
            throw FlashInset.forward(
                this.uriInfo().getBaseUriBuilder().clone()
                    .path(LoginRs.class)
                    .path(LoginRs.class, "register")
                    .build(),
                "please create a unique alias",
                Level.SEVERE
            );
        }
        final Alias alias = Iterables.get(this.user().aliases().iterate(), 0);
        if (alias.photo().equals(Alias.BLANK)) {
            alias.photo(this.auth().identity().photo());
        }
        return alias;
    }

    /**
     * User identified and has an alias.
     * @return TRUE if it has an alias
     */
    protected final boolean identified() {
        return !this.auth().identity().equals(Identity.ANONYMOUS)
            && !Iterables.isEmpty(this.user().aliases().iterate());
    }

    /**
     * Get spi.
     * @return The spi
     */
    protected final Base base() {
        final Base base = Base.class.cast(
            this.servletContext().getAttribute(Base.class.getName())
        );
        Validate.notNull(base, "spi is not initialized");
        return base;
    }

}
