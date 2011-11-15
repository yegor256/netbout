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
package com.netbout.hub;

import com.netbout.hub.data.MessageData;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Message in a hub.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@XmlRootElement(name = "message")
@XmlType(name = "message")
@XmlAccessorType(XmlAccessType.NONE)
final class HubMessage implements Message {

    /**
     * The viewer.
     */
    private Identity viewer;

    /**
     * The data.
     */
    private MessageData data;

    /**
     * Public ctor for JAXB.
     */
    public HubMessage() {
        throw new IllegalStateException("This ctor should never be called");
    }

    /**
     * Public ctor.
     * @param vwr The author
     * @param dat The data
     * @checkstyle ParameterNumber (3 lines)
     */
    public HubMessage(final Identity vwr, final MessageData dat) {
        this.viewer = vwr;
        this.data = dat;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity author() {
        try {
            return HubIdentity.friend(this.data.getAuthor());
        } catch (com.netbout.spi.UnknownIdentityException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * JAXB related method.
     * @return The author
     */
    @XmlElement
    public HubIdentity getAuthor() {
        return (HubIdentity) this.author();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String text() {
        return this.data.getText();
    }

    /**
     * JAXB related method.
     * @return The text
     */
    @XmlElement
    public String getText() {
        return this.text();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date date() {
        return this.data.getDate();
    }

    /**
     * JAXB related method.
     * @return The date
     */
    @XmlElement
    public Date getDate() {
        return this.date();
    }

}
