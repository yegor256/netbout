/**
 * Copyright (c) 2009-2011, netBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code occasionally and without intent to use it, please report this
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
package com.netbout.hub;

import com.netbout.spi.Identity;
import java.net.URL;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link User}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class UserTest {

    // /**
    //  * Name persistence.
    //  * @throws Exception If there is some problem inside
    //  */
    // @Test
    // public void testPersistenceOfHubUserName() throws Exception {
    //     final String name = "Big Lebowski";
    //     HubEntry.user(name);
    //     MatcherAssert.assertThat(
    //         HubEntry.user(name).name(),
    //         Matchers.equalTo(name)
    //     );
    // }
    //
    // /**
    //  * Identities should be persistent for a given user.
    //  * @throws Exception If there is some problem inside
    //  */
    // @Test
    // public void testPersistenceOfIdentities() throws Exception {
    //     final String name = "John Doe";
    //     final HubUser user = HubEntry.user(name);
    //     final String label = "8879";
    //     final URL photo = new URL("http://img.netbout.com/logo.png");
    //     final Identity identity = user.identity(label);
    //     identity.setPhoto(photo);
    //     MatcherAssert.assertThat(
    //         HubEntry.user(name).identity(label).photo(),
    //         Matchers.equalTo(photo)
    //     );
    // }
    //
    // /**
    //  * Duplicate identities should be prohibited.
    //  * @throws Exception If there is some problem inside
    //  */
    // @Test(expected = IllegalArgumentException.class)
    // public void testDuplicateIdentityCreation() throws Exception {
    //     final String name = "882763";
    //     HubEntry.user("peter").identity(name);
    //     HubEntry.user("alex").identity(name);
    // }

}
