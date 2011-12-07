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
import com.netbout.spi.Message;
import com.netbout.spi.Participant;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The bout.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class RestBout implements Bout {

    /**
     * Rest client.
     */
    private final transient RestClient client;

    /**
     * Public ctor.
     * @param clnt Rest client
     */
    public RestBout(final RestClient clnt) {
        this.client = clnt;
    }

    /**
     * Get its URI.
     * @return The URI
     */
    public URI uri() {
        return this.client.uri();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long number() {
        final String num = this.client
            .get("reading bout number")
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath("/page/bout")
            .xpath("/page/bout/number/text()")
            .get(0);
        return Long.valueOf(num);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String title() {
        return this.client
            .get("reading bout title")
            .assertStatus(HttpURLConnection.HTTP_OK)
            .xpath("/page/bout/title/text()")
            .get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rename(final String text) {
        this.client
            .get("reading 'rename' rel link")
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath("/page/links/link[@rel='rename']")
            .rel("rename")
            .post(String.format("renaming bout to '%s'", text), "title", text)
            .assertStatus(HttpURLConnection.HTTP_SEE_OTHER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Collection<Participant> participants() {
        final List<String> names = this.client
            .get("reading names of bout participants")
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath("/page/bout/participants")
            .xpath("/page/bout/participants/participant/identity/text()");
        final List<Participant> dudes = new ArrayList<Participant>();
        for (String name : names) {
            dudes.add(new RestParticipant(this.client.copy(), name));
        }
        return dudes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Participant invite(final Identity identity) {
        final String name = identity.name();
        final List<String> hrefs = this.client
            .queryParam("mask", name)
            .get(String.format("reading suggestions for '%s'", name))
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath(String.format("/page/mask[.='%s']", name))
            .assertXPath("/page/invitees")
            .xpath(
                String.format(
                    "/page/invitees/invitee[name='%s']/@href",
                    name
                )
            );
        if (hrefs.isEmpty()) {
            throw new IllegalStateException(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "Can't invite '%s' to the bout because Netbout doesn't suggest his/her identity",
                    name
                )
            );
        }
        final String participant = this.client
            .copy(hrefs.get(0))
            .get(String.format("inviting '%s' to the bout", name))
            .assertStatus(HttpURLConnection.HTTP_SEE_OTHER)
            .header("Participant-name");
        return new RestParticipant(this.client.copy(), participant);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public List<Message> messages(final String query) {
        final List<String> nums = this.client
            .get("reading numbers of bout messages")
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath("/page/bout/messages")
            .xpath("/page/bout/messages/message/number");
        final List<Message> msgs = new ArrayList<Message>();
        for (String num : nums) {
            msgs.add(new RestMessage(this.client.copy(), Long.valueOf(num)));
        }
        return msgs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message message(final Long number) {
        return new RestMessage(this.client.copy(), number);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void confirm(final boolean status) {
        if (status) {
            this.client
                .get("reading 'join' rel link")
                .assertStatus(HttpURLConnection.HTTP_OK)
                .assertXPath("/page/links/link[@rel='join']")
                .rel("join")
                .get("joining the bout")
                .assertStatus(HttpURLConnection.HTTP_SEE_OTHER);
        } else {
            this.client
                .get("reading 'leave' rel link")
                .assertStatus(HttpURLConnection.HTTP_OK)
                .assertXPath("/page/links/link[@rel='leave']")
                .rel("leave")
                .get("leaving the bout")
                .assertStatus(HttpURLConnection.HTTP_SEE_OTHER);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message post(final String text) {
        final String num = this.client
            .get("reading 'post' rel link")
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath("/page/links/link[@rel='post']")
            .rel("post")
            .post("posting new message to the bout", "text", text)
            .assertStatus(HttpURLConnection.HTTP_SEE_OTHER)
            .header("Message-number");
        return new RestMessage(this.client.copy(), Long.valueOf(num));
    }

}
