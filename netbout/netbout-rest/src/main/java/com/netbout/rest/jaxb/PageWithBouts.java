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
 * incident to the author by email: privacy@netbout.com.
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

// bout manipulation engine from com.netbout:netbout-engine
import com.netbout.engine.Bout;
import com.netbout.engine.BoutFactory;

// JDK
import java.util.ArrayList;
import java.util.List;

// JAXB
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Collection of Bouts.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@XmlRootElement(name = "page")
@XmlAccessorType(XmlAccessType.NONE)
public final class PageWithBouts {

    /**
     * Bout manipulation factory.
     */
    private final BoutFactory factory;

    /**
     * Query to retrieve list of bouts.
     */
    private final String query;

    /**
     * Public default ctor, required for JAXB.
     */
    public PageWithBouts() {
        this.factory = null;
        this.query = null;
    }

    /**
     * Public ctor.
     * @param fct The factory
     * @param qry The query
     */
    public PageWithBouts(final BoutFactory fct, final String qry) {
        this.factory = fct;
        this.query = qry;
    }

    /**
     * Collection of bouts.
     * @return The collection of bouts, to be converted into XML
     */
    @XmlElement(name = "bout")
    @XmlElementWrapper(name = "bouts")
    public List<ShortBout> getList() {
        final List<ShortBout> list = new ArrayList<ShortBout>();
        for (Bout bout : this.factory.list(this.query)) {
            list.add(new ShortBout(bout));
        }
        return list;
    }

}
