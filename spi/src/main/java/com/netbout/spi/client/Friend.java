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
package com.netbout.spi.client;

import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * Friend, with no connection to REST API.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
final class Friend implements Identity {

    /**
     * Name of it.
     */
    private final transient String iname;

    /**
     * Public ctor.
     * @param name The name of it
     */
    public Friend(final String name) {
        this.iname = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String user() {
        throw new UnsupportedOperationException(
            "#user() can't be called on a friend"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return this.iname;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout start() {
        throw new UnsupportedOperationException(
            "#start() can't be called on a friend"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Bout> inbox(final String query) {
        throw new UnsupportedOperationException(
            "#inbox() can't be called on a friend"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout bout(final Long num) {
        throw new UnsupportedOperationException(
            "#bout() can't be called on a friend"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL photo() {
        throw new UnsupportedOperationException(
            "#photo() can't be called on a friend"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPhoto(final URL photo) {
        throw new UnsupportedOperationException(
            "#setPhoto() can't be called on a friend"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity friend(final String name) {
        throw new UnsupportedOperationException(
            "#friend() can't be called on a friend"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Identity> friends(final String keyword) {
        throw new UnsupportedOperationException(
            "#friends() can't be called on a friend"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> aliases() {
        throw new UnsupportedOperationException(
            "#aliases() can't be called on a friend"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void alias(final String alias) {
        throw new UnsupportedOperationException(
            "#alias() can't be called on a friend"
        );
    }

}
