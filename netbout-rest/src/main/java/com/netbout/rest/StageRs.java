/**
 * Copyright (c) 2009-2012, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
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

import com.netbout.rest.jet.JetBuilder;
import com.netbout.spi.Bout;
import com.rexsl.page.PageBuilder;
import java.net.URL;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;

/**
 * Stage dispatcher, instantiated by {@link BoutRs}.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (400 lines)
 */
public final class StageRs extends BaseRs {

    /**
     * The bout we're in.
     */
    private final transient Bout bout;

    /**
     * Stage coordinates.
     */
    private final transient StageCoordinates coords;

    /**
     * Public ctor.
     * @param bot The bout
     * @param crd Coordinates
     */
    public StageRs(final Bout bot, final StageCoordinates crd) {
        super();
        this.bout = bot;
        this.coords = crd;
    }

    /**
     * Render stage resource.
     * @param path The path of request
     * @return Raw HTTP response body
     */
    @GET
    @Path("{path : .*}")
    public Response get(@PathParam("path") final String path) {
        this.coords.normalize(this.hub(), this.bout);
        URL home;
        try {
            home = this.base()
                .path("/{num}/s")
                .build(this.bout.number())
                .toURL();
        } catch (java.net.MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
        final String response = this.hub().make("render-stage-resource")
            .synchronously()
            .inBout(this.bout)
            .arg(this.bout.number())
            .arg(this.identity().name())
            .arg(this.coords.stage())
            .arg(home)
            .arg(String.format("/%s", path))
            .asDefault("")
            .exec();
        if (response.isEmpty()) {
            throw new ForwardException(
                this,
                this.base(),
                String.format("resource '%s' not found", path)
            );
        }
        Response resp;
        if ("home".equals(response)) {
            resp = new PageBuilder()
                .build(NbPage.class)
                .init(this)
                .authenticated(this.identity())
                .status(Response.Status.SEE_OTHER)
                .location(this.base().path("/{bout}").build(this.bout.number()))
                .build();
        } else if (response.startsWith("through")) {
            final String url = response.substring("through ".length());
            resp = new JetBuilder(url).build();
        } else {
            resp = StageRs.build(response);
        }
        return resp;
    }

    /**
     * Post something to this stage.
     * @param body Raw HTTP post body
     * @return The JAX-RS response
     */
    @POST
    public Response post(final String body) {
        this.coords.normalize(this.hub(), this.bout);
        final String dest = this.hub().make("stage-post-request")
            .synchronously()
            .inBout(this.bout)
            .arg(this.bout.number())
            .arg(this.identity().name())
            .arg(this.coords.stage())
            .arg(this.coords.place())
            .arg(body)
            .asDefault("")
            .exec();
        return new PageBuilder()
            .build(NbPage.class)
            .init(this)
            .authenticated(this.identity())
            .status(Response.Status.SEE_OTHER)
            .location(
                this.base().path("/{num}")
                    .replaceQueryParam(BoutRs.PLACE_PARAM, "{dest}")
                    .build(this.bout.number(), dest)
            )
            .build();
    }

    /**
     * Build response from text.
     * @param body The raw body
     * @return The response
     */
    private static Response build(final String body) {
        final Response.ResponseBuilder builder = Response.ok();
        final String[] lines = StringUtils.splitPreserveAllTokens(body, '\n');
        int pos = 0;
        while (pos < lines.length) {
            final String line = lines[pos];
            pos += 1;
            if (line.isEmpty()) {
                break;
            }
            final String[] parts = StringUtils.split(line, ':');
            if (parts.length != 2) {
                throw new IllegalArgumentException(
                    String.format("unclear HTTP header '%s'", line)
                );
            }
            builder.header(parts[0].trim(), parts[1].trim());
        }
        final StringBuilder content = new StringBuilder();
        while (pos < lines.length) {
            content.append(lines[pos]);
            pos += 1;
            if (pos < lines.length) {
                content.append('\n');
            }
        }
        builder.entity(content.toString());
        return builder.build();
    }

}
