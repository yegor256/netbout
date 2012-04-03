/**
 * Copyright (c) 2009-2012, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the NetBout.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netbout.spi.xml;

import com.netbout.spi.Urn;
import com.ymock.util.Logger;
import java.net.URL;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import org.w3c.dom.Document;

/**
 * Converts a JAXB-annotated object to XML.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class JaxbPrinter {

    /**
     * The object.
     */
    private final transient Object object;

    /**
     * Public ctor.
     * @param obj The object to convert
     */
    public JaxbPrinter(final Object obj) {
        this.object = obj;
    }

    /**
     * Print the XML document.
     * @return The document
     */
    public String print() {
        return this.print("");
    }

    /**
     * Print the XML document, adding a suffix to its namespace.
     * @param suffix Optional suffix for a namespace
     * @return The document
     */
    public String print(final String suffix) {
        final Urn namespace = this.namespace(this.object.getClass());
        if (namespace.isEmpty() && !suffix.isEmpty()) {
            throw new IllegalArgumentException(
                Logger.format(
                    "Can't add '%s' suffix to empty namespace document",
                    suffix
                )
            );
        }
        return this.print(
            Urn.create(String.format("%s%s", namespace, suffix))
        );
    }

    /**
     * Print the XML document, changing the namespace.
     * @param required New namespace to set there
     * @return The document
     */
    public String print(final Urn required) {
        final Document dom = this.marshall();
        final Urn namespace = this.namespace(this.object.getClass());
        if (!namespace.isEmpty()) {
            if (!namespace.equals(required)) {
                DomParser.rename(
                    dom,
                    dom.getDocumentElement(),
                    namespace.toString(),
                    required
                );
            }
            final SchemaLocation schema = (SchemaLocation) this.object
                .getClass()
                .getAnnotation(SchemaLocation.class);
            if (schema == null) {
                throw new IllegalStateException(
                    Logger.format(
                        "@SchemaLocation annotation is absent at %[type]s",
                        this.object
                    )
                );
            }
            URL location;
            try {
                location = new URL(schema.value());
            } catch (java.net.MalformedURLException ex) {
                throw new IllegalStateException(
                    Logger.format(
                        "Invalid URL '%s' for schemaLocation in %[type]s",
                        schema.value(),
                        this.object
                    )
                );
            }
            dom.getDocumentElement().setAttributeNS(
                "http://www.w3.org/2001/XMLSchema-instance",
                "xsi:schemaLocation",
                String.format("%s %s", required, location)
            );
        }
        return new DomPrinter(dom).print();
    }

    /**
     * Convert object to DOM document.
     * @return The document
     */
    private Document marshall() {
        JAXBContext ctx;
        try {
            ctx = JAXBContext.newInstance(this.object.getClass());
        } catch (javax.xml.bind.JAXBException ex) {
            throw new IllegalArgumentException(ex);
        }
        Marshaller mrsh;
        try {
            mrsh = ctx.createMarshaller();
        } catch (javax.xml.bind.JAXBException ex) {
            throw new IllegalStateException(ex);
        }
        Document dom;
        try {
            dom = DomParser.factory().newDocumentBuilder().newDocument();
        } catch (javax.xml.parsers.ParserConfigurationException ex) {
            throw new IllegalStateException(ex);
        }
        final Urn namespace = this.namespace(this.object.getClass());
        final XmlType annot = (XmlType) this.object.getClass()
            .getAnnotation(XmlType.class);
        QName qname;
        if (namespace.isEmpty()) {
            qname = new QName("", annot.name());
        } else {
            qname = new QName(namespace.toString(), annot.name());
        }
        try {
            mrsh.marshal(
                new JAXBElement(qname, this.object.getClass(), this.object),
                dom
            );
        } catch (javax.xml.bind.JAXBException ex) {
            throw new IllegalArgumentException(ex);
        }
        return dom;
    }

    /**
     * Get namespace of this object (it has to be static becuase it works
     * with Class, not an Object and is used from other classes in this form).
     * @param type The type
     * @return The namespace of it
     */
    public static Urn namespace(final Class type) {
        final XmlType annot = (XmlType) type.getAnnotation(XmlType.class);
        if (annot == null) {
            throw new IllegalArgumentException(
                String.format(
                    "Object of type '%s' is not @XmlType annotated entity",
                    type.getName()
                )
            );
        }
        Urn namespace;
        if ("##default".equals(annot.namespace())) {
            namespace = new Urn();
        } else {
            try {
                namespace = new Urn(annot.namespace());
            } catch (java.net.URISyntaxException ex) {
                throw new IllegalArgumentException(
                    Logger.format(
                        "Invalid format of namespace in '%s'",
                        type.getName()
                    ),
                    ex
                );
            }
        }
        return namespace;
    }

}
