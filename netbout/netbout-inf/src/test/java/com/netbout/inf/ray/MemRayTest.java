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
package com.netbout.inf.ray;

import com.netbout.inf.Attribute;
import com.netbout.inf.AttributeMocker;
import com.netbout.inf.Cursor;
import com.netbout.inf.FolderMocker;
import com.netbout.inf.MsgMocker;
import com.netbout.inf.Ray;
import java.io.File;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link MemRay}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class MemRayTest {

    /**
     * MemRay can store and find.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void storesAndFinds() throws Exception {
        final Ray ray = new MemRay(new FolderMocker().mock().path());
        final long number = MsgMocker.number();
        final Attribute attribute = AttributeMocker.reversive();
        final String value = "some value to set, \u0433!";
        ray.msg(1L);
        ray.cursor().add(
            ray.builder().picker(number),
            attribute,
            value
        );
        final Cursor cursor = ray.cursor().shift(
            ray.builder().matcher(attribute, value)
        );
        MatcherAssert.assertThat(cursor.end(), Matchers.is(false));
        MatcherAssert.assertThat(
            cursor.msg().number(),
            Matchers.equalTo(number)
        );
        MatcherAssert.assertThat(
            cursor.msg().attr(attribute),
            Matchers.equalTo(value)
        );
        ray.close();
    }

    /**
     * MemRay can persist itself and restore.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void persistsItselfInFile() throws Exception {
        final File dir = new FolderMocker().mock().path();
        final Ray ray = new MemRay(dir);
        ray.msg(1L);
        ray.msg(2L);
        ray.cursor().add(
            ray.builder().picker(2L),
            new Attribute("title"),
            "How are you, \u0434\u0440\u0443\u0433?"
        );
        ray.flush();
        MatcherAssert.assertThat(
            dir.list(),
            Matchers.notNullValue()
        );
        ray.close();
    }

    /**
     * MemRay can convert itself to string.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void convertsItselfToString() throws Exception {
        final File dir = new FolderMocker().mock().path();
        final Ray ray = new MemRay(dir);
        final long number = MsgMocker.number();
        ray.msg(number);
        ray.cursor().add(
            ray.builder().picker(number),
            new Attribute("some-attribute-5"),
            "How are you, dude? \u0434\u0440\u0443\u0433!"
        );
        MatcherAssert.assertThat(
            ray,
            Matchers.hasToString(Matchers.notNullValue())
        );
    }

}
