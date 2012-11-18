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

import com.jcabi.urn.URN;
import com.netbout.spi.Friend;
import com.netbout.spi.Participant;
import com.netbout.spi.Profile;
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
    private final transient URN iname;

    /**
     * Public ctor.
     * @param clnt Rest client
     * @param nam Name of participant
     */
    public RestParticipant(final RestClient clnt, final URN nam) {
        this.client = clnt;
        this.iname = nam;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.name().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Friend friend) {
        return this.name().compareTo(friend.name());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URN name() {
        return this.iname;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean confirmed() {
        return Boolean.valueOf(this.attr("confirmed"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean leader() {
        return Boolean.valueOf(this.attr("leader"));
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
            .assertXPath(this.xpath("", "/link[@rel='kickoff']"))
            .rel(this.xpath("", "/link[@rel='kickoff']/@href"))
            .get(String.format("kicking off '%s' participant", this.iname))
            .assertStatus(HttpURLConnection.HTTP_SEE_OTHER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Profile profile() {
        throw new UnsupportedOperationException(
            "Participant#profile() is not implemented yet"
        );
    }

    /**
     * Fetch attribute value.
     * @param attr Name of attribute
     * @return The value found
     */
    private String attr(final String attr) {
        return this.client
            .get(String.format("reading '%s' of a participant", attr))
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath(this.xpath(String.format("and @%s", attr), ""))
            .xpath(this.xpath("", String.format("/@%s", attr)))
            .get(0);
    }

    /**
     * Build xpath.
     * @param condition Extra condition to add to node
     * @param suffix At the end of expression
     * @return The XPath
     */
    private String xpath(final String condition, final String suffix) {
        return String.format(
            "/page/bout/participants/participant[identity='%s' %s]%s",
            this.iname,
            condition,
            suffix
        );
    }

}
