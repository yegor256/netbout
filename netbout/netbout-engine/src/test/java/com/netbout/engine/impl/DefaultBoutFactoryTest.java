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
package com.netbout.engine.impl;

import com.netbout.data.BoutEnt;
import com.netbout.data.BoutManager;
import com.netbout.engine.Bout;
import com.netbout.engine.BoutFactory;
import com.netbout.engine.Identity;
import org.junit.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class DefaultBoutFactoryTest {

    private static final Long BOUT_ID = 543L;

    private static final String BOUT_TITLE = "some title";

    private static final String IDENTITY_NAME = "John Doe";

    @Test
    public void testSimpleBoutFinding() throws Exception {
        final BoutEnt entity = mock(BoutEnt.class);
        doReturn(this.BOUT_TITLE).when(entity).title();
        final BoutManager manager = mock(BoutManager.class);
        doReturn(entity).when(manager).find(this.BOUT_ID);
        final BoutFactory factory = new DefaultBoutFactory(manager);
        final Bout found = factory.find(this.BOUT_ID);
        assertThat(
            found.title(),
            equalTo(this.BOUT_TITLE)
        );
        verify(manager).find(this.BOUT_ID);
    }

    @Test
    public void testDefaultClassInstantiating() throws Exception {
        final BoutFactory factory = new DefaultBoutFactory();
        assertThat(
            factory,
            instanceOf(BoutFactory.class)
        );
    }

    @Test
    public void testBoutCreatingMechanism() throws Exception {
        final BoutEnt entity = mock(BoutEnt.class);
        doReturn(this.BOUT_TITLE).when(entity).title();
        doReturn(this.BOUT_ID).when(entity).number();
        final BoutManager manager = mock(BoutManager.class);
        doReturn(entity).when(manager)
            .create(this.IDENTITY_NAME, this.BOUT_TITLE);
        doReturn(entity).when(manager).find(this.BOUT_ID);
        final Identity creator = mock(Identity.class);
        doReturn(this.IDENTITY_NAME).when(creator).name();
        final BoutFactory factory = new DefaultBoutFactory(manager);
        final Bout created = factory.create(creator, this.BOUT_TITLE);
        verify(manager).create(this.IDENTITY_NAME, this.BOUT_TITLE);
        assertThat(
            created.title(),
            equalTo(this.BOUT_TITLE)
        );
    }

}
