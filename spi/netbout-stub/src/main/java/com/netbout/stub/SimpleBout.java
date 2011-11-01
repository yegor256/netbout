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

import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import com.netbout.spi.Participant;
import java.util.Collection;
import java.util.List;

/**
 * Simple implementation of a {@link Bout}.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class SimpleBout implements Bout {

    /**
     * The viewer.
     */
    private Identity identity;

    /**
     * The data.
     */
    private BoutData data;

    /**
     * Public ctor.
     * @param idnt The viewer
     * @param dat The data
     */
    public SimpleBout(final Identity idnt, final BoutData dat) {
        this.identity = idnt;
        this.data = dat;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String title() {
        return this.data.getTitle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void title(final String text) {
        this.data.setTitle(text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Participant invite(final String identity) {
        return this.data.invite(identity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Participant> participants() {
        return (Collection) this.data.participants();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Message> messages(final String query) {
        return (List) this.data.messages(query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message post(final String text) {
        return this.data.post(this.identity.name(), text);
    }

}
