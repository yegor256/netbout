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

import com.jcabi.log.Logger;
import com.netbout.spi.Urn;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parses text and builds DOM Document object.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.DefaultPackage")
public final class DomParser {

    /**
     * XML Schema Instance namespace.
     */
    private static final String XSI_NAMESPACE =
        "http://www.w3.org/2001/XMLSchema-instance";

    /**
     * The XML content.
     */
    private final transient String xml;

    /**
     * Public ctor.
     * @param txt The XML text
     */
    public DomParser(final String txt) {
        this.xml = txt;
    }

    /**
     * Parse the text and return an object.
     * @return The DOM tree object
     */
    public Document parse() {
        Document dom;
        try {
            dom = DomParser.factory()
                .newDocumentBuilder()
                .parse(IOUtils.toInputStream(this.xml, CharEncoding.UTF_8));
        } catch (java.io.IOException ex) {
            throw new IllegalArgumentException(ex);
        } catch (org.xml.sax.SAXException ex) {
            throw new IllegalArgumentException(ex);
        } catch (javax.xml.parsers.ParserConfigurationException ex) {
            throw new IllegalArgumentException(ex);
        }
        return dom;
    }

    /**
     * Validates this document for full XML compliance (if it's an XML document
     * at all).
     * @throws DomValidationException If it's not valid
     */
    public void validate() throws DomValidationException {
        if (this.isXml()) {
            final DocumentBuilderFactory factory = DomParser.factory();
            factory.setValidating(true);
            DocumentBuilder builder;
            try {
                builder = factory.newDocumentBuilder();
            } catch (javax.xml.parsers.ParserConfigurationException ex) {
                throw new IllegalStateException(ex);
            }
            final DomErrorHandler handler = new DomErrorHandler();
            builder.setErrorHandler(handler);
            try {
                builder.parse(
                    IOUtils.toInputStream(this.clean(), CharEncoding.UTF_8)
                );
            } catch (java.io.IOException ex) {
                throw new IllegalStateException(ex);
            } catch (org.xml.sax.SAXException ex) {
                throw new DomValidationException(ex);
            }
            if (!handler.isEmpty()) {
                throw new DomValidationException(
                    Logger.format("%[list]s", handler.exceptions())
                );
            }
        }
    }

    /**
     * This document is an XML?
     * @return Is it?
     */
    public boolean isXml() {
        return this.xml != null
            && !this.xml.isEmpty()
            && this.xml.charAt(0) == '<';
    }

    /**
     * The document belongs to this namespace?
     * @param urn The namespace to belong to
     * @return Does it belong?
     */
    public boolean belongsTo(final Urn urn) {
        boolean belongs = false;
        if (this.isXml()) {
            final String namespace = this.parse()
                .getDocumentElement()
                .getNamespaceURI();
            belongs = namespace != null && this.matches(urn, namespace);
        }
        return belongs;
    }

    /**
     * Get namespace of the document.
     * @return The namespace of it
     * @throws DomValidationException If ther is no namespace or its format is
     *  wrong
     */
    public Urn namespace() throws DomValidationException {
        final String namespace = this.parse()
            .getDocumentElement()
            .getNamespaceURI();
        if (namespace == null || namespace.isEmpty()) {
            throw new DomValidationException(
                "Root element should belong to some namespace"
            );
        }
        try {
            return new Urn(namespace);
        } catch (java.net.URISyntaxException ex) {
            throw new DomValidationException(ex);
        }
    }

