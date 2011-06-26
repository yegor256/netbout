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
 * incident to the author by email: privacy@netbout.com.
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

import com.netbout.engine.Bout;
import com.netbout.engine.BoutFactory;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.junit.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.xmlmatchers.transform.XmlConverters.the;

/**
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class PageWithBoutsTest {

    private static final String QUERY = "all";

    private static final Long BOUT_ID = 7464L;

    private static final String BOUT_TITLE = "some text";

    @Test
    public void testSimpleJaxbMarshalling() throws Exception {
        final JAXBContext ctx =
            JAXBContext.newInstance("com.netbout.rest.jaxb");
        final Marshaller mrsh = ctx.createMarshaller();
        mrsh.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        final BoutFactory factory = mock(BoutFactory.class);
        final List<Bout> list = new ArrayList<Bout>();
        final Bout bout = mock(Bout.class);
        doReturn(this.BOUT_TITLE).when(bout).title();
        doReturn(this.BOUT_ID).when(bout).number();
        list.add(bout);
        doReturn(list).when(factory).list(this.QUERY);
        final PageWithBouts page = new PageWithBouts(factory, this.QUERY);
        final StringWriter writer = new StringWriter();
        mrsh.marshal(page, writer);
        final String xml = writer.toString();
        assertThat(
            the(xml),
            org.xmlmatchers.XmlMatchers.hasXPath(
                "/page/bouts/bout/number[text() = '"
                + this.BOUT_ID + "']"
            )
        );
        assertThat(
            the(xml),
            org.xmlmatchers.XmlMatchers.hasXPath("/page/bouts[count(bout) = 1]")
        );
        // assertThat(
        //     the(xml),
        //     org.xmlmatchers.XmlMatchers.hasXPath(
        //         "/processing-instruction('xml-stylesheet')[@href]"
        //     )
        // );
    }

}
