<?php
/**
 * @version $Id$
 */

/**
 * @see FaZend_Test_TestCase
 */
require_once 'FaZend/Test/TestCase.php';

/**
 * Invited user should be able to decide about participation in a NetBout.
 *
 * We will send an email to invited user.
 * It will contain invitation details like sender name, netbout title,
 * truncated description and 3 LINKS which will provide functionality for
 * a) ACCEPT invitation
 * b) DECLINE invitation
 * c) permanently BLOCK sending invitations from netbout.com to this email
 *
 * Above links should contain uniqueId which identify this invitation.
 * This id should not basing on auto_increment field because must be hard to
 * guess for other not permitted users.
 *
 * When invited user click on one of above links we should
 * - perform selected action (save this decision in our db, add permissions)
 * - notify inviter about invited user decision by email
 *
 * @todo #16:1h Implemenent this test and make it workable.
 */
class controllers_InvitationControllerTest extends FaZend_Test_TestCase
{
   /**
     * Invitation accepted
     *
     * Invited user should be able, to accept invitation and thanks for that
     * should be allowed to view netbout without registation.
     * When user click on "ACCEPT invitation" he will be redirected to NetBout
     * for which he was invited. We must remember this user as permitted to
     * view this NetBout.
     */
    public function testInvitedPersonCanAcceptInvitation()
    {
        $this->markTestIncomplete();
    }

   /**
     * Invitation declined
     *
     * Invited user should be able, to decline invitation.
     */
    public function testInvitedPersonCanDeclineInvitation()
    {
        $this->markTestIncomplete();
    }

   /**
     * Email block
     *
     * The user must have an option to block his email in netbout.com, and no more
     * invitations will be sent to him. When someone will try to invite him to
     * a NetBout, the SUD shall decline the operation immediately
     */
    public function testInvitedPersonCanPermanentlyBlockHisEmail()
    {
       $this->markTestIncomplete();
    }
}