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
package com.netbout.inf.ray;

import com.netbout.inf.Term;
import java.util.Set;

/**
 * If a term is cacheable.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
interface CacheableTerm extends Term {

    /**
     * Dependency.
     */
    class Dependency {
        /**
         * Attribute name.
         */
        private final transient String attrib;
        /**
         * The value.
         */
        private final transient String value;
        /**
         * Public ctor.
         * @param attr The attribute
         */
        public Dependency(final String attr) {
            this(attr, "");
        }
        /**
         * Public ctor.
         * @param attr The attribute
         * @param val The value
         */
        public Dependency(final String attr, final String val) {
            this.attrib = attr;
            this.value = val;
        }
        /**
         * Does it match the provided dep?
         * @param dep The dependency to match against
         * @return Yes or no
         */
        public boolean matches(final CacheableTerm.Dependency dep) {
            return dep.attrib.equals(this.attrib)
                && (dep.value.equals(this.value) || dep.value.isEmpty()
                || this.value.isEmpty());
        }
    }

    /**
     * Set of attribute/value pairs.
     * @return Set of them
     */
    Set<CacheableTerm.Dependency> dependencies();

}
