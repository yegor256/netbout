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
            .get()
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
            .get()
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
            .get()
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath("/page/links/link[@rel='rename']")
            .rel("rename")
            .post("title", text)
            .assertStatus(HttpURLConnection.HTTP_SEE_OTHER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Participant> participants() {
        final List<String> names = this.client
            .get()
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
        final String name = this.client
            .get()
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath("/page/links/link[@rel='invite']")
            .rel("invite")
            .queryParam("name", identity.name())
            .get()
            .assertStatus(HttpURLConnection.HTTP_SEE_OTHER)
            .header("participant-name");
        return new RestParticipant(this.client.copy(), name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Message> messages(final String query) {
        final List<String> nums = this.client
            .get()
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
                .get()
                .assertStatus(HttpURLConnection.HTTP_OK)
                .assertXPath("/page/links/link[@rel='join']")
                .rel("join")
                .get()
                .assertStatus(HttpURLConnection.HTTP_SEE_OTHER);
        } else {
            this.client
                .get()
                .assertStatus(HttpURLConnection.HTTP_OK)
                .assertXPath("/page/links/link[@rel='leave']")
                .rel("leave")
                .get()
                .assertStatus(HttpURLConnection.HTTP_SEE_OTHER);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message post(final String text) {
        final String num = this.client
            .get()
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath("/page/links/link[@rel='post']")
            .rel("post")
            .post("text", text)
            .assertStatus(HttpURLConnection.HTTP_SEE_OTHER)
            .header("message-id");
        return new RestMessage(this.client.copy(), Long.valueOf(num));
    }

}
