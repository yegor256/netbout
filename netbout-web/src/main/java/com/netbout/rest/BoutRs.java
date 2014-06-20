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

import com.google.common.collect.Iterables;
import com.jcabi.aspects.Tv;
import com.netbout.spi.Attachment;
import com.netbout.spi.Attachments;
import com.netbout.spi.Bout;
import com.netbout.spi.Friend;
import com.netbout.spi.Friends;
import com.netbout.spi.Inbox;
import com.netbout.spi.Message;
import com.netbout.spi.Messages;
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.Link;
import com.rexsl.page.PageBuilder;
import com.rexsl.page.inset.FlashInset;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import eu.medsea.mimeutil.MimeUtil;
import eu.medsea.mimeutil.detector.MagicMimeMimeDetector;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.logging.Level;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.ocpsoft.prettytime.PrettyTime;

/**
 * RESTful front of one Bout.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (1000 lines)
 * @checkstyle MultipleStringLiteralsCheck (1000 lines)
 * @checkstyle ClassFanOutComplexityCheck (1000 lines)
 */
@Path("/b/{num: [0-9]+}")
@SuppressWarnings({
    "PMD.TooManyMethods", "PMD.AvoidDuplicateLiterals", "PMD.ExcessiveImports"
})
public final class BoutRs extends BaseRs {

    /**
     * Number of the bout.
     */
    private transient Long number;

    /**
     * Name of attachment to show.
     */
    private transient String open;

    /**
     * Message number to exclude (show messages after this one).
     */
    private transient long start = Inbox.NEVER;

    static {
        MimeUtil.registerMimeDetector(
            MagicMimeMimeDetector.class.getCanonicalName()
        );
    }

    /**
     * Set number of bout.
     * @param num The number
     */
    @PathParam("num")
    public void setNumber(final Long num) {
        this.number = num;
    }

    /**
     * Set the name of an open attachment.
     * @param name The name
     */
    @QueryParam("open")
    public void setOpen(final String name) {
        this.open = name;
    }

    /**
     * Set start message numbe.
     * @param num The number
     */
    @QueryParam("start")
    public void setStart(final Long num) {
        if (num != null) {
            this.start = num;
        }
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
            .link(new Link("upload", "./upload"))
            .link(new Link("create", "./create"))
            .link(new Link("attach", "./attach"))
            .append(this.bundle(this.bout()))
            .render()
            .build();
    }

    /**
     * Download attachment.
     * @param name Name of attachment
     * @return Content
     * @throws IOException If fails
     */
    @GET
    @Path("/download")
    public Response download(@QueryParam("name") final String name)
        throws IOException {
        final Attachment attachment = this.bout().attachments().get(name);
        return Response
            .ok()
            .type(attachment.ctype())
            .header(
                "Content-Disposition",
                String.format(
                    "attachment; filename=\"%s\"",
                    URLEncoder.encode(attachment.name(), CharEncoding.UTF_8)
                )
            )
            .entity(
                new StreamingOutput() {
                    @Override
                    public void write(final OutputStream output)
                        throws IOException {
                        IOUtils.copyLarge(attachment.read(), output);
                    }
                }
            )
            .build();
    }

    /**
     * Upload attachment.
     * @param name Name of attachment
     * @param ctype Ctype of it
     * @param content Content to upload
     * @throws IOException If fails
     */
    @POST
    @Path("/upload")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void upload(@QueryParam("name") final String name,
        @QueryParam("ctype") final String ctype, final InputStream content)
        throws IOException {
        this.bout().attachments().get(name).write(content, ctype);
        throw FlashInset.forward(
            this.self(),
            String.format("attachment '%s' uploaded", name),
            Level.INFO
        );
    }

    /**
     * Attach a file as an attachment.
     * @param name Name of attachment
     * @param stream Stream
     * @param disposition Disposition
     * @throws IOException If fails
     */
    @POST
    @Path("/attach")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public void attach(
        @FormDataParam("name") final String name,
        @FormDataParam("file") final InputStream stream,
        @FormDataParam("file") final FormDataContentDisposition disposition)
        throws IOException {
        final File temp = File.createTempFile("netbout", "bin");
        IOUtils.copy(stream, new FileOutputStream(temp));
        final StringBuilder msg = new StringBuilder(Tv.HUNDRED);
        if (new Attachments.Search(this.bout().attachments()).exists(name)) {
            msg.append(String.format("attachment '%s' overwritten", name));
        } else {
            try {
                this.bout().attachments().create(name);
            } catch (final Attachments.InvalidNameException ex) {
                throw FlashInset.forward(this.self(), ex);
            }
            msg.append(String.format("attachment '%s' uploaded", name));
        }
        final Collection<?> ctypes = MimeUtil.getMimeTypes(temp);
        final String ctype;
        if (ctypes.isEmpty()) {
            ctype = MediaType.APPLICATION_OCTET_STREAM;
        } else {
            ctype = ctypes.iterator().next().toString();
        }
        msg.append(" (").append(temp.length())
            .append(" bytes, ").append(ctype).append(')');
        try {
            this.bout().attachments().get(name).write(
                new FileInputStream(temp),
                ctype
            );
        } catch (final Attachment.TooBigException ex) {
            throw FlashInset.forward(this.self(), ex);
        } catch (final Attachment.BrokenContentException ex) {
            throw FlashInset.forward(this.self(), ex);
        }
        FileUtils.forceDelete(temp);
        throw FlashInset.forward(this.self(), msg.toString(), Level.INFO);
    }

