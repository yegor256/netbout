/**
 * Copyright (c) 2009-2014, netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netbout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code accidentally and without intent to use it, please report this
 * incident to the author by email.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.netbout.rest;

import com.netbout.spi.Attachment;
import com.netbout.spi.Bout;
import com.netbout.spi.Friend;
import com.netbout.spi.Inbox;
import com.netbout.spi.Message;
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.Link;
import com.rexsl.page.PageBuilder;
import com.rexsl.page.inset.FlashInset;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * RESTful front of one Bout.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
@Path("/b/{num}")
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.AvoidDuplicateLiterals" })
public final class BoutRs extends BaseRs {

    /**
     * Number of the bout.
     */
    private transient Long number;

    /**
     * Set number of bout.
     * @param num The number
     */
    @PathParam("num")
    public void setNumber(final Long num) {
        this.number = num;
    }

    /**
     * Get bout front page.
     * @return The JAX-RS response
     * @throws IOException If fails
     */
    @GET
    @Path("/")
    public Response front() throws IOException {
        return new PageBuilder()
            .stylesheet("/xsl/bout.xsl")
            .build(NbPage.class)
            .init(this)
            .link(new Link("post", "./post"))
            .link(new Link("rename", "./rename"))
            .link(new Link("invite", "./invite"))
            .link(new Link("leave", "./leave"))
            .link(new Link("kick", "./kick"))
            .append(this.bundle(this.bout()))
            .render()
            .build();
    }

    /**
     * Download attachment.
     * @param name Name of attachment
     * @throws IOException If fails
     */
    @GET
    @Path("/download")
    public void download(@QueryParam("name") final String name)
        throws IOException {
        throw FlashInset.forward(
            this.uriInfo().getBaseUri(),
            "not implemented yet",
            Level.WARNING
        );
    }

    /**
     * Post new message to the bout.
     * @param text Text of message just posted
     * @throws IOException If fails
     */
    @POST
    @Path("/post")
    public void post(@FormParam("text") final String text) throws IOException {
        this.bout().messages().post(text);
        throw FlashInset.forward(
            this.self(),
            "message posted to the bout",
            Level.INFO
        );
    }

    /**
     * Rename this bout.
     * @param title New title to set
     * @throws IOException If fails
     */
    @POST
    @Path("/rename")
    public void rename(@FormParam("title") final String title)
        throws IOException {
        this.bout().rename(title);
        throw FlashInset.forward(
            this.self(),
            "bout renamed",
            Level.INFO
        );
    }

    /**
     * Invite new person.
     * @param name Name of the invitee
     * @throws IOException If fails
     */
    @POST
    @Path("/invite")
    public void invite(@FormParam("name") final String name)
        throws IOException {
        final String check = this.user().aliases().check(name);
        if (check.isEmpty()) {
            throw FlashInset.forward(
                this.self(),
                String.format("incorrect alias '%s', try again", name),
                Level.WARNING
            );
        }
        this.bout().friends().invite(name);
        throw FlashInset.forward(
            this.self(),
            "new person invited to the bout",
            Level.INFO
        );
    }

    /**
     * Leave this bout.
     * @throws IOException If fails
     */
    @GET
    @Path("/leave")
    public void leave() throws IOException {
        this.bout().friends().kick(this.alias().name());
        throw FlashInset.forward(
            this.self(),
            "you left this bout",
            Level.INFO
        );
    }

    /**
     * Kick-off somebody from the bout.
     * @param name Who to kick off
     * @throws IOException If fails
     */
    @GET
    @Path("/kick")
    public void kick(@QueryParam("name") final String name) throws IOException {
        this.bout().friends().kick(name);
        throw FlashInset.forward(
            this.self(),
            "you kicked him off this bout",
            Level.INFO
        );
    }

    /**
     * Get self URI.
     * @return URI
     * @throws IOException If fails
     */
    private URI self() throws IOException {
        return this.uriInfo().getBaseUriBuilder().clone()
            .path(BoutRs.class)
            .build(this.bout().number());
    }

    /**
     * Get bout.
     * @return The bout
     * @throws IOException If fails
     */
    private Bout bout() throws IOException {
        try {
            return this.alias().inbox().bout(this.number);
        } catch (final Inbox.BoutNotFoundException ex) {
            throw FlashInset.forward(
                this.uriInfo().getBaseUri(), ex
            );
        }
    }

    /**
     * Bundle of the bout.
     * @param bout Bout
     * @return Bundle
     * @throws IOException If fails
     */
    private JaxbBundle bundle(final Bout bout) throws IOException {
        return new JaxbBundle("bout")
            .add("number", Long.toString(bout.number()))
            .up()
            .add("title", bout.title()).up()
            .add(
                new JaxbBundle("friends").add(
                    new JaxbBundle.Group<Friend>(bout.friends().iterate()) {
                        @Override
                        public JaxbBundle bundle(final Friend friend) {
                            try {
                                return BoutRs.this.bundle(bout, friend);
                            } catch (final IOException ex) {
                                throw new IllegalStateException(ex);
                            }
                        }
                    }
                )
            )
            .add(
                new JaxbBundle("attachments").add(
                    // @checkstyle LineLength (1 line)
                    new JaxbBundle.Group<Attachment>(bout.attachments().iterate()) {
                        @Override
                        public JaxbBundle bundle(final Attachment attachment) {
                            try {
                                return BoutRs.this.bundle(bout, attachment);
                            } catch (final IOException ex) {
                                throw new IllegalStateException(ex);
                            }
                        }
                    }
                )
            )
            .add(
                new JaxbBundle("messages").add(
                    new JaxbBundle.Group<Message>(bout.messages().iterate()) {
                        @Override
                        public JaxbBundle bundle(final Message message) {
                            return BoutRs.this.bundle(message);
                        }
                    }
                )
            );
    }

    /**
     * Convert friend to bundle.
     * @param bout Bout we're in
     * @param friend Friend to convert
     * @return Bundle
     * @throws IOException If fails
     */
    private JaxbBundle bundle(final Bout bout,
        final Friend friend) throws IOException {
        return new JaxbBundle("friend")
            .add("alias", friend.alias())
            .up()
            .add("photo", friend.photo().toString()).up()
            .link(
                new Link(
                    "kick",
                    this.uriInfo().getBaseUriBuilder().clone()
                        .path(BoutRs.class)
                        .path(BoutRs.class, "kick")
                        .queryParam("name", "{x}")
                        .build(bout.number(), friend.alias())
                )
            );
    }

    /**
     * Convert attachment to bundle.
     * @param bout Bout we're in
     * @param attachment Attachment
     * @return Bundle
     * @throws IOException If fails
     */
    private JaxbBundle bundle(final Bout bout,
        final Attachment attachment) throws IOException {
        return new JaxbBundle("attachment")
            .add("name", attachment.name())
            .up()
            .add("ctype", attachment.ctype()).up()
            .link(
                new Link(
                    "open",
                    this.uriInfo().getBaseUriBuilder().clone()
                        .path(BoutRs.class)
                        .queryParam("attachment", "{a1}")
                        .build(bout.number(), attachment.name())
                )
            )
            .link(
                new Link(
                    "download",
                    this.uriInfo().getBaseUriBuilder().clone()
                        .path(BoutRs.class)
                        .path(BoutRs.class, "download")
                        .queryParam("attachment", "{a2}")
                        .build(bout.number(), attachment.name())
                )
            );
    }

    /**
     * Convert message to bundle.
     * @param message Message
     * @return Bundle
     */
    private JaxbBundle bundle(final Message message) {
        return new JaxbBundle("message")
            .add("number", Long.toString(message.number()))
            .up()
            .add("author", message.author()).up()
            .add("html", message.text()).up()
            .add(
                "date",
                DateFormatUtils.ISO_DATETIME_FORMAT.format(message.date())
            )
            .up();
    }

}
