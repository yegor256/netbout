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
package com.netbout.hub.predicates.xml;

import com.netbout.hub.Hub;
import com.netbout.hub.Predicate;
import com.netbout.hub.PredicateException;
import com.netbout.hub.predicates.AbstractVarargPred;
import com.netbout.spi.Message;
import com.ymock.util.Logger;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;
import org.w3c.dom.Document;

/**
 * DOM wrapper around text.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class DomText {

    /**
     * The document builder.
     */
    private final transient DocumentBuilder builder;

    /**
     * The error handler.
     */
    private final transient DomErrorHandler handler = new DomErrorHandler();

    /**
     * The text.
     */
    private final transient String text;

    /**
     * The document, if created by {@link #dom()}.
     */
    private transient Document document;

    /**
     * Public ctor.
     * @param txt The text
     */
    public DomText(final String txt) {
        final DocumentBuilderFactory factory =
            DocumentBuilderFactory.newInstance();
        try {
            // @checkstyle LineLength (1 line)
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setNamespaceAware(true);
            factory.setValidating(true);
            factory.setAttribute(
                "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                "http://www.w3.org/2001/XMLSchema"
            );
            this.builder = factory.newDocumentBuilder();
        } catch (javax.xml.parsers.ParserConfigurationException ex) {
            throw new IllegalStateException(ex);
        }
        this.builder.setErrorHandler(this.handler);
        this.text = txt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.text;
    }

    /**
     * Is it XML?
     * @return Is it?
     */
    public boolean isXml() {
        return !this.text.isEmpty() && this.text.charAt(0) == '<';
    }

    /**
     * Get its namespace.
     * @return The namespace
     * @throws DomValidationException If some problem inside
     */
    public String namespace() throws DomValidationException {
        return this.dom().getDocumentElement().getNamespaceURI();
    }

    /**
     * Validate and throw exception if not valid.
     * @param hub The hub with helpers
     * @throws DomValidationException If some problem inside
     */
    public void validate(final Hub hub) throws DomValidationException {
        final String namespace = this.namespace();
        final String uri = hub.make("resolve-xml-namespace")
            .arg(namespace)
            .asDefault("")
            .exec();
        if (uri.isEmpty()) {
            throw new DomValidationException(
                String.format(
                    "Namespace '%s' is not supported by helpers",
                    namespace
                )
            );
        }
        final String schema = this.schema(namespace);
        if (!uri.equals(schema)) {
            throw new DomValidationException(
                String.format(
                    "XML Schema for namespace '%s' should be '%s' (not '%s')",
                    namespace,
                    uri,
                    schema
                )
            );
        }
    }

    /**
     * Build a DOM document and return it.
     * @return The document
     * @throws DomValidationException If some problem inside
     */
    private Document dom() throws DomValidationException {
        synchronized (this) {
            if (this.document == null) {
                try {
                    this.document = this.builder.parse(
                        IOUtils.toInputStream(text, CharEncoding.UTF_8)
                    );
                } catch (java.io.IOException ex) {
                    throw new IllegalArgumentException(ex);
                } catch (org.xml.sax.SAXException ex) {
                    throw new IllegalArgumentException(ex);
                }
                if (!this.handler.isEmpty()) {
                    throw new DomValidationException(
                        Logger.format("%[list]s", this.handler.exceptions())
                    );
                }
            }
            return this.document;
        }
    }

    /**
     * URI of the schema.
     * @param namespace The namespace we're looking for
     * @return The URI
     * @throws DomValidationException If some problem inside
     */
    public String schema(final String namespace) throws DomValidationException {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new DomContext());
        String location;
        try {
            location = xpath.evaluate("/*/@xsi:schemaLocation", this.dom());
        } catch (javax.xml.xpath.XPathExpressionException ex) {
            throw new DomValidationException(ex);
        }
        final String[] parts = location.replaceAll("[\t\n\r ]+", " ")
            .trim()
            .split(" ");
        if (parts.length != 2) {
            throw new DomValidationException(
                String.format(
                    "Just two parts are expected in schemaLocation: '%s'",
                    location
                )
            );
        }
        if (!parts[0].equals(namespace)) {
            throw new DomValidationException(
                String.format(
                    "Namespace '%s' should have a URL in schemaLocation '%s'",
                    namespace,
                    location
                )
            );
        }
        return parts[1];
    }

}
