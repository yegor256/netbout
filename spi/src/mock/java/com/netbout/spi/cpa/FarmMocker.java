/**
 * Copyright (c) 2009-2011, NetBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the NetBout.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netbout.spi.cpa;

import com.netbout.spi.Identity;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

/**
 * Sample farm.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Farm
final class FarmMocker implements IdentityAware {

    /**
     * Mock this farm.
     * @return The URL for a helper
     * @throws java.net.MalformedURLException If some problem inside
     */
    public URL mock() throws java.net.MalformedURLException {
        return new URL(
            "file",
            "localhost",
            this.getClass().getPackage().getName()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final Identity identity) {
        MatcherAssert.assertThat(identity, Matchers.notNullValue());
    }

    /**
     * Sample operation.
     * @param text The text to translate
     * @param len Length to compare with
     * @return The translated text
     */
    @Operation("comparison")
    public Boolean longerThan(final String text, final Long len) {
        return text.length() > len;
    }

    /**
     * Empty operation with no result and no args.
     */
    @Operation("empty")
    public void empty() {
        // intentionally empty
    }

    /**
     * List as output.
     * @return The list just created
     */
    @Operation("list")
    public List<Long> list() {
        final List<Long> list = new ArrayList<Long>();
        final Random random = new Random();
        for (int pos = 0; pos < 2; pos += 1) {
            list.add(random.nextLong());
        }
        return list;
    }

    /**
     * List of texts.
     * @return The list just created
     */
    @Operation("texts")
    public List<String> texts() {
        final List<String> list = new ArrayList<String>();
        // @checkstyle MultipleStringLiterals (1 line)
        list.add("o n e");
        list.add("\"two\"");
        return list;
    }

    /**
     * Return back the same message as being sent.
     * @param msg The message
     * @return The same message
     */
    @Operation("echo")
    public String echo(final String msg) {
        return msg;
    }

}
