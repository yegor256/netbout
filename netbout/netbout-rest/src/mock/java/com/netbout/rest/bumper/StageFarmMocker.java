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

import com.netbout.spi.Identity;
import com.netbout.spi.Urn;
import com.netbout.spi.cpa.Farm;
import com.netbout.spi.cpa.IdentityAware;
import com.netbout.spi.cpa.Operation;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;

/**
 * The stage.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Farm
public final class StageFarmMocker implements IdentityAware {

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
     * Process POST request of the stage.
     * @param number Bout where it is happening
     * @param stage Name of stage to render
     * @param place The place in the stage to render
     * @param body Body of POST request
     * @throws Exception If some problem inside
     */
    @Operation("stage-post-request")
    public void stagePostRequest(final Long number, final Urn stage,
        final String place, final String body) {
        if (this.identity.name().equals(stage)) {
            if (body.isEmpty()) {
                throw new IllegalArgumentException("body can't be empty");
            }
            try {
                this.identity.bout(number).post(
                    URLDecoder.decode(
                        body.substring("data=".length()),
                        CharEncoding.UTF_8
                    )
                );
            } catch (com.netbout.spi.BoutNotFoundException ex) {
                throw new IllegalArgumentException(ex);
            } catch (com.netbout.spi.MessagePostException ex) {
                throw new IllegalArgumentException(ex);
            } catch (java.io.UnsupportedEncodingException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
    }

    /**
     * Get XML of the stage.
     * @param number Bout where it is happening
     * @param stage Name of stage to render
     * @param place The place in the stage to render
     * @return The XML document
     * @throws Exception If some problem inside
     */
    @Operation("render-stage-xml")
    public String renderStageXml(final Long number, final Urn stage,
        final String place) throws Exception {
        String xml = null;
        if (this.identity.name().equals(stage)) {
            xml = "<data/>";
        }
        return xml;
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
            xsl = this.res("bumper.xsl");
        }
        return xsl;
    }

    /**
     * Get XML of the stage.
     * @param number Bout where it is happening
     * @param stage Name of stage to render
     * @param base Base URI of the stage, e.g.
     *  "http://www.netbout.com/123/urn:test:shary/"
     * @param path Relative path inside this URI, e.g. "/test.xsd"
     * @return HTTP response full body
     * @throws Exception If some problem inside
     */
    @Operation("render-stage-resource")
    public String renderStageResource(final Long number, final Urn stage,
        final URL base, final String path)
        throws Exception {
        String response = null;
        if (this.identity.name().equals(stage)) {
            response = this.res(path);
        }
        return response;
    }

    /**
     * Local test resource.
     * @param name The name of it (inside
     *  {@code src/test/resources/com/netbout/rest/bumper})
     */
    private String res(final String name) {
        try {
            return IOUtils.toString(
                this.getClass().getResourceAsStream(name),
                CharEncoding.UTF_8
            );
        } catch (java.io.IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

}
