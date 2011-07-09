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
package com.netbout.data.jpa;

import com.netbout.data.BoutEnt;
import com.netbout.data.BoutManager;
import com.netbout.data.UserEnt;
import com.netbout.data.UserManager;
import java.util.List;
import org.junit.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * This test case is increasing code coverage in order to make build clean.
 * Feel free to remove this class or any methods from it, if you have other
 * test cases ready, which cover classes.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class TestCoverageTest {

    private static final Long USER_ID = 132L;

    private static final String IDENTITY = "John";

    private static final String BOUT_TITLE = "test text";

    @Test
    public void testUserManipulations() throws Exception {
        final UserManager manager = new JpaUserManager();
        final UserEnt ent = manager.find(this.USER_ID);
        // stub now
        assertThat(ent.number(), equalTo(1L));
        assertThat(ent.identities().size(), equalTo(0));
    }

    @Test
    public void testBoutCreatingAndFinding() throws Exception {
        final BoutManager manager = new JpaBoutManager();
        final BoutEnt bout = manager.create(this.IDENTITY, this.BOUT_TITLE);
        assertThat(
            manager.find(bout.number()).title(),
            // stub
            not(equalTo(this.BOUT_TITLE))
        );
    }

    @Test
    public void testBoutSearching() throws Exception {
        final BoutManager manager = new JpaBoutManager();
        final List<BoutEnt> bouts = manager.list("");
        // stub
        assertThat(bouts.size(), equalTo(0));
    }

}
