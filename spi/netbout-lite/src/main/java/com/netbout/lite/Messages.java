/**
 * Copyright (c) 2009-2012, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the NetBout.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netbout.lite;

import com.jcabi.log.Logger;
import com.jcabi.urn.URN;
import com.netbout.spi.Bout;
import com.netbout.spi.Friend;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import com.netbout.spi.Participant;
import com.netbout.spi.Query;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Holder of messages in one bout.
 *
 * <p>The class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class Messages {

    /**
     * All messages in chronological order.
     */
    private final transient ConcurrentMap<Long, Message> all =
        new ConcurrentSkipListMap<Long, Message>();

    /**
     * All participants.
     */
    private final transient ConcurrentMap<URN, Participant> dudes =
        new ConcurrentSkipListMap<URN, Participant>();

    /**
     * Invite new dude.
     * @param friend Who to invide
     * @return Invited participant
     */
    public Participant invite(final Friend friend) {
        this.dudes.putIfAbsent(
            friend.name(),
            new LiteParticipant(friend.name(), this)
        );
        Logger.info(
            this,
            "#invite('%s'): to bout, %d in total",
            friend,
            this.dudes.size()
        );
        return this.dudes.get(friend.name());
    }

    /**
     * Kick this dude off.
     * @param friend Who to kick off
     */
    public void kickOff(final Friend friend) {
        this.dudes.remove(friend.name());
        Logger.info(
            this,
            "#kickOff('%s'): from bout",
            friend
        );
    }

    /**
     * Get all participants.
     * @return All of them
     */
    public Collection<Participant> participants() {
        final Collection<Participant> list = this.dudes.values();
        // @checkstyle AnonInnerLength (50 lines)
        return new AbstractCollection<Participant>() {
            @Override
            public Iterator<Participant> iterator() {
                return list.iterator();
            }
            @Override
            public int size() {
                return list.size();
            }
            @Override
            public boolean contains(final Object object) {
                boolean contains = false;
                for (Participant dude : list) {
                    if (dude.name().equals(object.toString())) {
                        contains = true;
                        break;
                    }
                }
                return contains;
            }
        };
    }

    /**
     * Get messages from bout.
     * @param query The query
     * @return Messages
     */
    public Iterable<Message> query(final Query query) {
        final Term term = new QueryTerm(query);
        final List<Message> msgs = new LinkedList<Message>();
        for (Message msg : this.all.values()) {
            if (term.matches(msg)) {
                msgs.add(msg);
            }
        }
        return msgs;
    }

    /**
     * Get message by number.
     * @param number Message number
     * @return Found message
     * @throws Bout.MessageNotFoundException If not found
     * @checkstyle RedundantThrows (4 lines)
     */
    public Message get(final long number) throws Bout.MessageNotFoundException {
        final Message msg = this.all.get(number);
        if (msg == null) {
            throw new Bout.MessageNotFoundException(number);
        }
        return msg;
    }

    /**
     * Create new message in bout.
     * @param text Text of the message
     * @param author Who is posting
     * @return The message just created
     */
    public Message post(final String text, final Identity author) {
        synchronized (this.all) {
            long num;
            if (this.all.isEmpty()) {
                num = 1;
            } else {
                num = Collections.max(this.all.keySet()) + 1;
            }
            final Message msg = new LiteMessage(text, author, num);
            this.all.put(num, msg);
            Logger.info(
                this,
                "#post('%[text]s', '%s'): posted msg #%d (%d chars)",
                text,
                author,
                num,
                text.length()
            );
            return msg;
        }
    }

}
