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
package com.netbout.inf.motors.bundles;

import com.netbout.inf.Functor;
import com.netbout.inf.PredicateException;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Allows only unique values of the provided variable.
 *
 * <p>This class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@NamedAs("unique")
final class Unique implements Functor {

    /**
     * {@inheritDoc}
     */
    @Override
    final Term build(final Ray ray, final List<Atom> atoms) {
        final String attr = VariableAtom.class.cast(atoms.get(0)).attribute();
        return new Term() {
            private final transient ConcurrentMap<String, Term> terms =
                new ConcurrentHashMap<String, Term>();
            @Override
            public Cursor shift(final Cursor cursor) {
                final Cursor shifted = cursor.shift(
                    ray.builder().and(this.terms.values())
                );
                if (!shifted.end()) {
                    final String value = shifted.msg().get(attr);
                    this.terms.put(
                        value,
                        ray.builder().not(
                            ray.builder().matcher(attr, value)
                        )
                    );
                }
                return shifted;
            }
        };
    }

}
