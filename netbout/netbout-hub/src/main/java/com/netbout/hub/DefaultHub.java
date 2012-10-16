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
package com.netbout.hub;

import com.jcabi.log.Logger;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import com.netbout.bus.Bus;
import com.netbout.bus.DefaultBus;
import com.netbout.bus.TxBuilder;
import com.netbout.hh.StatsFarm;
import com.netbout.hub.cron.AbstractCron;
import com.netbout.hub.data.DefaultBoutMgr;
import com.netbout.inf.DefaultInfinity;
import com.netbout.inf.Infinity;
import com.netbout.spi.Friend;
import com.netbout.spi.Helper;
import com.netbout.spi.Identity;
import com.netbout.spi.Urn;
import com.netbout.spi.cpa.CpaHelper;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Entry point to Hub.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 * @checkstyle ClassFanOutComplexity (500 lines)
 */
@SuppressWarnings({
    "PMD.TooManyMethods",
    "PMD.DoNotUseThreads",
    "PMD.ExcessiveImports"
})
public final class DefaultHub implements PowerHub {

    /**
     * The bus.
     */
    private final transient Bus ibus;

    /**
     * Manager of bouts.
     */
    private final transient BoutMgr imanager;

    /**
     * Infinity.
     */
    private final transient Infinity inf;

    /**
     * Resolver.
     */
    private final transient UrnResolver iresolver;

    /**
     * All identities known for us at the moment.
     */
    private final transient ConcurrentMap<Urn, Identity> all =
        new ConcurrentHashMap<Urn, Identity>();

    /**
     * Cron runner.
     */
    private final transient ScheduledExecutorService service =
        Executors.newSingleThreadScheduledExecutor(
            new VerboseThreads(DefaultHub.class)
        );

    /**
     * Cron tasks running.
     */
    private final transient Collection<ScheduledFuture<?>> crons =
        new LinkedList<ScheduledFuture<?>>();

    /**
     * Public ctor.
     * @throws IOException If something wrong
     */
    public DefaultHub() throws IOException {
        this(new DefaultBus(), new DefaultInfinity());
    }

