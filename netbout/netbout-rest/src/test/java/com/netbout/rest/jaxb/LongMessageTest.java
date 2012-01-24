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
package com.netbout.rest.jaxb;

import com.netbout.hub.HubMocker;
import com.netbout.spi.BoutMocker;
import com.netbout.spi.MessageMocker;
import com.rexsl.test.JaxbConverter;
import com.rexsl.test.XhtmlMatchers;
import java.util.Map;
import org.apache.commons.lang.ArrayUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link LongMessage}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class LongMessageTest {

    /**
     * LongMessage can be converted to XML.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void convertsToXml() throws Exception {
        final LongMessage obj = new LongMessage(
            // @checkstyle MultipleStringLiterals (1 line)
            new HubMocker().doReturn("hello", "pre-render-message").mock(),
            new BoutMocker().mock(),
            new MessageMocker().withText("<>").mock()
        );
        MatcherAssert.assertThat(
            JaxbConverter.the(obj),
            Matchers.allOf(
                XhtmlMatchers.hasXPath("/message/number"),
                XhtmlMatchers.hasXPath("/message/author"),
                XhtmlMatchers.hasXPath("/message/text[.='<>']"),
                XhtmlMatchers.hasXPath("/message/render[.='hello']"),
                XhtmlMatchers.hasXPath("/message/date"),
                XhtmlMatchers.hasXPath("/message/@seen")
            )
        );
    }

    /**
     * LongMessage can convert meta-commands to HTML formatting.
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings({
        "PMD.AvoidInstantiatingObjectsInLoops", "PMD.UseConcurrentHashMap"
    })
    public void convertsMetaCommandsToHtmlFormatting() throws Exception {
        final Map<String, String> texts = ArrayUtils.toMap(
            new String[][] {
                {"**1 < 3**", "<b>1 &lt; 3</b>"},
                {"this is **bold** text", "this is <b>bold</b> text"},
                {"this is _italic_ text", "this is <i>italic</i> text"},
                {
                    "a [link](http://foo.com) here",
                    "a <a href='http://foo.com'>link</a> here",
                },
                {
                    "[\u0443\u0440\u0430!](http://a.com/\u0443)",
                    "<a href='http://a.com/&#1091;'>&#1091;&#1088;&#1072;!</a>",
                },
            }
        );
        for (Map.Entry<String, String> entry : texts.entrySet()) {
            final LongMessage msg = new LongMessage(
                // @checkstyle MultipleStringLiterals (1 line)
                new HubMocker().doReturn(entry.getKey(), "pre-render-message")
                    .mock(),
                new BoutMocker().mock(),
                new MessageMocker().withText(entry.getKey()).mock()
            );
            MatcherAssert.assertThat(
                msg.getRender(),
                Matchers.equalTo(entry.getValue())
            );
        }
    }

}
