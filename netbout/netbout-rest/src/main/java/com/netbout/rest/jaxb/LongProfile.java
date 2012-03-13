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

import com.netbout.rest.page.JaxbBundle;
import com.netbout.spi.Identity;
import com.ymock.util.Logger;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Set;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang.LocaleUtils;

/**
 * Short version of a bout.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@XmlRootElement(name = "profile")
@XmlAccessorType(XmlAccessType.NONE)
public final class LongProfile {

    /**
     * URI builder.
     */
    private final transient UriBuilder builder;

    /**
     * The viewer of it.
     */
    private final transient Identity viewer;

    /**
     * Public ctor for JAXB.
     */
    public LongProfile() {
        throw new IllegalStateException("This ctor should never be called");
    }

    /**
     * Private ctor.
     * @param bldr URI builder
     * @param vwr The viewer
     */
    public LongProfile(final UriBuilder bldr, final Identity vwr) {
        this.builder = bldr;
        this.viewer = vwr;
    }

    /**
     * List of languages.
     * @return The collection of links to them
     */
    @XmlElement(name = "link")
    @XmlElementWrapper(name = "locales")
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Collection<Link> getLocales() {
        final Locale current = this.viewer.profile().locale();
        final Collection<Link> links = new LinkedList<Link>();
        for (Locale locale : this.available()) {
            final Link link = new Link(
                "locale",
                this.builder.clone()
                    .path("/toggle")
                    .queryParam("l", "{locale}")
                    .build(locale)
            );
            link.add(new JaxbBundle("code", locale).element());
            link.add(
                new JaxbBundle(
                    "name",
                    locale.getDisplayName(current)
                ).element()
            );
            link.add(
                new JaxbBundle(
                    "langauge",
                    locale.getDisplayLanguage(current)
                ).element()
            );
            link.add(
                new JaxbBundle(
                    "country",
                    locale.getDisplayCountry(current)
                ).element()
            );
            links.add(link);
        }
        return links;
    }

    /**
     * Set locale.
     * @param text The locale as a text.
     */
    public void setLocale(final String text) {
        this.viewer.profile().setLocale(this.toLocale(text));
    }

    /**
     * Convert text to Locale.
     * @param text The locale as a text.
     * @return Ready to use locale
     */
    public static Locale toLocale(final String text) {
        Locale locale = LocaleUtils.toLocale(text);
        if (!LongProfile.available().contains(locale)) {
            Logger.error(
                LongProfile.class,
                "Unsupported Locale '%s', reverting to '%s'",
                locale,
                Locale.ENGLISH
            );
            locale = Locale.ENGLISH;
        }
        return locale;
    }

    /**
     * Get a list of available locales.
     * @return The list of them
     */
    private static Set<Locale> available() {
        final Set<Locale> available = new HashSet<Locale>();
        available.add(Locale.ENGLISH);
        // available.add(new Locale("es"));
        available.add(Locale.CHINESE);
        available.add(new Locale("ru"));
        return available;
    }

}
