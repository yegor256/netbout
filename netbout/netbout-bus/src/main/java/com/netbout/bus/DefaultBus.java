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
package com.netbout.bus;

import com.netbout.bus.bh.StageFarm;
import com.netbout.bus.cache.EmptyTokenCache;
import com.netbout.spi.Helper;
import com.netbout.spi.Identity;
import com.ymock.util.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Default implementation of {@link Bus}.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class DefaultBus implements Bus {

    /**
     * Transaction controller.
     */
    private final transient TxController controller = new DefaultTxController(
        new DefaultTxQueue(),
        new EmptyTokenCache()
    );

    /**
     * Quartz scheduler.
     */
    private final transient Scheduler scheduler;

    /**
     * Public ctor.
     */
    public DefaultBus() {
        try {
            this.scheduler = StdSchedulerFactory.getDefaultScheduler();
        } catch (org.quartz.SchedulerException ex) {
            throw new IllegalStateException(ex);
        }
        StageFarm.register(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        // nothing to do yet
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TxBuilder make(final String mnemo) {
        return new DefaultTxBuilder(this.controller, mnemo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(final Identity identity, final Helper helper) {
        this.controller.register(identity, helper);
        synchronized (this.scheduler) {
            try {
                if (this.scheduler.isInStandbyMode()) {
                    this.addRoutine();
                    this.scheduler.start();
                    Logger.info(
                        this,
                        "#register(%s): Quartz started",
                        helper.location()
                    );
                }
            } catch (org.quartz.SchedulerException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String stats() {
        return this.controller.stats();
    }

    /**
     * Add routine mechanism.
     * @throws org.quartz.SchedulerException If some problem
     * @checkstyle RedundantThrows (2 lines)
     */
    private void addRoutine() throws org.quartz.SchedulerException {
        final JobDataMap data = new JobDataMap();
        data.put(DefaultBus.RoutineJob.KEY, this);
        this.scheduler.scheduleJob(
            JobBuilder
                .newJob(DefaultBus.RoutineJob.class)
                .withIdentity("routine-bus-job")
                .usingJobData(data)
                .build(),
            TriggerBuilder
                .newTrigger()
                .startNow()
                .withSchedule(
                    SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(1)
                        .repeatForever()
                )
                .build()
        );
    }

    /**
     * Routine job.
     */
    @DisallowConcurrentExecution
    public static final class RoutineJob implements Job {
        /**
         * Key in context.
         */
        public static final String KEY = "bus";
        /**
         * {@inheritDoc}
         */
        @Override
        public void execute(final JobExecutionContext context) {
            final long start = System.currentTimeMillis();
            final Bus bus = (Bus) context.getMergedJobDataMap().get(this.KEY);
            bus.make("routine").asDefault(false).exec();
            Logger.debug(
                this,
                "#execute(%[type]s): routine job done in %dms",
                context,
                System.currentTimeMillis() - start
            );
        }
    }

}
