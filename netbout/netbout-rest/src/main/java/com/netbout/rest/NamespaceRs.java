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

import java.io.InputStream;
import java.net.URI;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.io.IOUtils;

/**
 * Front-end of all namespaces in WOQUO.com domain.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Path("/ns/{name : [a-zA-Z/]+}.xsd")
public final class NamespaceRs {

    /**
     * Namespace XSD page.
     * @param info Meta information about the request
     * @param name Name of namespace
     * @return The XSD schema
     */
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public String xsd(@Context final UriInfo info,
        @PathParam("name") final String name) {
        final String element = name.substring(name.lastIndexOf("/") + 1);
        final String namespace = String.format("urn:netbout:ns:%s", name);
        final URI uri = info.getBaseUriBuilder()
            .path("/ns/{name}.xsd/o")
            .build(name);
        // @checkstyle StringLiteralsConcatenation (8 lines)
        return "<?xml version='1.0'?>"
            + "<xs:schema version='1.0' elementFormDefault='qualified'"
            + String.format(" targetNamespace='%s'", namespace)
            + String.format(" xmlns:o='%s'", namespace)
            + " xmlns:xs='http://www.w3.org/2001/XMLSchema'>"
            + String.format("<xs:include schemaLocation='%s' />", uri)
            + String.format("<xs:element name='%s' type='o:%1$s' />", element)
            + "</xs:schema>";
    }

    /**
     * Original XSD document, with root type declaration.
     * @param name Name of namespace
     * @return The XSD schema
     */
    @GET
    @Path("/o")
    @Produces(MediaType.APPLICATION_XML)
    public String original(@PathParam("name") final String name) {
        final String path = String.format("/com/netbout/%s.xsd", name);
        final InputStream stream = this.getClass().getResourceAsStream(path);
        if (stream == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        try {
            return IOUtils.toString(stream);
        } catch (java.io.IOException ex) {
            throw new WebApplicationException(
                ex, Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

}
