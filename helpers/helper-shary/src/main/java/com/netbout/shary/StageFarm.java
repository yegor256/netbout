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
package com.netbout.shary;

import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import com.netbout.spi.NetboutUtils;
import com.netbout.spi.Urn;
import com.netbout.spi.cpa.CpaUtils;
import com.netbout.spi.cpa.Farm;
import com.netbout.spi.cpa.IdentityAware;
import com.netbout.spi.cpa.Operation;
import com.netbout.spi.xml.JaxbParser;
import com.netbout.spi.xml.JaxbPrinter;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;

/**
 * Stage farm.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Farm
@SuppressWarnings({
    "PMD.AvoidInstantiatingObjectsInLoops",
    "PMD.UseConcurrentHashMap",
    "PMD.TooManyMethods"
})
public final class StageFarm implements IdentityAware {

    /**
     * Me.
     */
    private transient Identity identity;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final Identity idnt) {
        this.identity = idnt;
    }

    /**
     * Does this stage exist in the bout?
     * @param number Bout where it is happening
     * @param stage Name of stage to render
     * @return Does it?
     */
    @Operation("does-stage-exist")
    public Boolean doesStageExist(final Long number, final Urn stage) {
        Boolean exists = null;
        if (this.identity.name().equals(stage)) {
            exists = Boolean.TRUE;
        }
        return exists;
    }

    /**
     * Get XML of the stage.
     * @param number Bout where it is happening
     * @param author Who is viewing this stage now
     * @param stage Name of stage to render
     * @param place The place in the stage to render
     * @return The XML document
     * @throws Exception If some problem inside
     * @checkstyle ParameterNumber (4 lines)
     */
    @Operation("render-stage-xml")
    public String renderStageXml(final Long number, final Urn author,
        final Urn stage, final String place) throws Exception {
        String xml = null;
        if (this.identity.name().equals(stage)) {
            final Bout bout = this.identity.bout(number);
            final Stage data = new Stage(place);
            data.add(this.extend(author, this.documents(bout)));
            xml = new JaxbPrinter(data).print();
        }
        return xml;
    }

    /**
     * Resolve namespace.
     * @param namespace The namespace
     * @return Its URL
     * @throws Exception If some problem inside
     */
    @Operation("resolve-xml-namespace")
    public URL resolveXmlNamespace(final Urn namespace) throws Exception {
        URL url = null;
        if (namespace.equals(Urn.create("urn:netbout:ns:shary/Slip"))) {
            url = new URL(
                namespace.toString().replaceAll(
                    "urn:netbout:ns:(.*)",
                    "http://www.netbout.com/ns/$1.xsd"
                )
            );
        }
        return url;
    }

    /**
     * Process POST request of the stage.
     * @param number Bout where it is happening
     * @param author Author of the message
     * @param stage Name of stage to render
     * @param place The place in the stage to render
     * @param body Body of POST request
     * @return New place in this stage
     * @throws Exception If some problem inside
     * @checkstyle ParameterNumber (5 lines)
     */
    @Operation("stage-post-request")
    public String stagePostRequest(final Long number, final Urn author,
        final Urn stage, final String place, final String body)
        throws Exception {
        String dest = null;
        if (this.identity.name().equals(stage)) {
            final Slip slip = this.slip(author, body);
            if (slip.getName().isEmpty() || slip.getUri().isEmpty()) {
                dest = "empty-args";
            } else if (slip.getUri().matches("(http|s3)://.*")) {
                dest = "";
                this.identity.bout(number).post(new JaxbPrinter(slip).print());
            } else {
                dest = "illegal-uri";
            }
        }
        return dest;
    }

    /**
     * Change place after rendering, if necessary.
     * @param number Bout where it is happening
     * @param author Author of the message
     * @param stage Name of stage to render
     * @param place The place in the stage to render
     * @return New place in this stage
     * @throws Exception If some problem inside
     * @checkstyle ParameterNumber (5 lines)
     */
    @Operation("post-render-change-place")
    public String postRenderChangePlace(final Long number, final Urn author,
        final Urn stage, final String place)
        throws Exception {
        String dest = null;
        if (this.identity.name().equals(stage)) {
            dest = "";
        }
        return dest;
    }

    /**
     * Process document requests.
     * @param number Bout where it is happening
     * @param author Who is posting
     * @param stage Name of stage to render
     * @param base Base URI of the stage, e.g. "http://www.netbout.com/123/s/"
     * @param path Relative path inside this URI, e.g. "/test.xsd"
     * @return HTTP response full body
     * @throws Exception If some problem inside
     * @checkstyle ParameterNumber (5 lines)
     */
    @Operation("render-stage-resource")
    public String renderStageResource(final Long number, final Urn author,
        final Urn stage, final URL base, final String path)
        throws Exception {
        String response = null;
        if (this.identity.name().equals(stage)) {
            final String[] parts = path.split(":", 2);
            final Bout bout = this.identity.bout(number);
            final Collection<SharedDoc> docs = this.documents(bout);
            SharedDoc found = null;
            for (SharedDoc doc : docs) {
                if (doc.getName().equals(parts[1])) {
                    found = doc;
                    break;
                }
            }
            if (found == null) {
                throw new IllegalArgumentException(
                    String.format("Document '%s' not found", parts[1])
                );
            }
            if ("/load".equals(parts[0])) {
                response = String.format("through %s", found.getUri());
            } else if ("/un".equals(parts[0])) {
                this.identity.bout(number).post(
                    new JaxbPrinter(
                        new Slip(
                            false,
                            "",
                            author.toString(),
                            found.getName()
                        )
                    ).print()
                );
                response = "home";
            }
        }
        return response;
    }

    /**
     * Get XML of the stage.
     * @param number Bout where it is happening
     * @param stage Name of stage to render
     * @return The XML document
     * @throws Exception If some problem inside
     */
    @Operation("render-stage-xsl")
    public String renderStageXsl(final Long number, final Urn stage)
        throws Exception {
        String xsl = null;
        if (this.identity.name().equals(stage)) {
            xsl = IOUtils.toString(
                this.getClass().getResourceAsStream("stage.xsl"),
                CharEncoding.UTF_8
            );
        }
        return xsl;
    }

    /**
     * Parse incoming Http BODY.
     * @param author Who is posting
     * @param body The body
     * @return The slip
     */
    private static Slip slip(final Urn author, final String body) {
        final Map<String, String> args = CpaUtils.decodeBody(body);
        return new Slip(
            true,
            args.get("uri"),
            author.toString(),
            args.get("name")
        );
    }

    /**
     * Attach links to all documents.
     * @param viewer Who is viewing
     * @param docs The documents
     * @return The same array of them
     */
    private Collection<SharedDoc> extend(final Urn viewer,
        final Collection<SharedDoc> docs) {
        for (SharedDoc doc : docs) {
            doc.add(
                new Link(
                    "load",
                    UriBuilder.fromPath("load:{name}").build(doc.getName())
                )
            );
            if (doc.getAuthor().equals(viewer.toString())) {
                doc.add(
                    new Link(
                        "unshare",
                        UriBuilder.fromPath("un:{name}").build(doc.getName())
                    )
                );
            }
            try {
                doc.setAlias(
                    NetboutUtils.aliasOf(
                        this.identity.friend(Urn.create(doc.getAuthor()))
                    )
                );
            } catch (com.netbout.spi.UnreachableUrnException ex) {
                doc.setAlias("somebody");
            }
        }
        return docs;
    }

    /**
     * Load all documents from the bout.
     * @param bout The bout to work with
     * @return The list of them
     */
    private static Collection<SharedDoc> documents(final Bout bout) {
        final Iterable<Message> inbox = bout.messages(
            String.format("(ns '%s')", Slip.NAMESPACE)
        );
        final Map<String, SharedDoc> docs =
            new HashMap<String, SharedDoc>();
        final Set<String> stops = new HashSet<String>();
        for (Message msg : inbox) {
            final Slip slip = new JaxbParser(msg.text()).parse(Slip.class);
            if (!slip.isAllow() || slip.getUri().isEmpty()) {
                stops.add(slip.getName());
                continue;
            }
            if (stops.contains(slip.getName())) {
                continue;
            }
            if (docs.containsKey(slip.getName())) {
                continue;
            }
            docs.put(slip.getName(), new SharedDoc(slip));
        }
        return docs.values();
    }

}
