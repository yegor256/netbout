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
import com.netbout.spi.Participant;
import com.netbout.spi.Urn;
import java.net.HttpURLConnection;

/**
 * The participant.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class RestParticipant implements Participant {

    /**
     * Rest client.
     */
    private final transient RestClient client;

    /**
     * Number of this guy.
     */
    private final transient Urn name;

    /**
     * Public ctor.
     * @param clnt Rest client
     * @param nam Name of participant
     */
    public RestParticipant(final RestClient clnt, final Urn nam) {
        this.client = clnt;
        this.name = nam;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout bout() {
        return new RestBout(this.client.copy());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity identity() {
        return new Friend(this.name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean confirmed() {
        return Boolean.valueOf(this.bySuffix("/@confirmed"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean leader() {
        return Boolean.valueOf(this.bySuffix("/@leader"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void consign() {
        throw new UnsupportedOperationException(
            "Participant#consign() is not implemented yet"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void kickOff() {
        this.client
            .get("reading 'kickoff' rel link")
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath(this.xpath("/link[@rel='kickoff']"))
            .rel(this.xpath("/link[@rel='kickoff']/@href"))
            .get(String.format("kicking off '%s' participant", this.name))
            .assertStatus(HttpURLConnection.HTTP_SEE_OTHER);
    }

    /**
     * Fetch by XPath suffix.
     * @param suffix The suffix of XPath
     * @return The value found
     */
    public String bySuffix(final String suffix) {
        return this.client
            .get(String.format("reading '%s' of a participant", suffix))
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath(this.xpath(suffix))
            .xpath(this.xpath(suffix))
            .get(0);
    }

    /**
     * Build XPath.
     * @param suffix The suffix of XPath
     * @return The XPath path
     */
    private String xpath(final String suffix) {
        return String.format(
            "/page/bout/participants/participant[identity='%s']%s",
            this.name,
            suffix
        );
    }

}
