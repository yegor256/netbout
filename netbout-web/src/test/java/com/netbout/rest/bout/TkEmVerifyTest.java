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
package com.netbout.rest.bout;

import com.jcabi.manifests.Manifests;
import com.jcabi.urn.URN;
import com.netbout.mock.MkBase;
import com.netbout.rest.TkEmVerify;
import com.netbout.spi.Alias;
import com.netbout.spi.User;
import java.net.URLEncoder;
import java.util.Iterator;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.Test;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.forward.RsFailure;
import org.takes.facets.forward.RsForward;

/**
 * Test case for {@link TkEmVerify}.
 * @author Dragan Bozanovic (bozanovicdr@gmail.com)
 * @version $Id$
 * @since 2.22
 */
public final class TkEmVerifyTest {
    /**
     * RqRegex.Fake pattern.
     */
    private static final String PATTERN = "(.*)";
    /**
     * Encryptor.
     */
    private static final StandardPBEStringEncryptor ENC =
        new StandardPBEStringEncryptor();

    static {
        TkEmVerifyTest.ENC.setPassword(
            Manifests.read("Netbout-EmailCryptSecret")
        );
    }

    /**
     * TkEmVerify can verify email.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void verifiesEmail() throws Exception {
        final MkBase base = new MkBase();
        final String urn = "urn:test:1";
        final User user = base.user(new URN(urn));
        user.aliases().add("alias1");
        final Alias alias = user.aliases().iterate().iterator().next();
        alias.email("old@example.com!new1@example.com");
        new TkEmVerify(base).act(
            request("urn:test:1:alias1:new1@example.com")
        );
        MatcherAssert.assertThat(
            alias.email(),
            Matchers.equalTo("new1@example.com")
        );
    }
    /**
     *
     * TkEmVerify can reject invalid verification link.
     * @throws Exception If some problem inside
     */
    @Test(expected = RsFailure.class)
    public void rejectsInvalidVerificationLink() throws Exception {
        final MkBase base = new MkBase();
        final String urn = "urn:test:2";
        final User user = base.user(new URN(urn));
        user.aliases().add("alias2");
        final Alias alias = user.aliases().iterate().iterator().next();
        alias.email("old@example.com!new2@example.com");
        new TkEmVerify(base).act(request("urn:test:2:alias2:ab@cd.com"));
    }
    /**
     * TkEmVerify can reject verification when none necessary.
     * @throws Exception If some problem inside
     */
    @Test(expected = RsFailure.class)
    public void rejectsUnneededVerification() throws Exception {
        final MkBase base = new MkBase();
        final String urn = "urn:test:3";
        final User user = base.user(new URN(urn));
        user.aliases().add("alias3");
        final Alias alias = user.aliases().iterate().iterator().next();
        alias.email("old@example.com");
        new TkEmVerify(base).act(
            request("urn:test:3:alias3:new3@example.com")
        );
    }
    /**
     *
     * TkEmVerify can return user friendly message if
     * the verification url is not properly encoded.
     * @throws Exception If some problem inside
     */
    @Test
    public void returnsUserFriendlyMessage() throws Exception {
        final MkBase base = new MkBase();
        final String urn = "urn:test:4";
        final User user = base.user(new URN(urn));
        user.aliases().add("alias4");
        final Alias alias = user.aliases().iterate().iterator().next();
        alias.email("old@example.com!new4@example.com");
        try {
            new TkEmVerify(base).act(
                new RqRegex.Fake(TkEmVerifyTest.PATTERN, "x")
            );
        } catch (final RsForward ex) {
            MatcherAssert.assertThat(
                ex,
                Matchers.not(Matchers.instanceOf(RsFailure.class))
            );
            final Iterator<String> response = ex.head().iterator();
            response.next();
            final String space = " ";
            final String[] cookie = response
                .next().split(space)[1].split("./")[0].split("=");
            MatcherAssert.assertThat(
                cookie[1].replace("+", space),
                Matchers.equalTo("verification link not valid")
            );
        }
    }
    /**
     * Creates a RqRegex for the provided verification code.
     *
     * @param code Verification code
     * @return RqRegex
     * @throws Exception If some problem inside
     */
    private static RqRegex request(final String code) throws Exception {
        return new RqRegex.Fake(
            TkEmVerifyTest.PATTERN,
            URLEncoder.encode(TkEmVerifyTest.ENC.encrypt(code), "UTF-8")
        );
    }
}
