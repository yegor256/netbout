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

import java.util.Collection;
import java.util.LinkedList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

/**
 * Stage.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@XmlType(name = "data")
@XmlAccessorType(XmlAccessType.NONE)
public final class Stage {

    /**
     * List of documents in it.
     */
    private final transient Collection<SharedDoc> docs =
        new LinkedList<SharedDoc>();

    /**
     * The place.
     */
    private final transient String place;

    /**
     * Public ctor, for JAXB.
     */
    public Stage() {
        throw new IllegalStateException("illegal call");
    }

    /**
     * Public ctor.
     * @param txt The place
     */
    public Stage(final String txt) {
        this.place = txt;
    }

    /**
     * Get place.
     * @return The place
     */
    @XmlElement(name = "place")
    public String getPlace() {
        return this.place;
    }

    /**
     * Get list of docs (for JAXB).
     * @return The list of them
     */
    @XmlElement(name = "doc")
    @XmlElementWrapper(name = "docs")
    public Collection<SharedDoc> getDocs() {
        return this.docs;
    }

    /**
     * Add new documents.
     * @param documents The docs to add
     */
    public void add(final Collection<SharedDoc> documents) {
        this.docs.addAll(documents);
    }

}
