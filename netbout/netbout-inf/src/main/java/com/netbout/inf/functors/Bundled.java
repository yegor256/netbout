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
package com.netbout.inf.functors;

import com.jcabi.log.Logger;
import com.netbout.inf.Atom;
import com.netbout.inf.Cursor;
import com.netbout.inf.Functor;
import com.netbout.inf.Lattice;
import com.netbout.inf.Ray;
import com.netbout.inf.Term;
import com.netbout.inf.atoms.VariableAtom;
import com.netbout.inf.lattice.LatticeBuilder;
import com.netbout.inf.notices.JoinNotice;
import com.netbout.inf.notices.KickOffNotice;
import com.netbout.inf.notices.MessagePostedNotice;
import com.netbout.spi.Bout;
import com.netbout.spi.Message;
import com.netbout.spi.Participant;
import com.netbout.spi.Urn;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Allows only bundled messages.
 *
 * <p>This class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Functor.NamedAs("bundled")
@Functor.DependsOn(Equal.class)
final class Bundled implements Functor {

    /**
     * {@inheritDoc}
     */
    @Override
    public Term build(final Ray ray, final List<Atom<?>> atoms) {
        return new Bundled.BundledTerm(ray);
    }

    /**
     * Notice when new message is posted.
     * @param ray The ray
     * @param notice The notice
     */
    @Noticable
    public void see(final Ray ray, final MessagePostedNotice notice) {
        ray.cursor().replace(
            ray.builder().picker(notice.message().number()),
            BundledAttribute.VALUE,
            Bundled.marker(notice.message())
        );
    }

    /**
     * Notice when participant removed.
     * @param ray The ray
     * @param notice The notice
     */
    @Noticable
    public void see(final Ray ray, final KickOffNotice notice) {
        ray.cursor().replace(
            ray.builder().matcher(
                VariableAtom.BOUT_NUMBER.attribute(),
                notice.bout().number().toString()
            ),
            BundledAttribute.VALUE,
            Bundled.marker(notice.bout())
        );
    }

    /**
     * Notice when new participant joined.
     * @param ray The ray
     * @param notice The notice
     */
    @Noticable
    public void see(final Ray ray, final JoinNotice notice) {
        ray.cursor().replace(
            ray.builder().matcher(
                VariableAtom.BOUT_NUMBER.attribute(),
                notice.bout().number().toString()
            ),
            BundledAttribute.VALUE,
            Bundled.marker(notice.bout())
        );
    }

    /**
     * The term to instantiate here.
     */
    private static final class BundledTerm implements Term {
        /**
         * Ray to use.
         */
        private final transient Ray ray;
        /**
         * Terms to ignore (passed already).
         */
        private final transient ConcurrentMap<String, Term> terms =
            new ConcurrentSkipListMap<String, Term>();
        /**
         * Public ctor.
         * @param iray The ray to work with
         */
        public BundledTerm(final Ray iray) {
            this.ray = iray;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Term copy() {
            return new Bundled.BundledTerm(this.ray);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Cursor shift(final Cursor cursor) {
            Term term;
            if (this.terms.isEmpty()) {
                term = this.ray.builder().always();
            } else if (this.terms.size() == 1) {
                term = this.terms.values().iterator().next();
            } else {
                term = this.ray.builder().and(this.terms.values());
            }
            final Cursor shifted = cursor.shift(term);
            if (!shifted.end()) {
                final String marker = shifted.msg().attr(
                    BundledAttribute.VALUE
                );
                if (this.terms.containsKey(marker)) {
                    throw new IllegalStateException(
                        String.format(
                            // @checkstyle LineLength (1 line)
                            "marker '%s' at %s has already been seen in %s among %d others, shifted from %s by %s",
                            marker,
                            shifted,
                            this.terms.get(marker),
                            this.terms.size(),
                            cursor,
                            term
                        )
                    );
                }
                this.terms.put(
                    marker,
                    this.ray.builder().not(
                        this.ray.builder().matcher(
                            BundledAttribute.VALUE,
                            marker
                        )
                    )
                );
            }
            return shifted;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "(BUNDLED)";
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Lattice lattice() {
            return new LatticeBuilder()
                .always()
                .and(this.terms.values())
                .build();
        }
    }

    /**
     * Create marker from bout.
     * @param bout The bout
     * @return Marker
     */
    private static String marker(final Bout bout) {
        final Set<Urn> names = new TreeSet<Urn>();
        for (Participant dude : bout.participants()) {
            names.add(dude.identity().name());
        }
        return Logger.format("%[list]s", names);
    }

    /**
     * Get marker from message.
     * @param message The message
     * @return The marker
     */
    private static String marker(final Message message) {
        return Bundled.marker(message.bout());
    }

}
