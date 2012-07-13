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

import com.netbout.inf.Atom;
import com.netbout.inf.Attribute;
import com.netbout.inf.Functor;
import com.netbout.inf.Ray;
import com.netbout.inf.Term;
import com.netbout.inf.atoms.TextAtom;
import com.netbout.inf.atoms.VariableAtom;
import com.netbout.inf.notices.JoinNotice;
import com.netbout.inf.notices.KickOffNotice;
import com.netbout.inf.notices.MessagePostedNotice;
import com.netbout.spi.Participant;
import java.util.List;

/**
 * Allows messages that are visible to the give person.
 *
 * <p>This class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Functor.NamedAs("talks-with")
@Functor.DependsOn(Equal.class)
final class TalksWith implements Functor {

    /**
     * The attribute to use.
     */
    private static final Attribute ATTR = new Attribute("talks");

    /**
     * {@inheritDoc}
     */
    @Override
    public Term build(final Ray ray, final List<Atom> atoms) {
        return ray.builder().matcher(
            TalksWith.ATTR,
            TextAtom.class.cast(atoms.get(0)).value()
        );
    }

    /**
     * Notice when new message is posted.
     * @param ray The ray
     * @param notice The notice
     */
    @Noticable
    public void see(final Ray ray, final MessagePostedNotice notice) {
        final long number = ray.msg(notice.message().number()).number();
        ray.cursor().delete(ray.builder().picker(number), TalksWith.ATTR);
        for (Participant dude : notice.message().bout().participants()) {
            ray.cursor().add(
                ray.builder().picker(number),
                TalksWith.ATTR,
                dude.identity().name().toString()
            );
        }
    }

    /**
     * Notice when participant removed.
     * @param ray The ray
     * @param notice The notice
     */
    @Noticable
    public void see(final Ray ray, final KickOffNotice notice) {
        ray.cursor().delete(
            ray.builder().matcher(
                VariableAtom.BOUT_NUMBER.attribute(),
                notice.bout().number().toString()
            ),
            TalksWith.ATTR,
            notice.identity().name().toString()
        );
    }

    /**
     * Notice when new participant joined.
     * @param ray The ray
     * @param notice The notice
     */
    @Noticable
    public void see(final Ray ray, final JoinNotice notice) {
        ray.cursor().add(
            ray.builder().matcher(
                VariableAtom.BOUT_NUMBER.attribute(),
                notice.bout().number().toString()
            ),
            TalksWith.ATTR,
            notice.identity().name().toString()
        );
    }

}
