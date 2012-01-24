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
package com.netbout.shary;

import com.netbout.spi.xml.JaxbParser;
import com.netbout.spi.xml.JaxbPrinter;
import java.util.Map;
import org.apache.commons.lang.ArrayUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Slip}.
 * @author Yegor Bugayenko (yegor@woquo.com)
 * @version $Id$
 */
public final class SlipTest {

    /**
     * Slip can be converted to XML.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void marshallsToXml() throws Exception {
        final Slip slip = new Slip(true, "uri", "author", "name");
        final String xml = new JaxbPrinter(slip).print();
        MatcherAssert.assertThat(
            xml,
            Matchers.allOf(
                Matchers.containsString("<Slip"),
                Matchers.containsString(Slip.NAMESPACE)
            )
        );
    }

    /**
     * Slip can be unmarshalled from XML.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void unmarshallsFromXml() throws Exception {
        final Slip slip = new JaxbParser(
            String.format(
                "<Slip xmlns='%s'><allow>true</allow></Slip>",
                Slip.NAMESPACE
            )
        ).parse(Slip.class);
        MatcherAssert.assertThat(
            slip,
            Matchers.allOf(
                Matchers.hasProperty("allow", Matchers.equalTo(true))
            )
        );
    }

    /**
     * Slip can understand media type of the URI.
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings({
        "PMD.UseConcurrentHashMap", "PMD.AvoidInstantiatingObjectsInLoops"
    })
    public void understandsMediaTypeOfUri() throws Exception {
        final Map<String, String> types = ArrayUtils.toMap(
            new String[][] {
                {"test.pdf", "application/pdf"},
                {"http://localhost/test.txt", "text/plain"},
                {"s3:/abc.cde/myfile.zip", "application/x-zip-compressed"},
                // {"ftp://example.com/data.mp3", "audio/mpeg"},
                {"http://google.com/image.png", "image/png"},
                {"http://example.com/test.doc", "application/msword"},
                {"http://example.com/test.xls", "application/excel"},
                // { "http://example.com/test.mp4", "video/mp4"},
                {"http://example.com/test.mpeg", "video/mpeg"},
                {"http://example.com/test", "application/octet-stream"},
            }
        );
        for (Map.Entry<String, String> entry : types.entrySet()) {
            MatcherAssert.assertThat(
                new Slip(true, entry.getKey(), "urn:test:john", "document"),
                Matchers.hasProperty("type", Matchers.equalTo(entry.getValue()))
            );
        }
    }

}
