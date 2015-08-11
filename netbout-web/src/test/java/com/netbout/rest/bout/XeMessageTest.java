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
package com.netbout.rest.bout;

import com.netbout.mock.MkBase;
import com.netbout.spi.Message;
import java.util.Date;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.xembly.Xembler;

/**
 * Test case for {@link XeMessage}.
 * @author Dmitry Zaytsev (dmitry.zaytsev@gmail.com)
 * @version $Id$
 * @since 2.16.1
 *
 */
public final class XeMessageTest {
    /**
     * XeMessage can handle non printable characters.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void handlesNonPrintableChars() throws Exception {
        final String xml = new Xembler(
            new XeMessage(
                new MkBase().randomBout(),
                new Message() {
                    @Override
                    public long number() {
                        return 0L;
                    }
                    @Override
                    public Date date() {
                        return new Date();
                    }
                    @Override
                    public String text() {
                        return String.format("text contains %c", 0x0);
                    }
                    @Override
                    public String author() {
                        return String.format("author  contains %c", 0x0);
                    }
                }
            ).toXembly()
        ).xml();
        MatcherAssert.assertThat(
            xml,
            Matchers.containsString("<author>author  contains \\u0000</author>")
        );
        MatcherAssert.assertThat(
            xml,
            Matchers.containsString("<text>text contains \\u0000</text>")
        );
    }
}
