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
package com.netbout.hub.hh;

import com.netbout.hub.HubStats;
import com.netbout.spi.Identity;
import com.netbout.spi.Urn;
import com.netbout.spi.cpa.Farm;
import com.netbout.spi.cpa.IdentityAware;
import com.netbout.spi.cpa.Operation;
import com.ymock.util.Logger;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;
import org.w3c.dom.Document;

/**
 * Stats.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Farm
public final class StatsFarm implements IdentityAware {

    /**
     * Stats from Hub.
     */
    private static HubStats stats;

    /**
     * Me.
     */
    private transient Identity identity;

    /**
     * Set data provider.
     * @param sts The stats
     */
    public static void setHubStats(final HubStats sts) {
        StatsFarm.stats = sts;
        Logger.debug(
            StatsFarm.class,
            "#setHubStats('%[type]s'): injected",
            sts
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final Identity idnt) {
        this.identity = idnt;
        Logger.debug(
            this,
            "#init('%s'): injected",
            this.identity.name()
        );
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
        Logger.debug(
            this,
            "#doesStageExist(#%d, '%s'): %B returned",
            number,
            stage,
            exists
        );
        return exists;
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
        if (StatsFarm.stats == null) {
            throw new IllegalArgumentException("HubStats was never injected");
        }
        String xml = null;
        if (this.identity.name().equals(stage)) {
            final Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
            doc.appendChild(StatsFarm.stats.stats(doc));
            final Transformer transformer = TransformerFactory.newInstance()
                .newTransformer();
            final StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            xml = writer.toString();
            Logger.debug(
                this,
                "#renderStageXml(#%d, '%s', '%s'): %d chars delivered",
                number,
                stage,
                place,
                xml.length()
            );
        }
        return xml;
    }

    /**
     * Get XSL for the stage.
     * @param number Bout number
     * @param stage Name of the stage
     * @return The XSL source
     * @throws java.io.IOException If some problem inside
     */
    @Operation("render-stage-xsl")
    public String renderStageXsl(final Long number, final Urn stage)
        throws java.io.IOException {
        String xsl = null;
        if (this.identity.name().equals(stage)) {
            xsl = IOUtils.toString(
                this.getClass().getResourceAsStream("stage.xsl"),
                CharEncoding.UTF_8
            );
            Logger.debug(
                this,
                "#renderStageXsl('%s'): %d chars delivered",
                stage,
                xsl.length()
            );
        }
        return xsl;
    }

}
