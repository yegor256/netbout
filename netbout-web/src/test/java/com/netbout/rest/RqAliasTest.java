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

import com.jcabi.urn.URN;
import com.netbout.mock.MkBase;
import java.net.URI;
import java.util.Collections;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.takes.facets.auth.Identity;
import org.takes.facets.auth.RqWithAuth;
import org.takes.rq.RqFake;

/**
 * Test case for {@link RqAlias}.
 *
 * @author Dmitry Zaytsev (dmitry.zaytsev@gmail.com)
 * @version $Id$
 * @since 2.21
 */
public final class RqAliasTest {
    /**
     * RqAlias can get the photo.
     * @throws Exception if some problems inside
     */
    @Test
    public void getsPhoto() throws Exception {
        final String[] pict = {
            "https://github.com/u/1",
            "https://facebook.com/u/1",
            "https://google.com/u/1",
        };
        final String picture = "picture";
        MatcherAssert.assertThat(
            RqAliasTest.photo("urn:github:1", "avatar", pict[0]),
            Matchers.equalTo(new URI(pict[0]))
        );
        MatcherAssert.assertThat(
            RqAliasTest.photo("urn:facebook:1", picture, pict[1]),
            Matchers.equalTo(new URI(pict[1]))
        );
        MatcherAssert.assertThat(
            RqAliasTest.photo("urn:google:1", picture, pict[2]),
            Matchers.equalTo(new URI(pict[2]))
        );
    }

    /**
     * Returns urn's photo.
     * @param urn Urn
     * @param prop Property name
     * @param pict Picture uri
     * @return Photo
     * @throws Exception if some problems inside
     */
    private static URI photo(final String urn, final String prop,
        final String pict) throws Exception {
        final MkBase base = new MkBase();
        base.user(new URN(urn)).aliases().add("jeff");
        return new RqAlias(
            base,
            new RqWithAuth(
                new Identity.Simple(
                    urn,
                    Collections.singletonMap(prop, pict)
                ), new RqFake()
            )
        ).alias().photo();
    }
}
