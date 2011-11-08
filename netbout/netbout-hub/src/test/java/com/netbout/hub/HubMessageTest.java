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

import java.net.URL;
import org.junit.Test;

/**
 * Test case of {@link HubMessage}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class HubMessageTest {

    /**
     * Object can be converted to XML through JAXB.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testJaxbIsWorking() throws Exception {
        // final Bout bout = Mockito.mock(Bout.class);
        // final Identity identity = Mockito.mock(Identity.class);
        // final Message message = new HubMessage(
        //     bout,
        //     identity,
        //     "this is a message",
        //     new Date()
        // );
        // final Source xml = JaxbConverter.the(message);
        // MatcherAssert.assertThat(
        //     xml,
        //     XmlMatchers.hasXPath("/message/author/name[.='John Doe']")
        // );
        // MatcherAssert.assertThat(
        //     xml,
        //     XmlMatchers.hasXPath("/message/text[starts-with(.,'this is')]")
        // );
        // MatcherAssert.assertThat(
        //     xml,
        //     XmlMatchers.hasXPath("/message/date")
        // );
    }

}
