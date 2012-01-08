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

import com.netbout.hub.Predicate;
import com.netbout.hub.PredicateException;
import com.netbout.hub.predicates.AbstractVarargPred;
import com.netbout.spi.Message;
import com.ymock.util.Logger;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;
import org.w3c.dom.Document;

/**
 * Namespace predicate.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class NsPred extends AbstractVarargPred {

    /**
     * The factory.
     */
    private static final DocumentBuilderFactory FACTORY =
        DocumentBuilderFactory.newInstance();

    /**
     * Public ctor.
     * @param args The arguments
     */
    public NsPred(final List<Predicate> args) {
        super("ns", args);
        try {
            // @checkstyle LineLength (1 line)
            this.FACTORY.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            this.FACTORY.setNamespaceAware(true);
        } catch (javax.xml.parsers.ParserConfigurationException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object evaluate(final Message msg, final int pos)
        throws PredicateException {
        final String namespace = (String) this.arg(0).evaluate(msg, pos);
        final String text = (String) this.arg(1).evaluate(msg, pos);
        boolean result = false;
        if (!text.isEmpty() && text.charAt(0) == '<') {
            Document doc;
            try {
                doc = this.FACTORY
                    .newDocumentBuilder()
                    .parse(IOUtils.toInputStream(text, CharEncoding.UTF_8));
            } catch (java.io.IOException ex) {
                throw new IllegalArgumentException(ex);
            } catch (javax.xml.parsers.ParserConfigurationException ex) {
                throw new IllegalArgumentException(ex);
            } catch (org.xml.sax.SAXException ex) {
                throw new IllegalArgumentException(ex);
            }
            final String uri = doc.getDocumentElement().getNamespaceURI();
            result = namespace.equals(uri);
            Logger.debug(
                this,
                // @checkstyle LineLength (1 line)
                "#evaluate(): namespace '%s' required, '%s' found inside '%s': %B",
                namespace,
                uri,
                text,
                result
            );
        }
        return result;
    }

}
