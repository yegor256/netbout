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
package com.netbout.stub;

import com.netbout.spi.Identity;
import com.netbout.spi.Bout;
import com.netbout.spi.Helper;
import com.netbout.spi.Participant;
import com.netbout.spi.User;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Simple implementation of a {@link Identity}.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class SimpleIdentity implements Identity {

    /**
     * The entry.
     */
    private final InMemoryEntry entry;

    /**
     * The name.
     */
    private final String name;

    /**
     * Public ctor.
     * @param ent The entry to work with
     * @param identity The identity
     * @see SimpleUser#identity(String)
     */
    public SimpleIdentity(final InMemoryEntry ent, final String identity) {
        this.entry = ent;
        this.name = identity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout start() {
        final BoutData data = new BoutData(this.entry);
        final Participant dude = data.invite(this.name);
        dude.confirm();
        this.entry.add(data);
        return new SimpleBout(this, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Bout> inbox(final String query) {
        final List<Bout> list = new ArrayList<Bout>();
        for (BoutData data : this.entry.bouts()) {
            for (Participant dude : data.participants()) {
                if (dude.identity().equals(this)) {
                    list.add(new SimpleBout(this, data));
                    break;
                }
            }
        }
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL photo() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void promote(final String pkg) {
        final Reflections reflections = new Reflections(pkg);
        final Set<Class<?>> annotated =
            reflections.getTypesAnnotatedWith(Helper.class)
            
    }

}
