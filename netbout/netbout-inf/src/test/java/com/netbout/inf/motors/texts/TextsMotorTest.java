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
package com.netbout.inf.motors.texts;

import com.netbout.inf.Atom;
import com.netbout.inf.FolderMocker;
import com.netbout.inf.Predicate;
import com.netbout.inf.PredicateStore;
import com.netbout.inf.atoms.TextAtom;
import com.netbout.inf.atoms.VariableAtom;
import com.netbout.spi.Message;
import com.netbout.spi.MessageMocker;
import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import org.apache.commons.lang.ArrayUtils;
import org.hamcrest.MatcherAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test case of {@link TextsMotor}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings({
    "PMD.UseConcurrentHashMap", "PMD.AvoidInstantiatingObjectsInLoops"
})
public final class TextsMotorTest {

    /**
     * Temporary folder.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public transient TemporaryFolder dir = new TemporaryFolder();

    /**
     * TextsMotor can match empty text.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void positivelyMatchesEmptyText() throws Exception {
        final Long number = new Random().nextLong();
        final Message message = new MessageMocker()
            .withNumber(number)
            .withText("hello, dude!")
            .mock();
        final File path = this.dir.newFolder("f1");
        final TextsMotor motor = new TextsMotor(path);
        motor.see(message);
        motor.setStore(
            new PredicateStore(new FolderMocker().withPath(path).mock())
        );
        final Predicate pred = motor.build(
            "",
            Arrays.asList(
                new Atom[] {
                    new TextAtom("  "),
                    VariableAtom.TEXT,
                }
            )
        );
        MatcherAssert.assertThat("matched", pred.contains(number));
        MatcherAssert.assertThat("is empty", !pred.hasNext());
    }

    /**
     * TextsMotor can match by keyword or a combination of them.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void positivelyMatchesByKeywords() throws Exception {
        final Map<String, String> matches = ArrayUtils.toMap(
            new String[][] {
                {"", ""},
                {"", "hello dear friend, how are you?"},
                {"up?", "hi there, what's up"},
                {"any time", "You can call me any time, really!"},
                {"call", "Call me when you can"},
                {"jeff lebowski", "the dude is Jeff Bridges (Lebowski)"},
            }
        );
        final File path = this.dir.newFolder("f2");
        final TextsMotor motor = new TextsMotor(path);
        motor.setStore(
            new PredicateStore(new FolderMocker().withPath(path).mock())
        );
        for (Map.Entry<String, String> entry : matches.entrySet()) {
            final Long number = new Random().nextLong();
            final Message message = new MessageMocker()
                .withNumber(number)
                .withText(entry.getValue())
                .mock();
            motor.see(message);
            final Predicate pred = motor.build(
                "",
                Arrays.asList(
                    new Atom[] {
                        new TextAtom(entry.getKey()),
                        VariableAtom.TEXT,
                    }
                )
            );
            MatcherAssert.assertThat(
                String.format(
                    "matches '%s' in '%s' as expected",
                    entry.getKey(),
                    entry.getValue()
                ),
                pred.contains(number)
            );
        }
    }

    /**
     * TextsMotor can avoid matching when it's not necessary.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void doesntMatchWhenItShouldnt() throws Exception {
        final Map<String, String> matches = ArrayUtils.toMap(
            new String[][] {
                {"boy", "short story about some girls"},
            }
        );
        final File path = this.dir.newFolder("f3");
        final TextsMotor motor = new TextsMotor(path);
        motor.setStore(
            new PredicateStore(new FolderMocker().withPath(path).mock())
        );
        for (Map.Entry<String, String> entry : matches.entrySet()) {
            final Long number = new Random().nextLong();
            final Message message = new MessageMocker()
                .withNumber(number)
                .withText(entry.getValue())
                .mock();
            motor.see(message);
            final Predicate pred = motor.build(
                "",
                Arrays.asList(
                    new Atom[] {
                        new TextAtom(entry.getKey()),
                        VariableAtom.TEXT,
                    }
                )
            );
            MatcherAssert.assertThat(
                String.format(
                    "doesn't match '%s' in '%s' as expected",
                    entry.getKey(),
                    entry.getValue()
                ),
                !pred.contains(number)
            );
        }
    }

}
