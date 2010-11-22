<?php
/**
 * netbout.com
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are PROHIBITED without prior written
 * permission from the author. This product may NOT be used
 * anywhere and on any computer except the server platform of
 * netbout.com. located at www.netbout.com. If you received this
 * code occasionally and without intent to use it, please report
 * this incident to the author by email: privacy@netbout.com
 *
 * @author Yegor Bugayenko <yegor256@yahoo.com>
 * @copyright Copyright (c) netbout.com, 2010
 * @version $Id$
 */

/**
 * One NetBout participant
 *
 * @package Model
 * @see participant.sql
 */
class Model_NetBout_Participant extends FaZend_Db_Table_ActiveRow_participant
{
    /**
     * Constants used for determine invitation status
     */
    const STATUS_CREATOR    = 'creator';

    /**
     * Default status set when invitation will be created, before sent. It is
     * important to have this status because in future we can have separate
     * process for sending invitations so we can recognize them as "to send"
     */
    const STATUS_PENDING    = 'pending';

    /**
     * Status after email hase been sent to invited user
     */
    const STATUS_INVITED    = 'invited';

    /**
     * Status when user accept this invitation
     */
    const STATUS_ACCEPTED   = 'accepted';

    /**
     * Status when user decline this invitation
     */
    const STATUS_DECLINE    = 'declined';

    /**
     * Create new participant
     *
     * @param Model_NetBout For which NetBout we want invite user
     * @param Model_User User who will be invited to participate in the NetBout
     * @param Model_User User who request for participation
     * @param string Participant status
     * @return Model_NetBout_Participant
     */
    public static function create(
        Model_NetBout $netBout,
        Model_User $invited,
        Model_User $inviter = null,
        $status = self::STATUS_PENDING)
    {
        $participant = new self();
        $participant->netBout = $netBout;
        $participant->invited = $invited;
        $participant->inviter = $inviter;

        // really random hard to guess 64 byte secret key
        $participant->secretKey = Model_User::getPasswordHash(uniqid('', true), rand());
        $participant->status = $status;
        $participant->save();

        return $participant;
    }

    /**
     * Send the invitation
     *
     * The email sent to the user shall explicitly inform him that he won't
     * receive any emails directly from NetBout participants. All communication
     * will happen online, through the netbout.com website.
     *
     * @return void
     * @throws Model_NetBout_Participant_Exception If invitation has been earlier sent
     * @todo #16:1h Implement invitation sending with html message template using
     *              {@link FaZend_Email::create()}
     */
    public function sendInvitation()
    {
        if ($this->status != self::STATUS_PENDING) {
            FaZend_Exception::raise(
                'Model_NetBout_Participant_Exception',
                'This invitation has been sent earlier'
            );
        }

        /**
         * @todo here real email should be send, to implement
         */

        // @todo remove this line when real invitations sending will be ready
        throw new Exception('Not implemented');

        $this->status = self::STATUS_INVITED;
        $this->save();
    }
}
