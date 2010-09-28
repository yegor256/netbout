<?php
/**
 * @version $Id$
 */

require_once 'FaZend/Test/TestCase.php';

/**
 * @todo #16:1h Implemenent this test and make it workable,
 * (We should test this functionality using real mailbox, because it is really important
 *  that this part work correctly in real conditions)
 */
class Model_NetBout_InvitationTest extends FaZend_Test_TestCase
{
    public function testClassExists()
    {
        $this->assertTrue(class_exists(substr(get_class(), 0, -4)));
    }

    public function testInvitationCanBeSent()
    {
        $inviter = Mocks_Model_User::get(null, 'testuser1');
        $invited = Mocks_Model_User::get('invited@email.com');
        $netBout = Mocks_Model_NetBout::get();

        $invitation = Model_NetBout_Invitation::create($netBout, $invited, $inviter);

        // @todo remove this line when this test will be workable
        $this->markTestIncomplete();

        $invitation->send();
    }

    public function testInvitationCanBeAccepted()
    {
        $this->markTestIncomplete();
    }

    public function testInvitationCanBeDeclined()
    {
        $this->markTestIncomplete();
    }

    /**
     * When user accept invitation, it should be able to participate in the
     * NetBout for which he was invited
     *
     * @todo #16:2h Model_Acl should be implemented with persistence storage to make this
     * scenario possible
     */
    public function testUserHasAccetToNetBoutAfterInvitationAcceptance()
    {
        $this->markTestIncomplete();

        $inviter = Mocks_Model_User::get(null, 'testuser1');
        $netBout = Mocks_Model_NetBout::get($inviter);
        $this->assertTrue(Model_Acl::isAllowed($inviter, $netBout));

        $invited = Mocks_Model_User::get('invited@email.com');

        $this->assertFalse(Model_Acl::isAllowed($user, $netBout, Model_Acl::PRIVILEGE_PARTICIPATE));
        $invitation->accept();
        $this->assertTrue(Model_Acl::isAllowed($user, $netBout, Model_Acl::PRIVILEGE_PARTICIPATE));
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
