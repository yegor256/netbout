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

import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryCursor;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;
import com.ymock.util.Logger;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Triples with Berkeley DB.
 *
 * <p>The class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @todo #358 This class doesn't work because of a limitation in Berkeley DB.
 *  They don't allow secondary databases to be used when primary database
 *  allows duplicates. It's strange, but this is how it is now in version 4.0.x
 *  of Berkeley DB. Maybe in version 5.0 it is fixed.
 *  http://stackoverflow.com/questions/10095199
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class BerkeleyTriples implements Triples {

    /**
     * Name of meta table.
     */
    private static final String META_TABLE = "-meta-info";

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
     * Cursors with their open times (in millis).
     */
    private final transient ConcurrentMap<Long, Cursor> cursors =
        new ConcurrentHashMap<Long, Cursor>();

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
        for (Cursor cursor : this.cursors.values()) {
            cursor.close();
        }
        final Collection<String> names = new LinkedList<String>();
        for (Database database : this.databases.values()) {
            names.add(database.getDatabaseName());
            database.close();
        }
        final File home = this.env.getHome();
        this.env.close();
        Logger.debug(
            this,
            "#close(): closed %s with %[list]s and %d cursor(s)",
            home,
            names,
            this.cursors.size()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> void put(final Long number, final String name, final T value) {
        this.database(name).put(null, this.key(number), this.entry(value));
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
            this.entry(value),
            null
        ) == OperationStatus.SUCCESS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(final Long number, final String name)
        throws MissedTripleException {
        final DatabaseEntry entry = new DatabaseEntry();
        if (this.database(name)
            .get(null, this.key(number), entry, null)
                != OperationStatus.SUCCESS) {
            throw new MissedTripleException(
                String.format("Number %d not found in '%s'", number, name)
            );
        }
        return this.<T>value(entry);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Iterator<T> all(final Long number, final String name) {
        final Cursor cursor = this.cursor(this.database(name));
        final DatabaseEntry key = this.key(number);
        return new AbstractIterator<T>() {
            @Override
            public T fetch() {
                final DatabaseEntry entry = new DatabaseEntry();
                T value = null;
                final OperationStatus status = cursor.getNext(key, entry, null);
                if (status == OperationStatus.SUCCESS) {
                    value = BerkeleyTriples.this.<T>value(entry);
                }
                return value;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Iterator<Long> reverse(final String name, final T value) {
        final SecondaryCursor cursor =
            (SecondaryCursor) this.cursor(this.secondary(name));
        final DatabaseEntry key = this.entry(value);
        return new AbstractIterator<Long>() {
            @Override
            public Long fetch() {
                final DatabaseEntry entry = new DatabaseEntry();
                final DatabaseEntry pkey = new DatabaseEntry();
                Long number = null;
                final OperationStatus status =
                    cursor.getNext(key, pkey, entry, null);
                if (status == OperationStatus.SUCCESS) {
                    number = BerkeleyTriples.this.<Long>value(pkey);
                }
                return number;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Iterator<Long> reverse(final String name,
        final String join, final T value) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear(final Long number, final String name) {
        if (this.database(name).delete(null, this.key(number))
            != OperationStatus.SUCCESS) {
            throw new IllegalStateException(
                String.format("Failed to delete %d in '%s'", number, name)
            );
        }
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
                if (name.charAt(0) != '-') {
                    config.setSortedDuplicates(true);
                }
                this.databases.put(
                    name,
                    this.env.openDatabase(null, name, config)
                );
                Logger.debug(this, "#database('%s'): opened", name);
            }
        }
        return this.databases.get(name);
    }

    /**
     * Get secondary database by name.
     * @param name The name
     * @return The database
     */
    private SecondaryDatabase secondary(final String name) {
        final String sname = String.format("%s-secondary", name);
        synchronized (this.databases) {
            if (!this.databases.containsKey(sname)) {
                final Database primary = this.database(name);
                final SecondaryConfig config = new SecondaryConfig();
                config.setAllowCreate(true);
                config.setSortedDuplicates(true);
                config.setKeyCreator(
                    new SecondaryKeyCreator() {
                        // @checkstyle ParameterNumber (5 lines)
                        @Override
                        public boolean createSecondaryKey(
                            final SecondaryDatabase database,
                            final DatabaseEntry key, final DatabaseEntry entry,
                            final DatabaseEntry result) {
                            result.setData(entry.getData());
                            return true;
                        }
                    }
                );
                this.databases.put(
                    sname,
                    this.env.openSecondaryDatabase(null, sname, primary, config)
                );
            }
        }
        return (SecondaryDatabase) this.databases.get(sname);
    }

    /**
     * Create new cursor.
     * @param database The database to use
     * @return The cursor
     */
    private Cursor cursor(final Database database) {
        final Cursor cursor = database.openCursor(null, null);
        synchronized (this.cursors) {
            Long now = System.currentTimeMillis();
            for (Long time : this.cursors.keySet()) {
                // @checkstyle MagicNumber (1 line)
                if (time < now - 10 * 1000) {
                    this.cursors.get(time).close();
                    this.cursors.remove(time);
                }
            }
            while (this.cursors.containsKey(now)) {
                --now;
            }
            this.cursors.put(now, cursor);
        }
        return cursor;
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
     * Create entry from value.
     * @param value The value
     * @return The entry
     * @param <T> Type of value
     */
    public <T> DatabaseEntry entry(final T value) {
        final DatabaseEntry entry = new DatabaseEntry();
        new SerialBinding(
            new StoredClassCatalog(this.database(BerkeleyTriples.META_TABLE)),
            value.getClass()
        ).objectToEntry(value, entry);
        return entry;
    }

    /**
     * Revert entry back to value.
     * @param entry The entry
     * @return The value
     * @param <T> Type of value
     */
    public <T> T value(final DatabaseEntry entry) {
        return (T) new SerialBinding(
            new StoredClassCatalog(this.database(BerkeleyTriples.META_TABLE)),
            Object.class
        ).entryToObject(entry);
    }

}
