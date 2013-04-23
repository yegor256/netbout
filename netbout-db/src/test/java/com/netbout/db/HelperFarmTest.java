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
package com.netbout.db;

import com.jcabi.urn.URN;
import java.net.URL;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link HelperFarm}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class HelperFarmTest {

    /**
     * Farm to work with.
     */
    private final transient HelperFarm farm = new HelperFarm();

    /**
     * HelperFarm can find bouts of some identity.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void registersNewHelperAndFindsIt() throws Exception {
        final URN identity = new IdentityRowMocker().mock();
        final URL url = new URL("http://localhost/some-address");
        this.farm.identityPromoted(identity, url);
        final List<URN> names = this.farm.getAllHelpers();
        MatcherAssert.assertThat(names, Matchers.hasItem(identity));
        MatcherAssert.assertThat(
            this.farm.getHelperUrl(identity),
            Matchers.equalTo(url)
        );
    }

    /**
     * HelperFarm can register helper twice.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void registersHelperTwice() throws Exception {
        final URN identity = new IdentityRowMocker().mock();
        final URL url = new URL("http://localhost/some-other-address");
        this.farm.identityPromoted(identity, url);
        this.farm.identityPromoted(identity, url);
    }

    /**
     * HelperFarm can catch a problem if a new URL is different.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = IllegalArgumentException.class)
    public void registersHelperTwiceWithDifferentUrl() throws Exception {
        final URN identity = new IdentityRowMocker().mock();
        this.farm.identityPromoted(identity, new URL("http://localhost/abc"));
        this.farm.identityPromoted(identity, new URL("http://localhost/cde"));
    }

}
