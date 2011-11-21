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
import com.netbout.hub.queue.HelpQueue;
import com.netbout.spi.Bout;
import com.netbout.spi.BoutNotFoundException;
import com.netbout.spi.Helper;
import com.netbout.spi.HelperException;
import com.netbout.spi.Identity;
import com.netbout.spi.User;
import com.ymock.util.Logger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * Identity.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @todo #123 This class needs refactoring. We should get rid of NULL in iuser
 *  and should break it into smaller classes - it's too big now.
 */
@XmlRootElement(name = "identity")
@XmlType(name = "identity")
@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso(HubBout.class)
@SuppressWarnings("PMD.TooManyMethods")
public final class HubIdentity implements Identity {

    /**
     * The name.
     */
    private final transient String iname;

    /**
     * Name of the user.
     */
    private transient User iuser;

    /**
     * The photo.
     */
    private transient URL iphoto;

    /**
     * List of bouts where I'm a participant.
     */
    private transient Set<Long> ibouts;

    /**
     * List of aliases.
     */
    private transient Set<String> ialiases;

    /**
     * Public ctor for JAXB.
     */
    public HubIdentity() {
        throw new IllegalStateException("This ctor should never be called");
    }

    /**
     * Public ctor.
     * @param name The identity's name
     * @param user The user
     * @see Identities#make(String,User)
     */
    public HubIdentity(final String name, final User user) {
        this.iname = name;
        this.iuser = user;
    }

    /**
     * Public ctor, when user is not known.
     * @param name The identity's name
     * @see Identities#make(String)
     */
    @SuppressWarnings("PMD.NullAssignment")
    public HubIdentity(final String name) {
        this.iname = name;
        this.iuser = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User user() {
        if (!this.isAssigned()) {
            throw new IllegalStateException(
                String.format(
                    "User is unknown for identity '%s'",
                    this.iname
                )
            );
        }
        return this.iuser;
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
        } catch (com.netbout.hub.data.BoutMissedException ex) {
            throw new IllegalStateException(ex);
        }
        final ParticipantData dude = ParticipantData.build(num, this.name());
        data.addParticipant(dude);
        dude.setConfirmed(true);
        Logger.debug(
            this,
            "#start(): bout started"
        );
        this.myBouts().add(num);
        return new HubBout(this, data);
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public Bout bout(final Long number) throws BoutNotFoundException {
        final HubBout bout;
        try {
            bout = new HubBout(this, Storage.INSTANCE.find(number));
        } catch (com.netbout.hub.data.BoutMissedException ex) {
            throw new BoutNotFoundException(ex);
        }
        if (!bout.isParticipant(this)) {
            throw new BoutNotFoundException(
                "'%s' is not a participant in bout #%d",
                this.name(),
                bout.number()
            );
        }
        return bout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Bout> inbox(final String query) {
        final List<Bout> list = new ArrayList<Bout>();
        for (Long num : this.myBouts()) {
            try {
                list.add(this.bout(num));
            } catch (com.netbout.spi.BoutNotFoundException ex) {
                throw new IllegalStateException(ex);
            }
        }
        Logger.debug(
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
        return this.iname;
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
        if (this.iphoto == null) {
            try {
                this.iphoto = new URL(
                    HelpQueue.make("get-identity-photo")
                        .priority(HelpQueue.Priority.SYNCHRONOUSLY)
                        .arg(this.iname)
                        .asDefault("http://img.netbout.com/unknown.png")
                        .exec(String.class)
                );
            } catch (java.net.MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return this.iphoto;
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
        synchronized (this) {
            this.iphoto = pic;
        }
        HelpQueue.make("changed-identity-photo")
            .priority(HelpQueue.Priority.SYNCHRONOUSLY)
            .arg(this.iname)
            .arg(this.iphoto.toString())
            .exec();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> aliases() {
        final Set<String> list = new HashSet<String>(this.myAliases());
        Logger.info(
            this,
            "#aliases(): %d returned",
            list.size()
        );
        return list;
    }

    /**
     * JAXB related method, to return aliases of identity.
     * @return The list of aliases
     */
    @XmlElement(name = "alias")
    @XmlElementWrapper(name = "aliases")
    public Set<String> getAliases() {
        return this.aliases();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void alias(final String alias) {
        if (this.myAliases().contains(alias)) {
            Logger.info(
                this,
                "#alias('%s'): it's already set for '%s'",
                alias,
                this.iname
            );
        } else {
            HelpQueue.make("added-identity-alias")
                .priority(HelpQueue.Priority.ASAP)
                .arg(this.iname)
                .arg(alias)
                .exec();
            Logger.info(
                this,
                "#alias('%s'): added for '%s'",
                alias,
                this.iname
            );
            this.myAliases().add(alias);
        }
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (3 lines)
     */
    @Override
    public void promote(final Helper helper) throws HelperException {
        helper.init(new HubEntry());
        HelpQueue.register(helper);
        Logger.info(
            this,
            "#promote(%s): '%s' promoted",
            helper.getClass().getName(),
            this.name()
        );
    }

    /**
     * Notification that I've been invited to the bout.
     * @param bout The bout
     */
    protected void invited(final Bout bout) {
        this.myBouts().add(bout.number());
    }

    /**
     * Does this identity matches a keyword?
     * @param keyword The keyword
     * @return Yes or no?
     */
    protected boolean matchesKeyword(final String keyword) {
        boolean matches = this.iname.contains(keyword);
        for (String alias : this.myAliases()) {
            matches |= alias.contains(keyword);
        }
        return matches;
    }

    /**
     * Does this identity belongs to the specified user?
     * @param user The user
     * @return Yes or no?
     */
    protected boolean belongsTo(final User user) {
        if (!this.isAssigned()) {
            throw new IllegalStateException(
                String.format(
                    "Identity '%s' is not assigned to any user yet",
                    this.iname
                )
            );
        }
        return this.iuser.equals(user);
    }

    /**
     * Does this identity belongs to some user already?
     * @return Yes or no?
     */
    protected boolean isAssigned() {
        return this.iuser != null;
    }

    /**
     * Assign the identity to the given user.
     * @param user The user
     */
    protected void assignTo(final User user) {
        if (this.isAssigned()) {
            throw new IllegalStateException(
                String.format(
                    "Identity '%s' already belongs to '%s'",
                    this.iname,
                    this.iuser.name()
                )
            );
        }
        this.iuser = user;
    }

    /**
     * Return a link to my list of bouts.
     * @return The list of them
     */
    private Set<Long> myBouts() {
        synchronized (this) {
            if (this.ibouts == null) {
                this.ibouts = new CopyOnWriteArraySet<Long>(
                    Arrays.asList(
                        HelpQueue.make("get-bouts-of-identity")
                            .priority(HelpQueue.Priority.SYNCHRONOUSLY)
                            .arg(this.iname)
                            .asDefault(new Long[]{})
                            .exec(Long[].class)
                    )
                );
            }
        }
        return this.ibouts;
    }

    /**
     * Returns a link to the list of aliases.
     * @return The link to the list of them
     */
    private Set<String> myAliases() {
        synchronized (this) {
            if (this.ialiases == null) {
                this.ialiases = new CopyOnWriteArraySet<String>(
                    Arrays.asList(
                        HelpQueue.make("get-aliases-of-identity")
                            .priority(HelpQueue.Priority.SYNCHRONOUSLY)
                            .arg(this.iname)
                            .asDefault(new String[]{})
                            .exec(String[].class)
                    )
                );
            }
        }
        return this.ialiases;
    }

}
