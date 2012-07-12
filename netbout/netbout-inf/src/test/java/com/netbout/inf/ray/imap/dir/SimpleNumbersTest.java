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
package com.netbout.inf.ray.imap.dir;

import com.netbout.inf.MsgMocker;
import com.netbout.inf.ray.imap.Numbers;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link SimpleNumbers}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class SimpleNumbersTest {

    /**
     * SimpleNumbers can save to stream and restore.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void savesAndRestores() throws Exception {
        final Numbers numbers = new SimpleNumbers();
        final long msg = MsgMocker.number();
        numbers.add(msg);
        numbers.add(msg - 1);
        MatcherAssert.assertThat(numbers.next(msg), Matchers.equalTo(msg - 1));
        final ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        numbers.save(ostream);
        final byte[] data = ostream.toByteArray();
        final Numbers restored = new SimpleNumbers();
        final InputStream istream = new ByteArrayInputStream(data);
        restored.load(istream);
        MatcherAssert.assertThat(restored.next(msg), Matchers.equalTo(msg - 1));
        MatcherAssert.assertThat(restored.next(msg - 1), Matchers.equalTo(0L));
    }

}
