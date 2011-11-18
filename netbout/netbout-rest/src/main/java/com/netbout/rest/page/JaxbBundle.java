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
package com.netbout.rest.page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * JAXB bundle.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class JaxbBundle {

    /**
     * Parent bundle, if exists.
     */
    private final JaxbBundle parent;

    /**
     * Name of it.
     */
    private final String name;

    /**
     * Text content of it.
     */
    private final String content;

    /**
     * Children.
     */
    private final List<JaxbBundle> children = new ArrayList<JaxbBundle>();

    /**
     * Attributes.
     */
    private final Map<String, String> attrs = new HashMap<String, String>();

    /**
     * Default ctor, for JAXB.
     */
    public JaxbBundle() {
        throw new IllegalStateException("illegal call");
    }

    /**
     * Public ctor.
     * @param nam The name of it
     */
    public JaxbBundle(final String nam) {
        this.parent = null;
        this.name = nam;
        this.content = null;
    }

    /**
     * Public ctor.
     * @param nam The name of it
     * @param text The content
     */
    public JaxbBundle(final String nam, final String text) {
        this.parent = null;
        this.name = nam;
        this.content = text;
    }

    /**
     * Public ctor.
     * @param prnt Parent bundle
     * @param nam The name of it
     * @param text The content
     */
    private JaxbBundle(final JaxbBundle prnt, final String nam,
        final String text) {
        this.parent = prnt;
        this.name = nam;
        this.content = text;
    }

    /**
     * Add new child.
     * @param nam The name of child
     * @return This object
     */
    public JaxbBundle add(final String nam) {
        return this.add(nam, "");
    }

    /**
     * Add new child with text value.
     * @param nam The name of child
     * @param txt The text
     * @return This object
     */
    public JaxbBundle add(final String nam, final Object txt) {
        if (txt == null) {
            throw new IllegalArgumentException(
                String.format(
                    "Can't add(%s, NULL) to '%s'",
                    nam,
                    this.name
                )
            );
        }
        final JaxbBundle child = new JaxbBundle(this, nam, txt.toString());
        this.children.add(child);
        return child;
    }

    /**
     * Add attribute.
     * @param nam The name of attribute
     * @param val The value
     * @return This object
     */
    public JaxbBundle attr(final String nam, final Object val) {
        this.attrs.put(nam, val.toString());
        return this;
    }

    /**
     * Return parent.
     * @return The parent bundle
     */
    public JaxbBundle up() {
        return this.parent;
    }

    /**
     * Get DOM element.
     * @return The element
     */
    public Element element() {
        if (this.parent != null) {
            throw new IllegalArgumentException(
                "You can convert only top level JaxbBundle to DOM"
            );
        }
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (javax.xml.parsers.ParserConfigurationException ex) {
            throw new IllegalStateException(ex);
        }
        final Document doc = builder.newDocument();
        return this.element(doc);
    }

    /**
     * Get DOM element.
     * @param doc The document
     * @return The element
     */
    private Element element(final Document doc) {
        final Element element = doc.createElement(this.name);
        for (Map.Entry<String, String> attr : this.attrs.entrySet()) {
            element.setAttribute(attr.getKey(), attr.getValue());
        }
        for (JaxbBundle child : this.children) {
            element.appendChild(child.element(doc));
        }
        element.appendChild(doc.createTextNode(this.content));
        return element;
    }

}
