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

/**
 * Allows messages that are visible to the give person.
 *
 * <p>This class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@NamedAs("talks-with")
final class TalksWith implements Functor, Noticable<MessagePostedNotice>,
    Noticable<KickOffNotice>, Noticable<JoinNotice> {

    /**
     * The attribute to use.
     */
    private static final String ATTR = "talks-with";

    /**
     * {@inheritDoc}
     */
    @Override
    final Term build(final Ray ray, final List<Atom> atoms) {
        return ray.builder().matcher(
            TalksWith.ATTR,
            TextAtom.class.cast(atoms.get(0)).value()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void see(final Ray ray, final MessagePostedNotice notice) {
        final Msg msg = ray.create(notice.message().number());
        msg.delete(TalksWith.ATTR);
        for (Participant dude : notice.message().bout().participants()) {
            msg.add(TalksWith.ATTR, dude.identity().name().toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void see(final Ray ray, final KickOffNotice notice) {
        ray.cursor.delete(
            ray.builder().matcher(
                VariableAtom.BOUT_NUMBER.attribute(),
                notice.bout().number().toString()
            ),
            TalksWith.ATTR,
            notice.identity().name().toString()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void see(final Ray ray, final JoinNotice notice) {
        ray.cursor.add(
            ray.builder().matcher(
                VariableAtom.BOUT_NUMBER.attribute(),
                notice.bout().number().toString()
            ),
            TalksWith.ATTR,
            notice.identity().name().toString()
        );
    }

}
