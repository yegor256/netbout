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

import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Participant;
import com.ymock.util.Logger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Identity.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@XmlRootElement(name = "participant")
@XmlType(name = "participant")
@XmlAccessorType(XmlAccessType.NONE)
public final class HubParticipant implements Participant {

    /**
     * Is it confirmed?
     */
    private boolean confirmed;

    /**
     * The identity.
     */
    private Identity identity;

    /**
     * Public ctor for JAXB.
     */
    public HubParticipant() {
        throw new IllegalStateException("This ctor should never be called");
    }

    /**
     * Public ctor.
     * @param idnt Identity
     * @param aye Is it confirmed
     */
    public HubParticipant(final Identity idnt, final boolean aye) {
        this.identity = idnt;
        this.confirmed = aye;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity identity() {
        return this.identity;
    }

    /**
     * JAXB related method.
     * @return The participant
     */
    @XmlElement
    public HubIdentity getIdentity() {
        return (HubIdentity) this.identity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean confirmed() {
        return this.confirmed;
    }

    /**
     * JAXB related method.
     * @return Is it confirmed?
     */
    @XmlAttribute
    public boolean getConfirmed() {
        return this.confirmed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void confirm(final boolean aye) {
        this.confirmed = aye;
        Logger.info(
            this,
            "#confirm(%b): done",
            aye
        );
    }

}
