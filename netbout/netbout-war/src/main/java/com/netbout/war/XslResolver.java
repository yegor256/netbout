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
package com.netbout.war;

// for JAX-RS
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

// JAXB
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

/**
 * Replace standard marshaller.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @link <a href="http://markmail.org/search/?q=list%3Anet.java.dev.jersey.users+ContextResolver%3CMarshaller%3E#query:list%3Anet.java.dev.jersey.users%20ContextResolver%3CMarshaller%3E+page:1+mid:q4fkq6eqlgkzdodc+state:results">discussion</a>
 */
@Provider
@Produces(MediaType.APPLICATION_XML)
public final class XslResolver implements ContextResolver<Marshaller> {

    /**
     * JAXB context.
     */
    private final JAXBContext context;

    /**
     * Public ctor.
     */
    public XslResolver() {
        try {
            this.context = JAXBContext.newInstance("com.netbout.rest.jaxb");
        } catch (javax.xml.bind.JAXBException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Marshaller getContext(final Class<?> type) {
        try {
            final Marshaller mrsh = this.context.createMarshaller();
            mrsh.setProperty(Marshaller.JAXB_FRAGMENT, true);
            mrsh.setProperty(
                "com.sun.xml.bind.xmlHeaders",
                "<?xml version='1.0'?>"
                + "<?xml-stylesheet href='/xsl/"
                + type.getSimpleName()
                + ".xsl'?>"
            );
            return mrsh;
        } catch (javax.xml.bind.JAXBException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
