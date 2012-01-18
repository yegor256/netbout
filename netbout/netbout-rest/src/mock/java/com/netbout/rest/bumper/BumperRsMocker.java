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
package com.netbout.rest.bumper;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

/**
 * Bumper front page.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Path("/bumper")
public final class BumperRsMocker {

    /**
     * Set URI Info.
     * @param info The info to inject
     */
    @Context
    public void setUriInfo(final UriInfo info) {
        BumperFarmMocker.setHome(info.getBaseUri());
    }

    /**
     * Namespace page.
     * @return The content
     */
    @GET
    @Path("/ns")
    @Produces(MediaType.TEXT_PLAIN)
    public String namespace() {
        return "try /bumper/ns.xsd instead";
    }

    /**
     * XSD page.
     * @return The XSD
     */
    @GET
    @Path("/ns.xsd")
    @Produces(MediaType.TEXT_XML)
    public String xsd() {
        // @checkstyle StringLiteralsConcatenation (3 lines)
        return "<?xml version='1.0'?><xs:schema"
            + " xmlns:xs='http://www.w3.org/2001/XMLSchema'"
            + " xmlns='/bumper/ns' elementFormDefault='qualified'"
            + " targetNamespace='/bumper/ns'>"
            + "<xs:element name='bump' type='bump'/>"
            + "<xs:complexType name='bump'>"
            + "<xs:sequence>"
            + "<xs:element name='text' type='xs:string'/>"
            + "</xs:sequence>"
            + "</xs:complexType>"
            + "</xs:schema>";
    }

}
