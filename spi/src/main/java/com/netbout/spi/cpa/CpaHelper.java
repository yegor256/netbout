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

import com.netbout.spi.Bout;
import com.netbout.spi.BoutNotFoundException;
import com.netbout.spi.Helper;
import com.netbout.spi.Identity;
import com.netbout.spi.Token;
import com.netbout.spi.UnreachableUrnException;
import com.netbout.spi.Urn;
import com.ymock.util.Logger;
import java.net.URL;
import java.util.List;
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
     * @param idnt The identity of me
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
    public void execute(final Token token) {
        if (!this.ops.containsKey(token.mnemo())) {
            throw new IllegalArgumentException(
                String.format(
                    "Operation '%s' not supported by '%s'",
                    token.mnemo(),
                    this.name()
                )
            );
        }
        final long start = System.currentTimeMillis();
        this.ops.get(token.mnemo()).execute(token);
        Logger.debug(
            this,
            "#execute('%s'): done in %dms",
            token.mnemo(),
            System.currentTimeMillis() - start
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Identity idnt) {
        return this.identity.compareTo(idnt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL authority() {
        return this.identity.authority();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Urn name() {
        return this.identity.name();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout start() {
        return this.identity.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Bout> inbox(final String query) {
        return this.identity.inbox(query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout bout(final Long number) throws BoutNotFoundException {
        return this.identity.bout(number);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL photo() {
        return this.identity.photo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPhoto(final URL photo) {
        this.identity.setPhoto(photo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity friend(final Urn name) throws UnreachableUrnException {
        return this.identity.friend(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Identity> friends(final String keyword) {
        return this.identity.friends(keyword);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> aliases() {
        return this.identity.aliases();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void alias(final String alias) {
        this.identity.alias(alias);
    }

    /**
     * Initialize.
     * @param url URL where to get the code
     * @return Discovered ops
     */
    private ConcurrentMap<String, HelpTarget> discover(final URL url) {
        final long start = System.currentTimeMillis();
        final ConcurrentMap<String, HelpTarget> found =
            new OpDiscoverer(this).discover(url);
        Logger.info(
            this,
            "#init('%s'): %d operations discovered in %dms: %[list]s",
            url,
            found.size(),
            System.currentTimeMillis() - start,
            found
        );
        return found;
    }

}
