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
package com.netbout.spi;

import com.jcabi.log.Logger;
import com.jcabi.urn.URN;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

/**
 * Bout, a conversation room.
 *
 * <p>Bouts are comparable by their numbers.
 *
 * <p>Instances of this interface must be thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
public interface Bout extends Comparable<Bout> {

    /**
     * If this person is already in the bout.
     * @see Bout#invite(Friend)
     */
    class DuplicateInvitationException extends Exception {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = 0x7526FA7AEBD21470L;
        /**
         * Public ctor.
         * @param cause The cause of the problem
         */
        public DuplicateInvitationException(final String cause) {
            super(cause);
        }
    }

    /**
     * Thowable when message can't be posted.
     * @see Bout#post(String)
     */
    class MessagePostException extends Exception {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = 0x8726FA78BBD21470L;
        /**
         * Public ctor.
         * @param cause The cause of the problem
         */
        public MessagePostException(final String cause) {
            super(cause);
        }
        /**
         * Public ctor.
         * @param cause The cause of the problem
         */
        public MessagePostException(final Throwable cause) {
            super(cause);
        }
        /**
         * Public ctor.
         * @param desc Description of the problem
         * @param cause The cause of the problem
         */
        public MessagePostException(final String desc, final Throwable cause) {
            super(desc, cause);
        }
    }

    /**
     * Thowable when message is not found.
     * @see Bout#message(Long)
     */
    class MessageNotFoundException extends Exception {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = 0x7526FA78EED67470L;
        /**
         * Public ctor.
         * @param num Number of it
         */
        public MessageNotFoundException(final Long num) {
            super(String.format("Message #%d not found", num));
        }
    }

    /**
     * Get its unique number.
     * @return The number of the bout
     */
    Long number();

    /**
     * When it was created.
     * @return The date of creation
     */
    Date date();

    /**
     * Get its title.
     * @return The title of the bout
     */
    String title();

    /**
     * Set its title.
     * @param text The title of the bout
     */
    void rename(String text);

    /**
     * Get all its participants.
     *
     * In order to check whether certain identity belongs to the list of
     * participants you can use {@link Collection#contains(Object)} method with
     * {@link String}, {@link Friend}, {@link URN}, {@link Identity}
     * or {@link Participant} as an argument. Actually, no matter what is the
     * type of the argument, only its {@code #toString()} result will matter.
     *
     * @return The list of them
     */
    Collection<Participant> participants();

    /**
     * Confirm participantion in this bout.
     */
    void confirm();

    /**
     * Leave this bout.
     */
    void leave();

    /**
     * Invite new participant.
     * @param identity Identity of the participant
     * @return This new participant
     * @throws Bout.DuplicateInvitationException If this person is already here
     */
    Participant invite(Friend identity)
        throws Bout.DuplicateInvitationException;

    /**
     * Get ordered list of all messages of the bout.
     * @param query Search query, if necessary
     * @return The list of them
     */
    Iterable<Message> messages(Query query);

    /**
     * Find message by ID.
     * @param number Number of the message to get
     * @return The message
     * @throws Bout.MessageNotFoundException If not found
     */
    Message message(Long number) throws Bout.MessageNotFoundException;

    /**
     * Post a new message.
     * @param text The text of the new message
     * @return The message just posted
     * @throws Bout.MessagePostException If can't post it for some reason
     */
    Message post(String text) throws Bout.MessagePostException;

    /**
     * Smart implementation of {@link Bout}, which extends it with useful
     * features.
     */
    class Smart implements Bout {
        /**
         * Original bout.
         */
        private final transient Bout origin;
        /**
         * Public ctor.
         * @param bout The original bout
         */
        public Smart(final Bout bout) {
            this.origin = bout;
        }
        /**
         * Get the latest date of the bout (when it was updated by anyone).
         * @return Its recent date
         */
        public Date updated() {
            final Iterable<Message> msgs = this.latest();
            Date recent = this.date();
            if (msgs.iterator().hasNext()) {
                final Message msg = msgs.iterator().next();
                final Date mdate = msg.date();
                if (mdate.before(recent)) {
                    throw new IllegalArgumentException(
                        String.format(
                            // @checkstyle LineLength (1 line)
                            "Message #%d in bout #%d created on '%s', which before bout was created '%s', how come?",
                            msg.number(),
                            this.number(),
                            mdate,
                            recent
                        )
                    );
                }
                recent = mdate;
            }
            return recent;
        }
        /**
         * This bout was seen already and has nothing new to read?
         * @return TRUE if this bout doesn't have anything new to read
         * @todo #332 This implementation is very ineffective and should be
         *  changed. We should introduce a new predicate in INF and use it.
         *  Something like "(seen-by ?)".
         */
        public boolean seen() {
            final Iterable<Message> msgs = this.latest();
            boolean seen = true;
            if (msgs.iterator().hasNext()) {
                seen = msgs.iterator().next().seen();
            }
            return seen;
        }
        /**
         * Find participant in the list of participants (or throw a runtime
         * exception if not found).
         * @param obj Participant, with {@code #toString()}
         * @return Participant found
         */
        public Participant participant(final Object obj) {
            String name;
            if (obj instanceof Friend) {
                name = Friend.class.cast(obj).name().toString();
            } else {
                name = obj.toString();
            }
            Participant found = null;
            final Collection<URN> names = new LinkedList<URN>();
            for (Participant dude : this.participants()) {
                names.add(dude.name());
                if (dude.name().equals(name)) {
                    found = dude;
                    break;
                }
            }
            if (found == null) {
                throw new IllegalStateException(
                    Logger.format(
                        "Can't find '%s' in %s among %d participants: %[list]s",
                        name,
                        this,
                        names.size(),
                        names
                    )
                );
            }
            return found;
        }
        /**
         * Get a collection with one latest message.
         * @return Collection with one msg
         */
        private Iterable<Message> latest() {
            return this.messages(new Query.Textual("(pos 0)"));
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(final Bout bout) {
            return this.origin.compareTo(bout);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(final Object bout) {
            return this.origin.equals(bout);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return this.origin.hashCode();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return this.origin.toString();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Long number() {
            return this.origin.number();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String title() {
            return this.origin.title();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Date date() {
            return this.origin.date();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void rename(final String text) {
            this.origin.rename(text);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void confirm() {
            this.origin.confirm();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void leave() {
            this.origin.leave();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Participant invite(final Friend friend)
            throws Bout.DuplicateInvitationException {
            return this.origin.invite(friend);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<Participant> participants() {
            return this.origin.participants();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Iterable<Message> messages(final Query query) {
            return this.origin.messages(query);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Message message(final Long num)
            throws Bout.MessageNotFoundException {
            return this.origin.message(num);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Message post(final String text)
            throws Bout.MessagePostException {
            return this.origin.post(text);
        }
    }

}
