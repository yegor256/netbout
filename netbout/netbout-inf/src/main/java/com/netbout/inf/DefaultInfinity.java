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
package com.netbout.inf;

import com.jcabi.log.Logger;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import com.jcabi.urn.URN;
import com.netbout.ih.StageFarm;
import com.netbout.inf.functors.DefaultStore;
import com.netbout.inf.ray.MemRay;
import com.netbout.spi.Query;
import com.rexsl.core.Manifests;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;

/**
 * Default implementation of Infitity.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.DoNotUseThreads")
public final class DefaultInfinity implements Infinity {

    /**
     * Multiplexer of tasks.
     */
    private final transient Mux mux;

    /**
     * The folder to work with.
     */
    private final transient Folder folder;

    /**
     * Store of functors.
     */
    private final transient Store store = new DefaultStore();

    /**
     * Ray of messages.
     */
    private final transient Ray ray;

    /**
     * Running service.
     */
    private final transient ScheduledExecutorService service =
        Executors.newSingleThreadScheduledExecutor(new VerboseThreads("inf"));

    /**
     * Public ctor.
     * @throws IOException If some IO problem
     */
    public DefaultInfinity() throws IOException {
        this(new NfsFolder(new File(Manifests.read("Netbout-InfMount"))));
    }

    /**
     * Protect ctor, for tests.
     * @param fldr The folder
     * @throws IOException If some IO problem
     */
    protected DefaultInfinity(final Folder fldr) throws IOException {
        this.folder = fldr;
        this.ray = new MemRay(new File(this.folder.path(), "ray"));
        this.mux = new Mux(this.ray, this.store);
        this.service.scheduleWithFixedDelay(
            new VerboseRunnable(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (!DefaultInfinity.this.folder.isWritable()
                                && DefaultInfinity.this.mux.eta() == 0L) {
                                DefaultInfinity.this.folder.close();
                            }
                        } catch (IOException ex) {
                            throw new IllegalArgumentException(ex);
                        }
                    }
                },
                true
            ),
            1L, 1L, TimeUnit.SECONDS
        );
        StageFarm.register(this);
        Logger.info(
            this,
            "#DefaultInfinity(%s): instantiated (max=%d)",
            this.folder.path(),
            this.maximum()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Logger.format(
            // @checkstyle LineLength (3 lines)
            // @checkstyle StringLiteralsConcatenation (2 lines)
            "maximum(): %d\n%[type]s:\n%s\n%[type]s:\n%s\n%[type]s:\n%s\n%[type]s:\n%s\n"
            + "Runtime:\n  availableProcessors(): %d\n  freeMemory(): %s\n  maxMemory(): %s\n  totalMemory(): %s",
            this.maximum(),
            this.mux,
            this.mux,
            this.store,
            this.store,
            this.folder,
            this.folder,
            this.ray,
            this.ray,
            Runtime.getRuntime().availableProcessors(),
            FileUtils.byteCountToDisplaySize(
                Runtime.getRuntime().freeMemory()
            ),
            FileUtils.byteCountToDisplaySize(
                Runtime.getRuntime().maxMemory()
            ),
            FileUtils.byteCountToDisplaySize(
                Runtime.getRuntime().totalMemory()
            )
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.mux.close();
        this.ray.close();
        this.folder.close();
        Logger.info(this, "#close(): closed");
    }

    /**
     * {@inheritDoc}
     *
     * <p>We should return ONE in case the Mux is not yet ready and we don't
     * have any tasks there and no data is in INF. It means that the Infinity
     * hasn't been intialized yet.
     */
    @Override
    public long eta(final URN... who) {
        long eta = this.mux.eta(who);
        if (eta == 0 && this.maximum() == 0) {
            eta = 1;
        }
        return eta;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Long> messages(final Query query)
        throws InvalidSyntaxException {
        final Term term = new ParserAdapter(this.store)
            .parse(query)
            .term(this.ray);
        Logger.debug(this, "#messages('%[text]s'): term '%s'", query, term);
        return new LazyMessages(this.ray, term);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long maximum() {
        return this.ray.maximum();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<URN> see(final Notice notice) {
        try {
            if (!this.folder.isWritable()) {
                throw new IllegalStateException("Folder is not writable");
            }
            return this.mux.add(notice);
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

}
