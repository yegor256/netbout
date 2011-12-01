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
 * Test case of {@link HubIdentity}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class HubIdentityTest {

    // /**
    //  * Name is persistent.
    //  * @throws Exception If there is some problem inside
    //  */
    // @Test
    // public void testNamePersistence() throws Exception {
    //     final HubUser user = HubEntry.user("Johnny Depp");
    //     final String name = "1345";
    //     MatcherAssert.assertThat(
    //         user.identity(name).name(),
    //         Matchers.equalTo(name)
    //     );
    // }
    //
    // /**
    //  * Photo is persistent.
    //  * @throws Exception If there is some problem inside
    //  */
    // @Test
    // public void testPhotoPersistence() throws Exception {
    //     final HubUser user = HubEntry.user("Bruce Willis");
    //     final String name = "9865";
    //     final URL photo = new URL("http://localhost/photo.png");
    //     user.identity(name).setPhoto(photo);
    //     MatcherAssert.assertThat(
    //         user.identity(name).photo(),
    //         Matchers.equalTo(photo)
    //     );
    // }
    //
    // /**
    //  * Manipulate with bouts.
    //  * @throws Exception If there is some problem inside
    //  */
    // @Test
    // public void testBoutsManipulations() throws Exception {
    //     final Identity identity = HubEntry.user("Jeffy").identity("73267");
    //     final Long number = identity.start().number();
    //     identity.bout(number);
    //     MatcherAssert.assertThat(
    //         identity.inbox("").size(),
    //         Matchers.equalTo(1)
    //     );
    // }
    //
    // /**
    //  * Manipulate with aliases.
    //  * @throws Exception If there is some problem inside
    //  */
    // @Test
    // public void testAliasesManipulations() throws Exception {
    //     final Identity identity = HubEntry.user("Lori").identity("7244");
    //     MatcherAssert.assertThat(
    //         identity.aliases().size(),
    //         Matchers.equalTo(0)
    //     );
    //     final String alias = "lori@example.com";
    //     identity.alias(alias);
    //     identity.alias("lorisa.townsend@example.com");
    //     MatcherAssert.assertThat(
    //         identity.aliases().size(),
    //         Matchers.equalTo(2)
    //     );
    //     MatcherAssert.assertThat(
    //         identity.aliases(),
    //         Matchers.hasItem(alias)
    //     );
    // }
    //
    // /**
    //  * Find bout that belongs to someone else.
    //  * @throws Exception If there is some problem inside
    //  */
    // @Test(expected = com.netbout.spi.BoutNotFoundException.class)
    // public void testFindingOfNotMyBout() throws Exception {
    //     final Long num = HubEntry.user("Victor").identity("66212")
    //         .start().number();
    //     HubEntry.user("Michael").identity("9980").bout(num);
    // }
    //
    // /**
    //  * Find non-existing bout.
    //  * @throws Exception If there is some problem inside
    //  */
    // @Test(expected = com.netbout.spi.BoutNotFoundException.class)
    // public void testFindingOfNonExistingBout() throws Exception {
    //     // @checkstyle MagicNumber (1 line)
    //     HubEntry.user("Sarah").identity("3324").bout(3456L);
    // }

}
