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

import com.netbout.spi.Identity;
import com.netbout.spi.IdentityMocker;
import com.netbout.spi.OwnProfileMocker;
import com.netbout.spi.Urn;
import com.netbout.spi.UrnMocker;
import com.rexsl.test.XhtmlMatchers;
import java.net.HttpURLConnection;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link FriendsRs}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class FriendsRsTest {

    /**
     * FriendsRs can render a login page.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void rendersListOfFriends() throws Exception {
        final FriendsRs rest = new NbResourceMocker().mock(FriendsRs.class);
        final Response response = rest.list("a", "123");
        MatcherAssert.assertThat(
            NbResourceMocker.the((NbPage) response.getEntity(), rest),
            XhtmlMatchers.hasXPath("/page[mask='a']")
        );
    }

    /**
     * FriendsRs can show a photo of a user.
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void showsPhotoOfAnIdentity() throws Exception {
        final String[] photos = new String[] {
            "http://img.netbout.com/logo-small.png",
            "http://img.netbout.com/non-existing-image.png",
        };
        for (String photo : photos) {
            final Identity identity = new IdentityMocker().mock();
            Mockito.doReturn(
                new IdentityMocker().withProfile(
                    new OwnProfileMocker().withPhoto(photo).mock()
                ).mock()
            ).when(identity).friend(Mockito.any(Urn.class));
            final FriendsRs rest = new NbResourceMocker()
                .withIdentity(identity)
                .mock(FriendsRs.class);
            final Response response = rest.photo(new UrnMocker().mock());
            MatcherAssert.assertThat(
                response,
                Matchers.allOf(
                    Matchers.hasProperty(
                        "status",
                        Matchers.equalTo(HttpURLConnection.HTTP_OK)
                    ),
                    Matchers.hasProperty(
                        "metadata",
                        Matchers.hasEntry(
                            Matchers.equalTo(HttpHeaders.CONTENT_TYPE),
                            Matchers.hasItem("image/png")
                        )
                    ),
                    Matchers.hasProperty("entity", Matchers.notNullValue())
                )
            );
        }
    }

}
