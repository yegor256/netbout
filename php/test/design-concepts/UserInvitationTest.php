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
 * Concern: "When the user receives a notification about an invitation into
 * a NetBout, he has an option to accept it or decline. If he doesn't accept it
 * during 7 days, it means he declines it. When invitation is accepted the user
 * starts talking in the NetBout.". How we will implement this functionality?
 *
 * Concept: We will send an email to invited user.
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
 * - notify user which sent invitation about invited user decision
 *
 * @todo #16 How we should notify user who invite other person
 *           (email, message in netbout, other method...)?
 *
 * @todo #16 Implemenent this test and make it workable
 */
class concepts_UserInvitationTest extends FaZend_Test_TestCase
{

    /**
     * Invitation send
     *
     * The email sent to the user shall explicitly inform him that he won't
     * receive any emails directly from NetBout participants. All communication
     * will happen online, through the netbout.com website.
     */
    public function testInvitationCanBeSent()
    {
        $this->markTestIncomplete();
    }

   /**
     * Invitation accepted
     *
     * Invited user should be able, to accept invitation and thanks for that
     * should be allowed to view netbout without registation.
     * When user click on "ACCEPT invitation" he will be redirected to NetBout
     * for which he was invited. We must remember this user as permitted to
     * view this NetBout.
     */
    public function testInvitationCanBeAccepted()
    {
        $this->markTestIncomplete();
    }

   /**
     * Invitation declined
     *
     * Invited user should be able, to decline invitation.
     */
    public function testInvitationCanBeDeclined()
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
    public function testInvitedUserCanPermanentlyBlockHisEmail()
    {
       $this->markTestIncomplete();
    }

   /**
     * Expired invitations
     *
     * Not accepted invitation should be automatically declined after some
     * configured interval of time (7 days)
     */
    public function testInvitationAreAutomaticallyDeclinedAfterExpirationInterval()
    {
       $this->markTestIncomplete();
    }
        
}