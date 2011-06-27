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

import com.netbout.engine.Identity;
import com.netbout.engine.User;
import java.util.ArrayList;
import java.util.List;
import org.junit.*;
import org.xmlmatchers.transform.XmlConverters;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class PageStartTest {

    private static final String IDENTITY = "Peter Smith";

    @Test
    public void testSimpleJaxbMarshalling() throws Exception {
        final User user = mock(User.class);
        final List<Identity> identities = new ArrayList<Identity>();
        final Identity identity = mock(Identity.class);
        doReturn(this.IDENTITY).when(identity).name();
        identities.add(identity);
        doReturn(identities).when(user).identities();
        final PageStart page = new PageStart(user);
        final String xml = new ObjectMarshaller().marshall(page);
        assertThat(
            XmlConverters.the(xml),
            org.xmlmatchers.XmlMatchers.hasXPath(
                "/page/identities/identity/name[text() = '"
                + this.IDENTITY + "']"
            )
        );
        assertThat(
            XmlConverters.the(xml),
            org.xmlmatchers.XmlMatchers.hasXPath(
                "/page/identities[count(identity) = 1]"
            )
        );
    }

    @Test(expected = IllegalStateException.class)
    public void testDefaultClassInstantiation() throws Exception {
        new PageStart();
    }

}