    /**
     * Create an attachment.
     * @param name Name of attachment
     * @throws IOException If fails
     */
    @POST
    @Path("/create")
    public void create(@FormParam("name") final String name)
        throws IOException {
        try {
            this.bout().attachments().create(name);
        } catch (final Attachments.InvalidNameException ex) {
            throw FlashInset.forward(this.self(), ex);
        }
        throw FlashInset.forward(
            this.self(),
            String.format("attachment '%s' created", name),
            Level.INFO
        );
    }

    /**
     * Delete an attachment.
     * @param name Name of attachment
     * @throws IOException If fails
     */
    @GET
    @Path("/delete")
    public void delete(@QueryParam("name") final String name)
        throws IOException {
        try {
            this.bout().attachments().delete(name);
        } catch (final Attachments.InvalidNameException ex) {
            throw FlashInset.forward(this.self(), ex);
        }
        throw FlashInset.forward(
            this.self(),
            String.format("attachment '%s' deleted", name),
            Level.INFO
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
            String.format(
                "message posted to the bout #%d",
                this.bout().number()
            ),
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
            String.format("bout #%d renamed", this.bout().number()),
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
        try {
            this.bout().friends().invite(name);
        } catch (final Friends.UnknownAliasException ex) {
            throw FlashInset.forward(this.self(), ex);
        }
        throw FlashInset.forward(
            this.self(),
            String.format(
                "new person invited to the bout #%d",
                this.bout().number()
            ),
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
            String.format(
                "you kicked '%s' off this bout #%d",
                name, this.bout().number()
            ),
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
        final Bout bout;
        try {
            bout = this.alias().inbox().bout(this.number);
        } catch (final Inbox.BoutNotFoundException ex) {
            throw new WebApplicationException(
                ex, HttpURLConnection.HTTP_NOT_FOUND
            );
        }
        if (!new Friends.Search(bout.friends()).exists(this.alias().name())) {
            throw FlashInset.forward(
                this.uriInfo().getBaseUri(),
                String.format("you're not in bout #%d", bout.number()),
                Level.WARNING
            );
        }
        return bout;
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
            .add("unread", Long.toString(bout.messages().unread())).up()
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
                    new JaxbBundle.Group<Message>(
                        Iterables.limit(
                            bout.messages().jump(this.start).iterate(),
                            Messages.PAGE
                        )
                    ) {
                        @Override
                        public JaxbBundle bundle(final Message message) {
                            try {
                                return BoutRs.this.bundle(message);
                            } catch (final IOException ex) {
                                throw new IllegalStateException(ex);
                            }
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
            .link(
                new Link(
                    "photo",
                    this.uriInfo().getBaseUriBuilder().clone()
                        .path(FriendRs.class)
                        .path(FriendRs.class, "png")
                        .build(friend.alias())
                        .toString()
                )
            )
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
        JaxbBundle bundle = new JaxbBundle("attachment")
            .add("name", attachment.name())
            .up()
            .add("ctype", attachment.ctype()).up()
            .add("unseen", Boolean.toString(attachment.unseen())).up()
            .link(
                new Link(
                    "delete",
                    this.uriInfo().getBaseUriBuilder().clone()
                        .path(BoutRs.class)
                        .path(BoutRs.class, "delete")
                        .queryParam("name", "{a4}")
                        .build(bout.number(), attachment.name())
                )
            )
            .link(
                new Link(
                    "download",
                    this.uriInfo().getBaseUriBuilder().clone()
                        .path(BoutRs.class)
                        .path(BoutRs.class, "download")
                        .queryParam("name", "{a2}")
                        .build(bout.number(), attachment.name())
                )
            );
        if (attachment.ctype().equals(Attachment.MARKDOWN)) {
            bundle = bundle.link(
                new Link(
                    "open",
                    this.uriInfo().getBaseUriBuilder().clone()
                        .path(BoutRs.class)
                        .queryParam("open", "{a1}")
                        .build(bout.number(), attachment.name())
                )
            );
        }
        if (attachment.name().equals(this.open)
            && attachment.ctype().equals(Attachment.MARKDOWN)) {
            bundle = bundle.add(
                "html",
                new Markdown(
                    IOUtils.toString(attachment.read(), CharEncoding.UTF_8)
                ).html()
            ).up();
        }
        return bundle;
    }

    /**
     * Convert message to bundle.
     * @param message Message
     * @return Bundle
     * @throws IOException In case of failure
     */
    private JaxbBundle bundle(final Message message) throws IOException {
        return new JaxbBundle("message")
            .add("number", Long.toString(message.number()))
            .up()
            .add("author", message.author()).up()
            .add("html", message.text()).up()
            .add("timeago", new PrettyTime().format(message.date())).up()
            .add(
                "date",
                DateFormatUtils.ISO_DATETIME_FORMAT.format(message.date())
            )
            .up()
            .link(
                new Link(
                    "photo",
                    this.uriInfo().getBaseUriBuilder().clone()
                        .path(FriendRs.class)
                        .path(FriendRs.class, "png")
                        .build(message.author())
                        .toString()
                )
            )
            .link(
                new Link(
                    "more",
                    this.uriInfo().getRequestUriBuilder().clone()
                        .replaceQueryParam("start", "{x}")
                        .build(message.number())
                )
            );
    }

}
