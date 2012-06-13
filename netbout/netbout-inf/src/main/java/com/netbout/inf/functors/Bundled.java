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
import com.netbout.inf.notices.MessagePostedNotice;
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
@NamedAs("bundled")
final class Bundled implements Functor {

    /**
     * The attribute to use (also used by {@link Unbundled}).
     */
    @SuppressWarnings("PMD.DefaultPackage")
    static final String ATTR = "bundled-marker";

    /**
     * {@inheritDoc}
     */
    @Override
    public Term build(final Ray ray, final List<Atom> atoms) {
        // @checkstyle AnonInnerLength (50 lines)
        return new Term() {
            private final transient ConcurrentMap<String, Term> terms =
                new ConcurrentSkipListMap<String, Term>();
            @Override
            public Cursor shift(final Cursor cursor) {
                final Cursor shifted = cursor.shift(
                    ray.builder().and(this.terms.values())
                );
                if (!shifted.end()) {
                    final String marker = shifted.msg().first(Bundled.ATTR);
                    this.terms.put(
                        marker,
                        ray.builder().not(
                            ray.builder().matcher(Bundled.ATTR, marker)
                        )
                    );
                }
                return shifted;
            }
            @Override
            public String toString() {
                return "(BUNDLED)";
            }
            @Override
            public Lattice lattice() {
                return Lattice.and(this.terms.values());
            }
            private Cursor next(final Cursor cursor) {
                Cursor next;
                if (cursor.end()) {
                    next = cursor;
                } else {
                    next = cursor.shift(ray.builder().always());
                }
                return next;
            }
        };
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
            Bundled.ATTR,
            Bundled.marker(notice.message())
        );
    }

    /**
     * Create marker from a message.
     * @param message The message
     * @return Marker
     */
    private static String marker(final Message message) {
        final Set<Urn> names = new TreeSet<Urn>();
        for (Participant dude : message.bout().participants()) {
            names.add(dude.identity().name());
        }
        return Logger.format("%[list]s", names);
    }

}
