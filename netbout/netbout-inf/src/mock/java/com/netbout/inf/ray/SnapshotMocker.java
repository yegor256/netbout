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

import com.jcabi.log.Logger;
import com.netbout.inf.atoms.VariableAtom;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Mocker of {@link Snapshot}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class SnapshotMocker {

    /**
     * The randomizer.
     */
    private final transient Random random = new SecureRandom();

    /**
     * The directory.
     */
    private final transient File directory;

    /**
     * The object.
     */
    private final transient Snapshot snapshot;

    /**
     * Total number of messages.
     */
    private transient int maximum;

    /**
     * Public ctor.
     * @param dir The directory to work with
     * @throws IOException If something wrong inside
     */
    public SnapshotMocker(final File dir) throws IOException {
        this.snapshot = new Snapshot(dir, "A1B2C3");
        this.directory = dir;
    }

    /**
     * Total amount of messages to have there.
     * @param max Total number of messages
     * @return This object
     * @throws IOException If something wrong inside
     */
    public SnapshotMocker withMaximum(final int max) throws IOException {
        this.maximum = max;
        final File file = this.snapshot.attr(VariableAtom.NUMBER.attribute());
        final PrintWriter writer = new PrintWriter(new FileWriter(file));
        final PrintWriter map = new PrintWriter(
            new FileWriter(this.snapshot.map())
        );
        for (int pos = 1; pos <= this.maximum; ++pos) {
            writer.print(pos);
            writer.print('\n');
            writer.print(' ');
            writer.print(pos);
            writer.print('\n');
            map.print(pos);
            map.print('\n');
        }
        writer.close();
        map.close();
        Logger.info(this, "#withMaximum(): %s (%d bytes)", file, file.length());
        return this;
    }

    /**
     * Total amount of bouts to have there.
     * @param bouts Total number of bouts
     * @param max Maximum number of messages per bout
     * @return This object
     * @throws IOException If something wrong inside
     */
    public SnapshotMocker withBouts(final int bouts, final int max)
        throws IOException {
        final File file = this.snapshot.attr(
            VariableAtom.BOUT_NUMBER.attribute()
        );
        final PrintWriter writer = new PrintWriter(new FileWriter(file));
        final List<Long> msgs = new ArrayList<Long>(this.maximum);
        for (long pos = 1; pos <= this.maximum; ++pos) {
            msgs.add(pos);
        }
        Collections.shuffle(msgs);
        for (int pos = 1; pos <= bouts; ++pos) {
            writer.print(pos);
            writer.print('\n');
            for (int msg = 1; msg <= max; ++msg) {
                writer.print(' ');
                if (msgs.isEmpty()) {
                    writer.print(this.random.nextInt(this.maximum) + 1);
                } else {
                    writer.print(msgs.get(0));
                    msgs.remove(0);
                }
                writer.print('\n');
            }
        }
        writer.close();
        Logger.info(this, "#withBouts(): %s (%d bytes)", file, file.length());
        return this;
    }

    /**
     * With this attribute.
     * @param name Name of attribute
     * @param prefix The prefix for values
     * @param num Number of letters to add to prefix
     * @return This object
     * @throws IOException If something wrong inside
     */
    public SnapshotMocker withAttr(final String name, final String prefix,
        final int num) throws IOException {
        final File file = this.snapshot.attr(name);
        final PrintWriter writer = new PrintWriter(new FileWriter(file));
        final List<Long> msgs = new ArrayList<Long>(this.maximum);
        for (long pos = 1; pos <= this.maximum; ++pos) {
            msgs.add(pos);
        }
        Collections.shuffle(msgs);
        for (int pos = 1; pos <= num; ++pos) {
            writer.print(prefix);
            writer.print(pos);
            writer.print('\n');
            for (int msg = 1;
                msg <= this.maximum / (this.random.nextInt(num) + 1); ++msg) {
                writer.print(' ');
                if (msgs.isEmpty()) {
                    writer.print(this.random.nextInt(this.maximum) + 1);
                } else {
                    writer.print(msgs.get(0));
                    msgs.remove(0);
                }
                writer.print('\n');
            }
        }
        writer.close();
        Logger.info(
            this,
            "#withAttr(%s): %s (%d bytes)",
            name,
            file,
            file.length()
        );
        return this;
    }

    /**
     * Build it.
     * @return The snapshot
     * @throws IOException If something wrong inside
     */
    public Snapshot mock() throws IOException {
        new Files(this.directory).publish(this.snapshot);
        return this.snapshot;
    }

}
