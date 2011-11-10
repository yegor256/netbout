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

import com.netbout.hub.data.BoutData;
import com.netbout.hub.data.ParticipantData;
import com.netbout.hub.data.Storage;
import com.netbout.spi.Bout;
import com.netbout.spi.BoutNotFoundException;
import com.netbout.spi.DuplicateIdentityException;
import com.netbout.spi.Helper;
import com.netbout.spi.Identity;
import com.netbout.spi.UnknownIdentityException;
import com.netbout.spi.User;
import com.ymock.util.Logger;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * Identity.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@XmlRootElement(name = "identity")
@XmlType(name = "identity")
@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso(HubBout.class)
public final class HubIdentity implements Identity {

    /**
     * All identities known for us at the moment, and their users.
     */
    private static final Map<String, HubIdentity> ALL =
        new HashMap<String, HubIdentity>();

    /**
     * The name.
     */
    private final String name;

    /**
     * Name of the user.
     */
    private final String user;

    /**
     * The photo.
     */
    private URL photo;

    /**
     * List of bouts where I'm a participant.
     */
    private final Set<Long> bouts = new HashSet<Long>();

    /**
     * Public ctor for JAXB.
     */
    public HubIdentity() {
        throw new IllegalStateException("This ctor should never be called");
    }

    /**
     * Public ctor.
     * @param nam The identity's name
     * @param usr Name of the user
     * @see HubUser#identity(String)
     */
    public HubIdentity(final String nam, final String usr) {
        this.name = nam;
        this.user = usr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout start() {
        final Long num = Storage.INSTANCE.create();
        BoutData data;
        try {
            data = Storage.INSTANCE.find(num);
        } catch (BoutNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
        data.addParticipant(new ParticipantData(this, true));
        Logger.info(
            this,
            "#start(): bout started"
        );
        this.bouts.add(num);
        return new HubBout(this, data);
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public Bout bout(final Long number) throws BoutNotFoundException {
        return new HubBout(this, Storage.INSTANCE.find(number));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Bout> inbox(final String query) {
        final List<Bout> list = new ArrayList<Bout>();
        final List<Long> broken = new ArrayList<Long>();
        for (Long num : this.bouts) {
            try {
                list.add(this.bout(num));
            } catch (com.netbout.spi.BoutNotFoundException ex) {
                broken.add(num);
            }
        }
        for (Long num : broken) {
            this.bouts.remove(num);
        }
        Logger.info(
            this,
            "#inbox('%s'): %d bouts found",
            query,
            list.size()
        );
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return this.name;
    }

    /**
     * JAXB related method, to return the name of identity.
     * @return The name
     */
    @XmlElement
    public String getName() {
        return this.name();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL photo() {
        return this.photo;
    }

    /**
     * JAXB related method, to return photo of identity.
     * @return The photo
     */
    @XmlElement(required = true)
    public URL getPhoto() {
        return this.photo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPhoto(final URL pic) {
        this.photo = pic;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void promote(final Helper hlp) {
        Logger.info(
            this,
            "#promote(%s): '%s' promoted",
            hlp.getClass().getName(),
            this.name()
        );
    }

    /**
     * Make new identity or find existing one.
     * @param label The name of identity
     * @param usr Name of the user
     * @return Identity found
     * @throws DuplicateIdentityException If this identity is taken
     */
    protected static Identity make(final String label, final String usr)
        throws DuplicateIdentityException {
        HubIdentity identity;
        if (HubIdentity.ALL.containsKey(label)) {
            identity = HubIdentity.ALL.get(label);
            if (!identity.user.equals(usr)) {
                throw new DuplicateIdentityException("Identity '%s' is taken");
            }
        } else {
            identity = new HubIdentity(label, usr);
            HubIdentity.ALL.put(label, identity);
        }
        return identity;
    }

    /**
     * Find identity by name.
     * @param label The name of identity
     * @return Identity found
     */
    protected static Identity friend(final String label)
        throws UnknownIdentityException {
        if (HubIdentity.ALL.containsKey(label)) {
            return HubIdentity.ALL.get(label);
        }
        throw new UnknownIdentityException("Identity '%s' not found", label);
    }

}
