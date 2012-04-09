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

import com.rexsl.page.JaxbBundle;
import com.rexsl.page.PageBuilder;
import com.ymock.util.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * Miscellaneous pages.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Path("/m")
public final class MiscRs extends AbstractRs {

    /**
     * Get "error" page by code.
     * @param code Error code
     * @return The JAX-RS response
     * @todo #122 Should be implemented nicely,
     *  see http://stackoverflow.com/questions/8179547
     */
    @GET
    @Path("/{code : \\d{3}}")
    public Response error(@PathParam("code") final Integer code) {
        final Response.Status status = Response.Status.fromStatusCode(code);
        String message = "unknown";
        if (status != null) {
            message = status.toString();
        }
        Logger.debug(
            this,
            "#error(#%d): at '%s'",
            code,
            this.uriInfo().getAbsolutePath()
        );
        return new PageBuilder()
            .stylesheet("/xsl/error.xsl")
            .build(BasePage.class)
            .init(this, false)
            .append(
                new JaxbBundle("error")
                    .add("code", code)
                    .up()
                    .add("message", message)
                    .up()
            )
            .render()
            .preserved()
            .status(code)
            .build();
    }

    /**
     * Get "error" page by code, when with POST.
     * @param code Error code
     * @return The JAX-RS response
     */
    @POST
    @Path("/{code : \\d{3}}")
    public Response errorByPost(@PathParam("code") final Integer code) {
        return this.error(code);
    }

}
