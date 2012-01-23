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
import com.netbout.spi.Urn;
import com.netbout.spi.cpa.Farm;
import com.netbout.spi.cpa.IdentityAware;
import com.netbout.spi.cpa.Operation;
import com.woquo.netbout.Jaxb;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;

/**
 * Stage farm.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Farm
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
     * @param stage Name of stage to render
     * @param place The place in the stage to render
     * @return The XML document
     * @throws Exception If some problem inside
     */
    @Operation("render-stage-xml")
    @SuppressWarnings({
        "PMD.AvoidInstantiatingObjectsInLoops", "PMD.UseConcurrentHashMap"
    })
    public String renderStageXml(final Long number, final Urn stage,
        final String place) throws Exception {
        String xml = null;
        if (this.identity.name().equals(stage)) {
            final Bout bout = this.identity.bout(number);
            final List<Message> inbox = bout.messages(
                String.format("(ns '%s')", Slip.NAMESPACE)
            );
            final Stage data = new Stage();
            final Map<String, SharedDoc> docs =
                new HashMap<String, SharedDoc>();
            final Set<String> stops = new HashSet<String>();
            for (Message msg : inbox) {
                final Slip slip = Jaxb.parse(msg.text(), Slip.class);
                if (!slip.isAllow()) {
                    stops.add(slip.getName());
                    continue;
                }
                if (stops.contains(slip.getName())) {
                    continue;
                }
                if (docs.containsKey(slip.getName())) {
                    continue;
                }
                docs.put(
                    slip.getName(),
                    new SharedDoc(slip.getName(), slip.getType())
                );
            }
            data.add(docs.values());
            xml = Jaxb.format(data);
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
            xsl = IOUtils.toString(
                this.getClass().getResourceAsStream("stage.xsl"),
                CharEncoding.UTF_8
            );
        }
        return xsl;
    }

}
