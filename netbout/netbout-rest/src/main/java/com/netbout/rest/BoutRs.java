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

import com.netbout.engine.Bout;
import com.netbout.rest.jaxb.PageOfBout;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * RESTful front of one Bout. The class is instantiated from {@link ListRs}.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Path("{id: \\d+}")
public final class BoutRs extends AbstractRs {

    /**
     * The bout to work with.
     */
    private final Bout bout;

    /**
     * Public ctor.
     * @param boutId ID of the bout to work with
     */
    public BoutRs(@PathParam("id") final Long boutId) {
        super();
        this.bout = this.builder().getBoutFactory().find(boutId);
    }

    /**
     * Ctor for unit testing.
     * @param builder The factory builder
     * @param boutId ID of the bout
     */
    protected BoutRs(final FactoryBuilder builder, final Long boutId) {
        super(builder);
        this.bout = this.builder().getBoutFactory().find(boutId);
    }

    /**
     * Get bout data.
     * @return The bout, convertable to XML
     */
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public PageOfBout bout() {
        return new PageOfBout(this.bout);
    }

}
