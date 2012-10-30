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
package com.netbout.spi.client;

import com.netbout.spi.Participant;
import com.netbout.spi.Urn;
import java.net.HttpURLConnection;
import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.List;

/**
 * List of {@link Participant}-s in a {@link Bout}.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class RestParticipants extends AbstractCollection<Participant> {

    /**
     * Rest client.
     */
    private final transient RestClient client;

    /**
     * Public ctor.
     * @param clnt Rest client
     */
    public RestParticipants(final RestClient clnt) {
        super();
        this.client = clnt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Participant> iterator() {
        final Iterator<String> names = this.names().iterator();
        return new Iterator<Participant>() {
            @Override
            public Participant next() {
                return new RestParticipant(
                    RestParticipants.this.client.copy(),
                    Urn.create(names.next())
                );
            }
            @Override
            public boolean hasNext() {
                return names.hasNext();
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return this.names().size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final Object object) {
        return this.names().contains(object.toString());
    }

    /**
     * Fetch list of names.
     * @return Names of participants
     */
    private List<String> names() {
        return this.client
            .get("bout.participants()")
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath("/page/bout/participants")
            .xpath("/page/bout/participants/participant/identity/text()");
    }

}
