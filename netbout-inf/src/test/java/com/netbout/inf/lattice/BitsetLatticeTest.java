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
 * this code accidentally and without intent to use it, please report this
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
package com.netbout.inf.lattice;

import com.netbout.inf.Cursor;
import com.netbout.inf.CursorMocker;
import com.netbout.inf.Lattice;
import com.rexsl.test.SimpleXml;
import com.rexsl.test.XmlDocument;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link BitsetLattice}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle MagicNumber (500 lines)
 */
public final class BitsetLatticeTest {

    /**
     * BitsetLattice can shift a cursor to the right position.
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings({ "unchecked", "PMD.AvoidInstantiatingObjectsInLoops" })
    public void shiftsCursorToTheRightPosition() throws Exception {
        final XmlDocument xml = new SimpleXml(
            this.getClass().getResourceAsStream("numbers.xml")
        );
        for (XmlDocument test : xml.nodes("/numbers/test")) {
            Lattice lattice = BitsetLatticeTest.lattice(
                test.xpath("numbers/text()").get(0)
            );
            if (!test.xpath("numbers/@reverse").isEmpty()) {
                lattice = new LatticeBuilder().copy(lattice).revert().build();
            }
            for (XmlDocument asrt : test.nodes("asserts/assert")) {
                final Method method = BitsetLatticeTest.matcher(
                    asrt.xpath("matcher/text()").get(0)
                );
                MatcherAssert.assertThat(
                    BitsetLatticeTest.corrected(
                        lattice,
                        Long.valueOf(asrt.xpath("cursor/text()").get(0))
                    ),
                    Matcher.class.cast(
                        method.invoke(
                            null,
                            Long.valueOf(asrt.xpath("value/text()").get(0))
                        )
                    )
                );
            }
        }
    }

    /**
     * Create lattice with these numbers.
     * @param text List of numbers, in any order, comma-separated
     * @return Lattice
     */
    private static Lattice lattice(final String text) {
        final String[] parts = text.trim().split("\\s*,\\s*");
        final SortedSet<Long> numbers =
            new TreeSet<Long>(Collections.reverseOrder());
        for (int pos = 0; pos < parts.length; ++pos) {
            if (parts[pos].matches("\\d+-\\d+")) {
                final String[] range = parts[pos].split("-", 2);
                for (long msg = Long.valueOf(range[0]);
                    msg > Long.valueOf(range[1]); --msg) {
                    numbers.add(msg);
                }
            } else {
                numbers.add(Long.valueOf(parts[pos]));
            }
        }
        final Iterator<Long> iterator = numbers.iterator();
        return new LatticeBuilder().fill(
            new Feeder() {
                @Override
                public long next() {
                    long next;
                    if (iterator.hasNext()) {
                        next = iterator.next();
                    } else {
                        next = 0L;
                    }
                    return next;
                }
            }
        ).build();
    }

    /**
     * Shift from the number through lattice and return the resulted
     * message number.
     * @param lattice The lattice to use for correction
     * @param from Message num to correct
     * @return Corrected number
     */
    private static long corrected(final Lattice lattice, final long from) {
        final AtomicLong msg = new AtomicLong(from);
        lattice.correct(
            new CursorMocker().withMsg(from).mock(),
            new Lattice.Shifter() {
                @Override
                public Cursor shift(final Cursor cursor, final long num) {
                    msg.set(num);
                    return cursor;
                }
            }
        );
        return msg.get();
    }

    /**
     * Create matcher by name.
     * @param name The name of it
     * @return Method of {@link Matchers}
     */
    private static Method matcher(final String name) {
        Method matcher = null;
        for (Method method : Matchers.class.getDeclaredMethods()) {
            if (method.getName().equals(name)) {
                matcher = method;
                break;
            }
        }
        return matcher;
    }

}
