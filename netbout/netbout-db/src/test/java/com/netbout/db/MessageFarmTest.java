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
package com.netbout.db;

import java.util.Date;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case of {@link MessageFarm}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class MessageFarmTest {

    /**
     * Farm to work with.
     */
    private final MessageFarm farm = new MessageFarm();

    /**
     * Bout number to work with.
     */
    private Long bout;

    /**
     * Start new bout to work with.
     * @throws Exception If there is some problem inside
     */
    @Before
    public void prepareNewBout() throws Exception {
        final BoutFarm bfarm = new BoutFarm();
        this.bout = bfarm.getNextBoutNumber();
        bfarm.startedNewBout(bout);
    }

    /**
     * Add new message to a bout and retrieve it back.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testAddMessageAndRetrieveItBack() throws Exception {
        final Long number = this.farm.createBoutMessage(this.bout);
        this.farm.changedMessageDate(number, new Date());
        final Long[] nums = this.farm.getBoutMessages(this.bout);
        MatcherAssert.assertThat(
            nums,
            Matchers.hasItemInArray(number)
        );
    }

    /**
     * Set and change message author.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testChangeMessageAuthor() throws Exception {
        final Long number = this.farm.createBoutMessage(this.bout);
        this.farm.changedMessageDate(number, new Date());
        final String author = "Jeff Bridges";
        this.farm.changedMessageAuthor(number, author);
        MatcherAssert.assertThat(
            this.farm.getMessageAuthor(number),
            Matchers.equalTo(author)
        );
    }

    /**
     * Set and change message text.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testChangeMessageText() throws Exception {
        final Long number = this.farm.createBoutMessage(this.bout);
        this.farm.changedMessageDate(number, new Date());
        final String text = "hello, dude! :)";
        this.farm.changedMessageText(number, text);
        MatcherAssert.assertThat(
            this.farm.getMessageText(number),
            Matchers.equalTo(text)
        );
    }

}
