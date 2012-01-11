/**
 * Copyright (c) 2009-2011, netBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code occasionally and without intent to use it, please report this
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

import com.netbout.rest.jaxb.Link;
import com.netbout.rest.jaxb.ShortBout;
import com.netbout.rest.page.JaxbBundle;
import com.netbout.rest.page.JaxbGroup;
import com.netbout.rest.page.PageBuilder;
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import com.netbout.spi.client.RestSession;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 * RESTful front of user's inbox.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Path("/")
public final class InboxRs extends AbstractRs {

    /**
     * Threshold param.
     */
    private static final String PERIOD_PARAM = "p";

    /**
     * Query to filter messages with.
     */
    private transient String query = "";

    /**
     * Viewpoint to use.
     */
    private transient String viewpoint;

    /**
     * Set filtering keyword.
     * @param keyword The query
     */
    @QueryParam(RestSession.QUERY_PARAM)
    public void setQuery(final String keyword) {
        if (keyword != null) {
            this.query = keyword;
        }
    }

    /**
     * Set viewpoint.
     * @param vpnt The viewpoint
     */
    @QueryParam(InboxRs.PERIOD_PARAM)
    public void setViewpoint(final String vpnt) {
        if (vpnt != null) {
            this.viewpoint = vpnt;
        }
    }

    /**
     * Get inbox.
     * @return The JAX-RS response
     */
    @GET
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Response inbox() {
        final Identity identity = this.identity();
        final List<ShortBout> bouts = new ArrayList<ShortBout>();
        final List<Link> periods = new ArrayList<Link>();
        final UriBuilder base = this.base()
            .queryParam(RestSession.QUERY_PARAM, this.query);
        Period period;
        if (this.viewpoint == null) {
            period = new Period();
        } else {
            period = Period.valueOf(this.viewpoint);
            periods.add(new Link("newest", base));
        }
        int counter = 0;
        int pos = 0;
        final List<Bout> inbox = identity.inbox(this.fullQuery());
        for (Bout bout : inbox) {
            pos += 1;
            if (counter > 2) {
                break;
            }
            if (period.add(this.date(bout))) {
                if (counter > 0) {
                    continue;
                }
                bouts.add(
                    new ShortBout(
                        bout,
                        this.base().path(String.format("/%d", bout.number())),
                        identity
                    )
                );
                continue;
            }
            periods.add(
                new Link(
                    "more",
                    period.title(),
                    base.queryParam(InboxRs.PERIOD_PARAM, period)
                )
            );
            period = period.next();
            counter += 1;
        }
        if (!period.isEmpty()) {
            periods.add(
                new Link(
                    "earliest",
                    String.format(
                        "%s (%d more)",
                        period.title(),
                        inbox.size() - pos
                    ),
                    base.queryParam(InboxRs.PERIOD_PARAM, period)
                )
            );
        }
        return new PageBuilder()
            .schema("")
            .stylesheet(this.base().path("/xsl/inbox.xsl"))
            .build(AbstractPage.class)
            .init(this)
            .append(new JaxbBundle("query", this.query))
            .append(new JaxbBundle("period", period.title()))
            .append(JaxbGroup.build(bouts, "bouts"))
            .append(JaxbGroup.build(periods, "periods"))
            .link("friends", this.base().path("/f"))
            .link("helper", this.base().path("/h"))
            .authenticated(identity)
            .build();
    }

    /**
     * Start new bout.
     * @return The JAX-RS response
     */
    @Path("/s")
    @GET
    public Response start() {
        final Identity identity = this.identity();
        final Bout bout = identity.start();
        return new PageBuilder()
            .build(AbstractPage.class)
            .init(this)
            .authenticated(identity)
            .entity(String.format("bout #%d created", bout.number()))
            .status(Response.Status.SEE_OTHER)
            .location(this.base().path("/{num}").build(bout.number()))
            .header("Bout-number", bout.number())
            .build();
    }

    /**
     * Create query with period.
     * @return The query
     */
    private String fullQuery() {
        String original;
        if (!this.query.isEmpty() && this.query.charAt(0) == '(') {
            original = this.query;
        } else {
            original = String.format("(matches '%s' $text)", this.query);
        }
        return String.format(
            "(and (greater-than $date '%s') (matches '%s' $text))",
            this.period.start(),
            original
        );
    }

    /**
     * Calculate date of the bout.
     * @param bout The bout
     * @return Recent date in it
     */
    private Date date(final Bout bout) {
        final String query = "(equal $pos 0)";
        final List<Message> msgs = bout.messages(query);
        Date date;
        if (msgs.isEmpty()) {
            date = bout.date();
        } else {
            date = max(bout.date(), msgs.get(0).date());
        }
        return date;
    }

}
