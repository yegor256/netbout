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

import com.netbout.mock.MkBase;
import com.netbout.spi.Base;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.takes.Request;
import org.takes.facets.auth.RqWithAuth;
import org.xembly.Xembler;

/**
 * Test case for {@link XeAlias}.
 * @author Carlos Gines (efrel.v2@gmail.com)
 * @version $Id$
 * @since 2.23
 */
public final class XeAliasTest {
    /**
     * XeAlias can generate corresponding Xembly properly.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void generatesXembly() throws Exception {
        final Base base = new MkBase();
        final Request request = new RqWithAuth("urn:test:1");
        final String xml = new Xembler(
            new XePage(base, request, new XeAlias(base, request)).toXembly()
        ).xml();
        MatcherAssert.assertThat(
            xml, Matchers.containsString("<name>")
        );
        MatcherAssert.assertThat(
            xml, Matchers.containsString("<locale>")
        );
        MatcherAssert.assertThat(
            xml, Matchers.containsString("<photo>")
        );
        MatcherAssert.assertThat(
            xml, Matchers.containsString("<email>")
        );
        MatcherAssert.assertThat(
            xml, Matchers.containsString("<newEmail/>")
        );
    }
}
