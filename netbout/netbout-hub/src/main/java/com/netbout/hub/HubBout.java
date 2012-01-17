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
package com.netbout.hub;

import com.netbout.hub.predicates.xml.DomText;
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import com.netbout.spi.MessageNotFoundException;
import com.netbout.spi.MessagePostException;
import com.netbout.spi.Participant;
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Identity.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class HubBout implements Bout {

    /**
     * The hub.
     */
    private final transient Hub hub;

    /**
     * The viewer.
     */
    private final transient Identity viewer;

    /**
     * The data.
     */
    private final transient BoutDt data;

    /**
     * Public ctor.
     * @param ihub The hub
     * @param idnt The viewer
     * @param dat The data
     */
    public HubBout(final Hub ihub, final Identity idnt, final BoutDt dat) {
        this.hub = ihub;
        this.viewer = idnt;
        this.data = dat;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Bout bout) {
        return HubBout.recent(this).compareTo(HubBout.recent(bout));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long number() {
        return this.data.getNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String title() {
        return this.data.getTitle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date date() {
        return this.data.getDate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void confirm() {
        this.data.confirm(this.viewer.name());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void leave() {
        this.data.kickOff(this.viewer.name());
        if (this.viewer instanceof InvitationSensitive) {
            ((InvitationSensitive) this.viewer).kickedOff(this.number());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rename(final String text) {
        if (!this.confirmed()) {
            throw new IllegalStateException(
                String.format(
                    "You '%s' can't rename bout #%d until you join",
                    this.viewer,
                    this.number()
                )
            );
        }
        this.data.setTitle(text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Participant invite(final Identity friend) {
        if (!this.confirmed()) {
            throw new IllegalStateException(
                String.format(
                    "You '%s' can't invite %s until you join bout #%d",
                    this.viewer,
                    friend,
                    this.number()
                )
            );
        }
        final ParticipantDt dude = this.data.addParticipant(friend.name());
        Logger.debug(
            this,
            "#invite('%s'): success",
            friend
        );
        if (friend instanceof InvitationSensitive) {
            ((InvitationSensitive) friend).invited(this);
        }
        return new HubParticipant(this.hub, this, dude, this.data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Collection<Participant> participants() {
        final Collection<Participant> participants
            = new ArrayList<Participant>();
        for (ParticipantDt dude : this.data.getParticipants()) {
            participants.add(
                new HubParticipant(this.hub, this, dude, this.data)
            );
        }
        Logger.debug(
            this,
            "#participants(): %d participant(s) found in bout #%d",
            participants.size(),
            this.number()
        );
        return participants;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public List<Message> messages(final String query) {
        final List<MessageDt> datas =
            new ArrayList<MessageDt>(this.data.getMessages());
        final List<Message> messages = new ArrayList<Message>();
        for (MessageDt msg : datas) {
            messages.add(new HubMessage(this.hub, this.viewer, this, msg));
        }
        Collections.sort(messages, Collections.reverseOrder());
        final List<Message> result = this.filter(messages, query);
        Logger.debug(
            this,
            "#messages('%s'): %d message(s) found",
            query,
            result.size()
        );
        return result;
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public Message message(final Long num) throws MessageNotFoundException {
        final Message message = new HubMessage(
            this.hub,
            this.viewer,
            this,
            this.data.findMessage(num)
        );
        Logger.debug(
            this,
            "#message(#%d): found",
            num
        );
        return message;
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public Message post(final String text) throws MessagePostException {
        if (!this.confirmed()) {
            throw new IllegalStateException(
                String.format(
                    "You '%s' can't post to bout #%d until you join",
                    this.viewer,
                    this.number()
                )
            );
        }
        try {
            new DomText(text).validate(this.hub);
        } catch (com.netbout.hub.predicates.xml.DomValidationException ex) {
            throw new MessagePostException(ex);
        }
        final MessageDt msg = this.data.addMessage();
        msg.setDate(new Date());
        msg.setAuthor(this.viewer.name());
        msg.setText(text);
        Logger.debug(
            this,
            "#post('%s'): message posted",
            text
        );
        final Message message = new HubMessage(
            this.hub,
            this.viewer,
            this,
            msg
        );
        message.text();
        this.hub.make("notify-bout-participants")
            .arg(this.number())
            .arg(message.number())
            .asDefault(false)
            .exec();
        return message;
    }

    /**
     * This identity has confirmed participation?
     * @return Is it?
     */
    private boolean confirmed() {
        for (Participant dude : this.participants()) {
            if (dude.identity().equals(this.viewer)) {
                return dude.confirmed();
            }
        }
        throw new IllegalStateException(
            Logger.format(
                "Can't find myself ('%s') among %d participants: %[list]s",
                this.viewer,
                this.participants().size(),
                this.participants()
            )
        );
    }

    /**
     * Maximum date of a bout.
     * @param bout The bout to work with
     * @return Its recent date
     */
    protected static Date recent(final Bout bout) {
        final List<Message> msgs = bout.messages("(equal $pos 0)");
        Date recent = bout.date();
        if (!msgs.isEmpty()) {
            final Date mdate = msgs.get(0).date();
            if (mdate.before(recent)) {
                throw new IllegalArgumentException(
                    String.format(
                        // @checkstyle LineLength (1 line)
                        "Message #%d in bout #%d created on '%s', which before bout was created '%s', how come?",
                        msgs.get(0).number(),
                        bout.number(),
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
     * Filter list of messages with a predicate.
     * @param list The list to filter
     * @param query The query
     * @return New list of them
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public List<Message> filter(final List<Message> list,
        final String query) {
        final List<Message> result = new ArrayList<Message>();
        final Predicate predicate = new PredicateBuilder(this.hub).parse(query);
        for (Message msg : list) {
            boolean visible = true;
            if (!query.isEmpty()) {
                final Object response = predicate.evaluate(msg, result.size());
                if (response instanceof Boolean) {
                    visible = (Boolean) response;
                } else if (response instanceof String) {
                    result.add(new PlainMessage(this, (String) response));
                    break;
                } else {
                    throw new IllegalArgumentException(
                        Logger.format(
                            "Can't understand %[type]s response from '%s'",
                            response,
                            query
                        )
                    );
                }
            }
            if (visible) {
                result.add(msg);
            }
        }
        if (list.isEmpty()) {
            final Object response = predicate.evaluate(
                new PlainMessage(this, ""), 0
            );
            if (response instanceof String) {
                result.add(new PlainMessage(this, (String) response));
            }
        }
        return result;
    }

}
