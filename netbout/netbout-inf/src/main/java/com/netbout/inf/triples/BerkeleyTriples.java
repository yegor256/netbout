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
package com.netbout.inf.triples;

import com.netbout.spi.Message;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.ymock.util.Logger;
import java.io.Closeable;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang.CharEncoding;

/**
 * Triples with Berkeley DB.
 *
 * <p>The class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class BerkeleyTriples implements Triples {

    /**
     * The environment.
     */
    private final transient Environment env;

    /**
     * The databases (by names).
     */
    private final transient ConcurrentMap<String, Database> databases =
        new ConcurrentHashMap<String, Database>();

    /**
     * Public ctor.
     * @param dir Where to keep data
     */
    public BerkeleyTriples(final File dir) {
        final EnvironmentConfig config = new EnvironmentConfig();
        config.setAllowCreate(true);
        this.env = new Environment(dir, config);
        Logger.debug(this, "#BerkeleyTriples(%s): instantiated", dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws java.io.IOException {
        final Collection<String> names = new LinkedList<String>();
        for (Database database : this.databases.values()) {
            names.add(database.getDatabaseName());
            database.close();
        }
        final File home = this.env.getHome();
        this.env.close();
        Logger.debug(
            this,
            "#close(): closed %s with %[list]s",
            home,
            names
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> void put(final Long number, final String name, final T value) {
        this.database(name).put(null, this.key(number), this.data(value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> boolean has(final Long number, final String name,
        final T value) {
        return this.database(name).getSearchBoth(
            null,
            this.key(number),
            this.data(value),
            LockMode.DEFAULT
        ) == OperationStatus.SUCCESS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(final Long number, final String name)
        throws MissedTripleException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Iterator<T> all(final Long number, final String name) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Iterator<Long> reverse(final String name, final T value) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Iterator<Long> reverse(final String name,
        final Iterator<T> values) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear(final Long number, final String name) {
    }

    /**
     * Get database by name.
     * @param name The name
     * @return The database
     */
    private Database database(final String name) {
        synchronized (this.databases) {
            if (!this.databases.containsKey(name)) {
                final DatabaseConfig config = new DatabaseConfig();
                config.setAllowCreate(true);
                config.setDeferredWrite(true);
                this.databases.put(
                    name,
                    env.openDatabase(null, name, config)
                );
                Logger.debug(this, "#database('%s'): opened", name);
            }
        }
        return this.databases.get(name);
    }

    /**
     * Create key.
     * @param number The number
     * @return The key
     */
    private DatabaseEntry key(final Long number) {
        final DatabaseEntry key = new DatabaseEntry();
        TupleBinding.getPrimitiveBinding(Long.class).objectToEntry(number, key);
        return key;
    }

    /**
     * Create data.
     * @param value The value
     * @return The data
     */
    public <T> DatabaseEntry data(final T value) {
        DatabaseEntry data;
        try {
            data = new DatabaseEntry(
                value.toString().getBytes(CharEncoding.UTF_8)
            );
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
        return data;
    }

}
