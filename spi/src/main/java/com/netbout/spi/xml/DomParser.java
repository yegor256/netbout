/**
 * Copyright (c) 2009-2011, NetBout.com
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
import java.io.StringWriter;
import java.net.URL;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
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
public final class DomParser {

    /**
     * DOM factory.
     */
    protected static final DocumentBuilderFactory FACTORY =
        DocumentBuilderFactory.newInstance();

    /**
     * Static configuration.
     */
    static {
        DomParser.FACTORY.setNamespaceAware(true);
    }

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
     */
    public Document parse() {
        Document dom;
        try {
            dom = this.FACTORY
                .newDocumentBuilder()
                .parse(IOUtils.toInputStream(xml, CharEncoding.UTF_8));
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
     * Rename this node and all children.
     * @param dom The XML document
     * @param node The node to rename
     * @param actual Original namespace
     * @param required New namespace
     * @checkstyle ParameterNumber (4 lines)
     */
    protected static void rename(final Document dom, final Node node,
        final Urn actual, final Urn required) {
        if (node.getNodeType() == Node.ELEMENT_NODE
            && node.getNamespaceURI().equals(actual.toString())) {
            dom.renameNode(node, required.toString(), node.getNodeName());
        }
        final NodeList list = node.getChildNodes();
        for (int pos = 0; pos < list.getLength(); pos += 1) {
            DomParser.rename(dom, list.item(pos), actual, required);
        }
    }

}
