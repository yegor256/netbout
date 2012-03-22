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
package com.netbout.inf.index;

import com.netbout.inf.Index;
import com.ymock.util.Logger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.SerializationUtils;

/**
 * Index in file-system.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.DoNotUseThreads")
public final class FsIndex implements Index {

    /**
     * The file to use.
     */
    private final transient File file = new File(
        System.getProperty("java.io.tmpdir"),
        "netbout-INF-data.ser"
    );

    /**
     * All maps.
     * @checkstyle LineLength (3 lines)
     */
    private final transient ConcurrentMap<String, ConcurrentMap<Object, Object>> maps;

    /**
     * Public ctor.
     */
    public FsIndex() {
        synchronized (FsIndex.class) {
            this.maps = FsIndex.load(this.file);
        }
        Executors.newSingleThreadScheduledExecutor()
            .scheduleAtFixedRate(new Saver(), 0L, 1L, TimeUnit.MINUTES);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <X, Y> ConcurrentMap<X, Y> get(final String name) {
        synchronized (this.maps) {
            if (!this.maps.containsKey(name)) {
                this.maps.put(
                    name,
                    (ConcurrentMap) new ConcurrentHashMap<X, Y>()
                );
            }
            return (ConcurrentHashMap<X, Y>) this.maps.get(name);
        }
    }

    /**
     * Flush it to disc.
     */
    public void flush() {
        final long start = System.nanoTime();
        try {
            synchronized (FsIndex.class) {
                SerializationUtils.serialize(
                    (Serializable) this.maps,
                    new FileOutputStream(this.file)
                );
            }
            Logger.info(
                this,
                "#flush(): MAPS saved %d bytes to %s in %[nano]s",
                this.file.length(),
                this.file,
                System.nanoTime() - start
            );
        } catch (java.io.IOException ex) {
            Logger.error(
                this,
                "#flush(): failed to save MAPS to %s: %[exception]s",
                this.file,
                ex
            );
        }
    }

    /**
     * Load maps.
     * @param src Where to load from
     * @return Loaded or created maps
     */
    private static ConcurrentMap<String, ConcurrentMap<Object, Object>> load(
        final File src) {
        ConcurrentMap<String, ConcurrentMap<Object, Object>> maps =
            new ConcurrentHashMap<String, ConcurrentMap<Object, Object>>();
        if (src.exists()) {
            final long start = System.nanoTime();
            try {
                maps = (ConcurrentMap) SerializationUtils.deserialize(
                    new FileInputStream(src)
                );
                Logger.info(
                    FsIndex.class,
                    "#load(%s): MAPS loaded %d bytes in %[nano]s",
                    src,
                    src.length(),
                    System.nanoTime() - start
                );
            } catch (java.io.IOException ex) {
                Logger.error(
                    FsIndex.class,
                    "#load(%s): failed to load MAPS: %[exception]s",
                    src,
                    ex
                );
            } catch (org.apache.commons.lang.SerializationException ex) {
                Logger.error(
                    FsIndex.class,
                    "#load(%s): failed to deserialize MAPS: %[exception]s",
                    src,
                    ex
                );
            }
        }
        return maps;
    }

    private final class Saver implements Runnable {
        @Override
        public void run() {
            FsIndex.this.flush();
        }
    }

}
