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
package com.netbout.rest.jaxb;

import com.netbout.queue.HelpQueue;
import com.netbout.queue.ProgressReport;
import com.netbout.queue.TextProgressReport;
import com.netbout.rest.StageCoordinates;
import java.io.StringReader;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Long version of a stage.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@XmlRootElement(name = "stage")
@XmlAccessorType(XmlAccessType.NONE)
public final class LongStage {

    /**
     * The number of the bout.
     */
    private transient Long bout;

    /**
     * Coordinates of the stage.
     */
    private transient StageCoordinates coords;

    /**
     * Public ctor for JAXB.
     */
    public LongStage() {
        throw new IllegalStateException("This ctor should never be called");
    }

    /**
     * Private ctor.
     * @param num Bout number
     * @param crds The coordinates
     */
    private LongStage(final Long num, final StageCoordinates crds) {
        this.bout = num;
        this.coords = crds;
    }

    /**
     * Builder.
     * @param num Bout number
     * @param crds The coordinates
     * @return The instance just created
     */
    public static LongStage build(final Long num, final StageCoordinates crds) {
        return new LongStage(num, crds);
    }

    /**
     * Name of identity.
     * @return The number
     */
    @XmlAttribute
    public String getName() {
        return this.coords.getStage();
    }

    /**
     * The place in it.
     * @return The number
     */
    @XmlAttribute
    public String getPlace() {
        return this.coords.getPlace();
    }

    /**
     * Get XML of one stage.
     * @return The XML
     */
    @XmlAnyElement(lax = true)
    @XmlMixed
    public Element getContent() throws Exception {
        return DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(new InputSource(new StringReader(this.xml())))
            .getDocumentElement();
    }

    /**
     * Get XML of one stage.
     * @return The XML
     */
    private String xml() {
        final ProgressReport report = new TextProgressReport();
        String xml = HelpQueue
            .make("render-stage-xml")
            .priority(HelpQueue.Priority.NORMAL)
            .arg(this.bout)
            .arg(this.coords.getStage())
            .arg(this.coords.getPlace())
            .scope(this.bout)
            .progressReport(report)
            .exec(String.class);
        if (xml == null) {
            xml = report.toString();
        }
        return xml;
    }

}
