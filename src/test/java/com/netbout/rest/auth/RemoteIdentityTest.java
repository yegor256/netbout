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
package com.netbout.rest.auth;

import com.jcabi.urn.URN;
import com.netbout.spi.Identity;
import com.netbout.spi.Profile;
import com.netbout.spi.xml.JaxbParser;
import java.net.URL;
import java.util.Locale;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link RemoteIdentity}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class RemoteIdentityTest {

    /**
     * RemoteIdentity can be unmarshalled from XML text.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void unmarshallsFromXml() throws Exception {
        final String xml = "<?xml version='1.0' ?>"
            // @checkstyle StringLiteralsConcatenation (7 lines)
            + "<identity>"
            + "<aliases><alias>Johnny</alias></aliases>"
            + "<authority>http://localhost</authority>"
            + "<locale>zh</locale>"
            + "<name>urn:test:johnny</name>"
            + "<photo>http://localhost/photo.png</photo>"
            + "</identity>";
        final Identity identity =
            new JaxbParser(xml).parse(RemoteIdentity.class);
        MatcherAssert.assertThat(
            identity.name(),
            Matchers.equalTo(new URN("urn:test:johnny"))
        );
        MatcherAssert.assertThat(
            identity.profile().photo(),
            Matchers.equalTo(new URL("http://localhost/photo.png"))
        );
        MatcherAssert.assertThat(
            identity.profile().locale(),
            Matchers.equalTo(Locale.CHINESE)
        );
        MatcherAssert.assertThat(
            new Profile.Conventional(identity).aliases(),
            Matchers.hasItem("Johnny")
        );
    }

}
