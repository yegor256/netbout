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
package com.netbout.rest.auth;

import com.jcabi.log.Logger;
import com.netbout.hub.Hub;
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Profile;
import com.netbout.spi.Urn;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.lang.LocaleUtils;

/**
 * Remote identity, returned by {@link AuthMediator#authenticate(Urn,String)}.
 *
 * <p>The class has to be public because it's instantiated by JAXB.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@XmlType(name = "identity")
@SuppressWarnings("PMD.TooManyMethods")
public final class RemoteIdentity implements Identity {

    /**
     * Authority.
     */
    private transient URL iauthority;

    /**
     * Identity name.
     */
    private transient Urn iname;

    /**
     * Profile of identity.
     */
    private final transient RemoteProfile iprofile = new RemoteProfile();

    /**
     * Problems occured during unmarshalling.
     */
    private final transient List<String> problems = new LinkedList<String>();

    /**
     * Find it in hub and return.
     * @param hub The hub to find in
     * @return The identity found
     * @throws com.netbout.spi.UnreachableUrnException If can't find it
     * @checkstyle RedundantThrows (4 lines)
     */
    public Identity findIn(final Hub hub)
        throws com.netbout.spi.UnreachableUrnException {
        final Identity identity = hub.identity(this.iname);
        for (String alias : this.profile().aliases()) {
            identity.profile().alias(alias);
        }
        identity.profile().setPhoto(this.profile().photo());
        return identity;
    }

    /**
     * Set authority, method for JAXB unmarshaller.
     * @param url The name of it
     */
    @XmlElement
    public void setAuthority(final String url) {
        try {
            this.iauthority = new URL(url);
        } catch (java.net.MalformedURLException ex) {
            this.problems.add(
                String.format(
                    "Invalid /identity/authority format: %s",
                    ex.getMessage()
                )
            );
        }
    }

    /**
     * Set identity name, method for JAXB unmarshaller.
     * @param name The name of it
     */
    @XmlElement
    public void setName(final String name) {
        try {
            this.iname = new Urn(name);
        } catch (java.net.URISyntaxException ex) {
            this.problems.add(
                String.format(
                    "Invalid /identity/name format at '%s': %s",
                    name,
                    ex.getMessage()
                )
            );
        }
    }

    /**
     * Set photo, method for JAXB unmarshaller.
     * @param url The URL
     */
    @XmlElement
    public void setPhoto(final String url) {
        try {
            this.iprofile.setPhoto(new URL(url));
        } catch (java.net.MalformedURLException ex) {
            this.problems.add(
                String.format(
                    "Invalid /identity/photo format: %s",
                    ex.getMessage()
                )
            );
        }
    }

    /**
     * Set locale, method for JAXB unmarshaller.
     * @param locale The locale to set
     */
    @XmlElement
    public void setLocale(final String locale) {
        this.iprofile.setLocale(LocaleUtils.toLocale(locale));
    }

    /**
     * Set aliases, method for JAXB unmarshaller.
     * @param names List of them
     */
    @XmlElement(name = "alias")
    @XmlElementWrapper(name = "aliases")
    public void setAliases(final Set<String> names) {
        for (String name : names) {
            this.iprofile.alias(name);
        }
    }

    /**
     * Get aliases, method for JAXB unmarshaller (it returns an empty set
     * of aliases, which will be extended by unmarshaller and pushed back
     * to {@link #setAliases()} - this is how JAXB works).
     * @return List of aliases
     */
    public Set<String> getAliases() {
        return new HashSet<String>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL authority() {
        this.validate();
        return this.iauthority;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Urn name() {
        this.validate();
        return this.iname;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Profile profile() {
        this.validate();
        return this.iprofile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Identity identity) {
        throw new UnsupportedOperationException("#compareTo()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long eta() {
        return 0L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout start() {
        throw new UnsupportedOperationException("#start()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout bout(final Long number) {
        throw new UnsupportedOperationException("#bout()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Bout> inbox(final String query) {
        throw new UnsupportedOperationException("#inbox()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity friend(final Urn name) {
        throw new UnsupportedOperationException("#friend()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Identity> friends(final String keyword) {
        throw new UnsupportedOperationException("#friends()");
    }

    /**
     * Validate and throw exception if there are some problems.
     */
    private void validate() {
        if (this.iname == null) {
            this.problems.add("/identity/name is absent");
        }
        if (this.iauthority == null) {
            this.problems.add("/identity/authority is absent");
        }
        if (!this.problems.isEmpty()) {
            throw new IllegalStateException(
                Logger.format(
                    "/page/identity is not valid: %[list]s",
                    this.problems
                )
            );
        }
    }

}
