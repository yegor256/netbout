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

import com.netbout.hub.Hub;
import com.netbout.rest.MetaText;
import com.netbout.rest.period.PeriodsBuilder;
import com.netbout.spi.Bout;
import com.netbout.spi.Message;
import com.netbout.spi.xml.DomParser;
import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * Message convertable to XML through JAXB.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@XmlRootElement(name = "message")
@XmlSeeAlso(RawXml.class)
@XmlAccessorType(XmlAccessType.NONE)
public final class LongMessage {

    /**
     * The bus.
     */
    private final transient Hub hub;

    /**
     * The bout.
     */
    private final transient Bout bout;

    /**
     * The message.
     */
    private transient Message message;

    /**
     * Public ctor for JAXB.
     */
    public LongMessage() {
        throw new IllegalStateException("This ctor should never be called");
    }

    /**
     * Private ctor.
     * @param ihub The hub
     * @param ibout The bout
     * @param msg The message
     */
    public LongMessage(final Hub ihub, final Bout ibout, final Message msg) {
        this.hub = ihub;
        this.bout = ibout;
        this.message = msg;
    }

    /**
     * Get its number.
     * @return The number
     */
    @XmlElement
    public Long getNumber() {
        return this.message.number();
    }

    /**
     * Get author.
     * @return The author
     */
    @XmlElement
    public String getAuthor() {
        return this.message.author().name().toString();
    }

    /**
     * Get its text.
     * @return The text
     */
    @XmlElement
    public String getText() {
        return this.message.text();
    }

    /**
     * Get its text for rendering.
     * @return The text
     */
    @XmlElement
    public Object getRender() {
        final String txt = this.getText();
        final String render = this.hub.make("pre-render-message")
            .synchronously()
            .inBout(this.bout)
            .arg(this.bout.number())
            .arg(this.message.number())
            .arg(txt)
            .asDefault(txt)
            .exec();
        Object output = null;
        if (render.equals(txt)) {
            final DomParser dom = new DomParser(txt);
            if (dom.isXml()) {
                output = new RawXml(dom);
            }
        }
        if (output == null) {
            output = new MetaText(StringEscapeUtils.escapeXml(render)).html();
        }
        return output;
    }

    /**
     * Get its date.
     * @return The date
     */
    @XmlElement
    public Date getDate() {
        return this.message.date();
    }

    /**
     * Get text explanation when this message was posted.
     * @return The explanation
     */
    @XmlElement
    public String getWhen() {
        return PeriodsBuilder.when(this.message.date());
    }

    /**
     * Was it seen by the author already?
     * @return The status
     */
    @XmlAttribute
    public Boolean getSeen() {
        return this.message.seen();
    }

}
