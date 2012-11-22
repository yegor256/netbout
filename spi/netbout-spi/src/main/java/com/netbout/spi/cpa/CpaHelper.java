/**
 * Copyright (c) 2009-2012, Netbout.com
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

import com.jcabi.log.Logger;
import com.netbout.spi.Helper;
import com.netbout.spi.Identity;
import com.netbout.spi.Token;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * Classpath annotations helper.
 *
 * <p>Your classes should be annotated with <tt>&#64;Farm</tt> and
 * <tt>&#64;Operation</tt> annotations.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class CpaHelper implements Helper {

    /**
     * Who am I.
     */
    private final transient Identity identity;

    /**
     * Where this helper lives.
     */
    private final transient URL home;

    /**
     * All discovered operations.
     */
    private final transient ConcurrentMap<String, HelpTarget> ops;

    /**
     * Public ctor.
     * @param idnt The identity, which this helper will act on behalf of
     * @param url Jar URL where to get the code
     */
    public CpaHelper(final Identity idnt, final URL url) {
        this.identity = idnt;
        this.home = url;
        this.ops = this.discover(url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL location() {
        return this.home;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> supports() {
        return this.ops.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        // nothing to do here
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final Token token) {
        if (!this.ops.containsKey(token.mnemo())) {
            throw new IllegalArgumentException(
                String.format(
                    "Operation '%s' not supported by '%s' (%s)",
                    token.mnemo(),
                    this.identity.name(),
                    this.home
                )
            );
        }
        final long start = System.nanoTime();
        this.ops.get(token.mnemo()).execute(token);
        Logger.debug(
            this,
            "#execute('%s'): done in %[nano]s",
            token.mnemo(),
            System.nanoTime() - start
        );
    }

    /**
     * Initialize.
     * @param url URL where to get the code
     * @return Discovered ops
     */
    private ConcurrentMap<String, HelpTarget> discover(final URL url) {
        final long start = System.nanoTime();
        final ConcurrentMap<String, HelpTarget> found =
            new OpDiscoverer(this.identity).discover(url);
        Logger.info(
            this,
            "#init('%s'): %d operations discovered in %[nano]s: %[list]s",
            url,
            found.size(),
            System.nanoTime() - start,
            found.keySet()
        );
        return found;
    }

}