    /**
     * Public ctor, for tests mostly.
     * @param bus The bus
     * @param infinity The infinity
     * @todo #358 We expose THIS to sub-objects, which is very bad practice,
     *  mostly because they may start using it BEFORE the ctor is finished. We
     *  should introduce a start() method in this class.
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public DefaultHub(final Bus bus, final Infinity infinity) {
        StatsFarm.register(this);
        this.ibus = bus;
        this.inf = infinity;
        this.imanager = new DefaultBoutMgr(this);
        this.iresolver = new DefaultUrnResolver(this);
        this.promote(this.persister());
        for (Callable<?> task : AbstractCron.all(this)) {
            this.crons.add(
                this.service.scheduleWithFixedDelay(
                    new VerboseRunnable(task, true),
                    0L, 1L, TimeUnit.MINUTES
                )
            );
        }
        Logger.debug(
            this,
            "#DefaultHub(%[type]s): instantiated",
            bus
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        text.append(Logger.format("%d identities total\n", this.all.size()));
        text.append(this.imanager.statistics());
        text.append(this.iresolver.statistics());
        return text.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        for (ScheduledFuture<?> cron : this.crons) {
            cron.cancel(true);
        }
        this.service.shutdown();
        this.ibus.close();
        this.inf.close();
        Logger.info(this, "#close(): closed successfully");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UrnResolver resolver() {
        return this.iresolver;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TxBuilder make(final String mnemo) {
        return this.ibus.make(mnemo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BoutMgr manager() {
        return this.imanager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long eta(final Urn who) {
        return this.inf.eta(who);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Infinity infinity() {
        return this.inf;
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public Identity identity(final Urn name)
        throws Identity.UnreachableUrnException {
        this.resolver().authority(name);
        Identity identity;
        if (this.all.containsKey(name)) {
            identity = this.all.get(name);
        } else {
            identity = this.save(new HubIdentity(this, name));
            Logger.debug(
                this,
                "#identity('%s'): created new (%d total)",
                name,
                this.all.size()
            );
        }
        return identity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Helper promote(final Identity identity, final URL location) {
        if (identity instanceof Helper) {
            throw new IllegalArgumentException(
                Logger.format(
                    "Can't promote '%s' (%[type]s) since it's already a helper",
                    identity.name(),
                    identity
                )
            );
        }
        if (!(identity instanceof HubIdentity)) {
            throw new IllegalArgumentException(
                Logger.format(
                    "Can't promote '%s' (%[type]s) since it's not from Hub",
                    identity.name(),
                    identity
                )
            );
        }
        final CpaHelper helper = new CpaHelper(identity, location);
        this.ibus.register(identity, helper);
        Identity existing;
        try {
            existing = this.identity(identity.name());
        } catch (Identity.UnreachableUrnException ex) {
            throw new IllegalArgumentException(ex);
        }
        this.all.remove(existing);
        this.save(new HelperIdentity((HubIdentity) identity, helper));
        Logger.debug(
            this,
            "#promote('%s', '%[type]s'): replaced existing identity (%[type]s)",
            identity.name(),
            helper,
            existing
        );
        this.make("identity-promoted")
            .arg(identity.name())
            .arg(helper.location())
            .asDefault(true)
            .exec();
        return helper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity join(final Identity main, final Identity child) {
        final Collection<String> aliases = child.profile().aliases();
        final Urn mname = main.name();
        final Urn cname = child.name();
        synchronized (this.all) {
            this.make("identities-joined")
                .synchronously()
                .arg(mname)
                .arg(cname)
                .asDefault(true)
                .exec();
            this.all.remove(cname);
            this.manager().destroy(cname);
            this.all.remove(mname);
            this.manager().destroy(mname);
        }
        Logger.info(
            this,
            "'%s' and '%s' were joined successfully",
            mname,
            cname
        );
        Identity joined;
        try {
            joined = this.identity(mname);
        } catch (Identity.UnreachableUrnException ex) {
            throw new IllegalStateException(ex);
        }
        for (String alias : aliases) {
            joined.profile().alias(alias);
        }
        return joined;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Friend> findByKeyword(final Identity who,
        final String keyword) {
        final Set<Friend> found = new HashSet<Friend>();
        if (!keyword.isEmpty()) {
            final List<Urn> names = this
                .make("find-identities-by-keyword")
                .synchronously()
                .arg(who.name())
                .arg(keyword)
                .asDefault(new ArrayList<Urn>())
                .exec();
            for (Urn name : names) {
                try {
                    found.add(this.identity(name));
                } catch (Identity.UnreachableUrnException ex) {
                    Logger.warn(
                        this,
                        // @checkstyle LineLength (1 line)
                        "#findByKeyword('%s'): some helper returned '%s' identity that is not reachable:\n%[exception]s",
                        keyword,
                        name,
                        ex
                    );
                }
            }
        }
        return found;
    }

    /**
     * Save identity to storage.
     * @param identity The identity
     * @return The identity to use
     */
    private Identity save(final Identity identity) {
        this.all.put(identity.name(), identity);
        this.make("identity-mentioned")
            .synchronously()
            .arg(identity.name())
            .asDefault(true)
            .exec();
        return this.all.get(identity.name());
    }

    /**
     * Promote all helpers.
     * @param persister DB helper
     */
    private void promote(final Identity persister) {
        final long start = System.nanoTime();
        final List<Urn> helpers = this.make("get-all-helpers")
            .synchronously()
            .asDefault(new ArrayList<Urn>())
            .exec();
        Logger.debug(this, "#promote(): promoting %[list]s", helpers);
        for (Urn name : helpers) {
            if (name.equals(persister.name())) {
                continue;
            }
            final URL url = this.make("get-helper-url")
                .synchronously()
                .arg(name)
                .exec();
            try {
                this.promote(this.identity(name), url);
            } catch (Identity.UnreachableUrnException ex) {
                Logger.error(
                    this,
                    "#start(): failed to create '%s' identity:\n%[exception]s",
                    name,
                    ex
                );
            }
        }
        Logger.debug(
            this,
            "#promote(): done with all helpers in %[nano]s: %[list]s",
            System.nanoTime() - start,
            helpers
        );
    }

    /**
     * Create persister's identity.
     * @return The persister
     */
    private Identity persister() {
        final Identity persister;
        try {
            persister = this.identity(new Urn("netbout", "db"));
        } catch (Identity.UnreachableUrnException ex) {
            throw new IllegalStateException(
                "Failed to create starter's identity",
                ex
            );
        }
        try {
            this.promote(
                persister,
                new URL("file", "", "com.netbout.db")
            );
        } catch (java.net.MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
        return persister;
    }

}
