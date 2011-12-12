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
import com.ymock.util.Logger;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

/**
 * Discovers operations in classpath.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class OpDiscoverer {

    /**
     * Manifest attribute to denote the name of the package on JAR.
     */
    private static final transient String MF_ATTR = "Netbout-Helper-Package";

    /**
     * The identity of the helper.
     */
    private final transient Identity identity;

    /**
     * Public ctor.
     * @param idnt The identity of helper
     */
    public OpDiscoverer(final Identity idnt) {
        this.identity = idnt;
    }

    /**
     * Discover all targets in the JAR.
     * @param url The URL with sources
     * @return Associative array of discovered targets/operations
     */
    public ConcurrentMap<String, HelpTarget> discover(final URL url) {
        Reflections reflections;
        if ("file".equals(url.getProtocol())) {
            reflections = this.fromPackage(url.getPath());
        } else if ("http".equals(url.getProtocol())) {
            try {
                reflections = this.fromWeb(url);
            } catch (IOException ex) {
                throw new IllegalArgumentException(ex);
            }
        } else {
            throw new IllegalArgumentException(
                String.format(
                    "Unknown protocol '%s' in URL '%s'",
                    url.getProtocol(),
                    url
                )
            );
        }
        return this.retrieve(reflections);
    }

    /**
     * Discover all methods in the provided farm.
     * @param farm The object annotated with {@link Farm}
     * @return Associative array of discovered targets/operations
     */
    private ConcurrentMap<String, HelpTarget> inFarm(final Object farm) {
        final ConcurrentMap<String, HelpTarget> targets =
            new ConcurrentHashMap<String, HelpTarget>();
        for (Method method : farm.getClass().getDeclaredMethods()) {
            final Annotation atn = method.getAnnotation(Operation.class);
            if (atn == null) {
                continue;
            }
            final String mnemo = ((Operation) atn).value();
            targets.put(mnemo, HelpTarget.build(farm, method));
            Logger.debug(
                this,
                "#inFarm(%s): @Operation('%s') found",
                farm.getClass().getName(),
                mnemo
            );
        }
        return targets;
    }

    /**
     * Load them from reflections.
     * @param ref The reflections
     * @return Associative array of discovered targets/operations
     */
    private ConcurrentMap<String, HelpTarget> retrieve(final Reflections ref) {
        final ConcurrentMap<String, HelpTarget> targets =
            new ConcurrentHashMap<String, HelpTarget>();
        for (Class tfarm : ref.getTypesAnnotatedWith(Farm.class)) {
            Logger.debug(
                this,
                "#discover(..): @Farm found at '%s'",
                tfarm.getName()
            );
            Object farm;
            try {
                farm = tfarm.newInstance();
            } catch (InstantiationException ex) {
                throw new IllegalArgumentException(ex);
            } catch (IllegalAccessException ex) {
                throw new IllegalArgumentException(ex);
            }
            if (farm instanceof IdentityAware) {
                ((IdentityAware) farm).init(this.identity);
            }
            targets.putAll(this.inFarm(farm));
        }
        return targets;
    }

    /**
     * Creates reflections from HTTP URL.
     * @param url The URL of the JAR
     * @return Reflections
     * @throws IOException If failed
     */
    private Reflections fromWeb(final URL url) throws IOException {
        URL jurl;
        try {
            jurl = this.download(url).toURL();
        } catch (java.net.MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
        final Object pkg = ((JarURLConnection) jurl.openConnection())
                .getManifest()
                .getMainAttributes()
                .get(this.MF_ATTR);
        if (pkg == null) {
            throw new IllegalArgumentException(
                String.format(
                    "Attribute '%s' not found in MANIFEST.MF in '%s'",
                    this.MF_ATTR,
                    url
                )
            );
        }
        return new Reflections(
            new ConfigurationBuilder()
                .filterInputsBy(new FilterBuilder().include((String) pkg))
                .setUrls(new URL[] {jurl})
        );
    }

    /**
     * Creates reflections from package.
     * @param pkg The name of the package
     * @return Reflections
     */
    private Reflections fromPackage(final String pkg) {
        if (pkg.isEmpty()) {
            throw new IllegalArgumentException(
                "Package is empty, can't load classes"
            );
        }
        String name = pkg;
        if (name.charAt(0) == '/') {
            name = name.substring(1);
        }
        return new Reflections(name);
    }

    /**
     * Download file from the given URL.
     * @param url The URL to download from
     * @return Reflections
     * @throws IOException If failed
     */
    private File download(final URL url) throws IOException {
        final File file = File.createTempFile("netbout", ".jar");
        return file;
    }

}
