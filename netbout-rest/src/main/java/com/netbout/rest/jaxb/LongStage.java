/**
 * Copyright (c) 2009-2012, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code accidentally and without intent to use it, please report this
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

import com.netbout.hub.Hub;
import com.netbout.rest.StageCoordinates;
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.xml.DomParser;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import org.w3c.dom.Element;

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
     * The hub to work with.
     */
    private transient Hub hub;

    /**
     * The bout.
     */
    private transient Bout bout;

    /**
     * Coordinates of the stage.
     */
    private transient StageCoordinates coords;

    /**
     * Who is viewing.
     */
    private transient Identity viewer;

    /**
     * Public ctor for JAXB.
     */
    public LongStage() {
        throw new IllegalStateException("This ctor should never be called");
    }

    /**
     * Private ctor.
     * @param ihub The hub
     * @param bot Bout to work with
     * @param crds The coordinates
     * @param vwr The viewer
     * @checkstyle ParameterNumber (3 lines)
     */
    public LongStage(final Hub ihub, final Bout bot,
        final StageCoordinates crds, final Identity vwr) {
        this.hub = ihub;
        this.bout = bot;
        this.coords = crds;
        this.viewer = vwr;
    }

    /**
     * Name of identity.
     * @return The number
     */
    @XmlAttribute
    public String getName() {
        return this.coords.stage().toString();
    }

    /**
     * The place in it.
     * @return The number
     */
    @XmlAttribute
    public String getPlace() {
        return this.coords.place();
    }

    /**
     * Get XML of one stage.
     * @return The XML
     * @throws Exception If some problem with DOM operations
     */
    @XmlAnyElement(lax = true)
    @XmlMixed
    public Element getContent() throws Exception {
        return new DomParser(this.xml()).parse().getDocumentElement();
    }

    /**
     * Get XML of one stage.
     * @return The XML
     */
    private String xml() {
        return this.hub.make("render-stage-xml")
            .arg(this.bout.number())
            .arg(this.viewer.name())
            .arg(this.coords.stage())
            .arg(this.coords.place())
            .noCache()
            .inBout(this.bout)
            .asDefault("<no-data/>")
            .exec();
    }

}