    /**
     * Get {@code schemaLocation} for the given schema URN of this document.
     * @param namespace The namespace we're looking for
     * @return The URL of schema location
     * @throws DomValidationException If some problem inside
     * @see <a href="http://www.w3.org/TR/xmlschema-0/#schemaLocation">W3C on schemaLocation</a>
     */
    public URL schemaLocation(final Urn namespace)
        throws DomValidationException {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new DomContext());
        String location;
        try {
            location = xpath.evaluate("/*/@xsi:schemaLocation", this.parse());
        } catch (javax.xml.xpath.XPathExpressionException ex) {
            throw new DomValidationException(ex);
        }
        final String space = " ";
        final String[] parts = location.replaceAll("[\t\n\r ]+", space)
            .trim()
            .split(space);
        String found = null;
        for (int pos = 0; pos < parts.length; pos += 1) {
            if (this.matches(namespace, parts[pos])) {
                if (pos + 1 >= parts.length) {
                    throw new DomValidationException(
                        String.format(
                            "URL missed in schemaLocation for '%s'",
                            parts[pos]
                        )
                    );
                }
                found = parts[pos + 1];
            }
        }
        if (found == null) {
            throw new DomValidationException(
                String.format(
                    "schemaLocation '%s' doesn't know about '%s' namespace",
                    location,
                    namespace
                )
            );
        }
        try {
            return new URL(found);
        } catch (java.net.MalformedURLException ex) {
            throw new DomValidationException(ex);
        }
    }

    /**
     * Create and configure a factory.
     * @return The factory
     */
    static DocumentBuilderFactory factory() {
        final DocumentBuilderFactory factory =
            DocumentBuilderFactory.newInstance();
        try {
            // @checkstyle LineLength (1 line)
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setNamespaceAware(true);
            factory.setAttribute(
                "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                "http://www.w3.org/2001/XMLSchema"
            );
        } catch (javax.xml.parsers.ParserConfigurationException ex) {
            throw new IllegalStateException(ex);
        }
        return factory;
    }

    /**
     * Rename this node and all children.
     * @param dom The XML document
     * @param node The node to rename
     * @param actual Original namespace
     * @param required New namespace
     * @checkstyle ParameterNumber (4 lines)
     */
    static void rename(final Document dom, final Node node,
        final String actual, final Urn required) {
        if (node.getNodeType() == Node.ELEMENT_NODE
            && node.getNamespaceURI().equals(actual)) {
            dom.renameNode(node, required.toString(), node.getNodeName());
        }
        final NodeList list = node.getChildNodes();
        for (int pos = 0; pos < list.getLength(); pos += 1) {
            DomParser.rename(dom, list.item(pos), actual, required);
        }
    }

    /**
     * Actual namespace matches the canonical one.
     * @param canonical The namespace as it should be, without a suffix
     * @param actual The actual namespace
     * @return Actual is a variation of a canonical one
     */
    static boolean matches(final Urn canonical, final String actual) {
        boolean matches = false;
        if (!canonical.isEmpty() && actual != null) {
            matches = actual.matches(
                String.format("^\\Q%s\\E(\\?.*)?$", canonical.toString())
            );
        }
        return matches;
    }

    /**
     * Convert our XML into a new one, where namespace is cleaned from
     * a suffix ("?...").
     * @return Clean version of it
     * @throws DomValidationException If some problem
     */
    private String clean() throws DomValidationException {
        Document dom;
        try {
            dom = DomParser.factory()
                .newDocumentBuilder()
                .parse(IOUtils.toInputStream(this.xml, CharEncoding.UTF_8));
        } catch (java.io.IOException ex) {
            throw new IllegalArgumentException(ex);
        } catch (org.xml.sax.SAXException ex) {
            throw new DomValidationException(ex);
        } catch (javax.xml.parsers.ParserConfigurationException ex) {
            throw new IllegalArgumentException(ex);
        }
        final Urn namespace = this.namespace();
        if (namespace.hasParams()) {
            final Element root = dom.getDocumentElement();
            DomParser.rename(
                dom,
                root,
                namespace.toString(),
                namespace.pure()
            );
            root.setAttributeNS(
                this.XSI_NAMESPACE,
                "xsi:schemaLocation",
                root.getAttributeNS(
                    this.XSI_NAMESPACE,
                    "schemaLocation"
                ).replace(namespace.toString(), namespace.pure().toString())
            );
        }
        return new DomPrinter(dom).print();
    }

}
