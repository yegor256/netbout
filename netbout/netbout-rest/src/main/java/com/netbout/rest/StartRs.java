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
 * incident to the author by email: privacy@netbout.com.
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

// bout manipulation engine from com.netbout:netbout-engine
import com.netbout.engine.Bout;
import com.netbout.engine.BoutFactory;
import com.netbout.engine.impl.DefaultBoutFactory;

// JAXB implemented data manipulators
import com.netbout.rest.jaxb.PageStart;

// for JAX-RS
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * Start new bout.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Path("/new")
public final class StartRs extends AbstractRs {

    /**
     * Bout manipulation factory.
     */
    private final BoutFactory factory;

    /**
     * Public ctor.
     */
    public StartRs() {
        this(new DefaultBoutFactory());
    }

    /**
     * Ctor for unit testing.
     * @param fct The factory
     */
    protected StartRs(final BoutFactory fct) {
        this.factory = fct;
    }

    /**
     * Start new bout.
     * @return JAX-RS response
     */
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public PageStart start() {
        return new PageStart();
    }

    /**
     * Start new bout.
     * @param identity The identity to use as creator
     * @param title The title of the bout
     * @return JAX-RS response
     */
    @POST
    public Response start(@QueryParam("i") final String identity,
        @QueryParam("t") final String title) {
        final Bout bout = this.factory.create(
            this.user().identity(identity),
            title
        );
        final UriBuilder builder = this.uriInfo().getAbsolutePathBuilder()
            .path(ListRs.class, "bout");
        return Response
            .created(builder.build(bout.number()))
            .build();
    }

}
