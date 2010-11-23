<?php
/**
 * @version $Id$
 */

/**
 * @see FaZend_Test_TestCase
 */
require_once 'FaZend/Test/TestCase.php';

/**
 * We should take into account two types of users.
 * 1. Not registered, which want just create netbout, have been invited
 *
 * After user first netbout post/message user we will:
 * - add record to users table with some flag that will inform it's temporary user
 * - set long life cookie to identifiy this user
 * - when this user will register new account, we will attach items created so far by him
 *   to his account
 *
 * 2. For registration process we will use own implemention of registration with fields
 *    (email, login, password, password confirmation)
 *
 * @todo #80:2h for implement #15
 * @todo #15:2hrs to implement this test and make it workable
 *           (implement UserController and required front end templates)
 */
class controllers_UserControllerTest extends FaZend_Test_TestCase
{
    public function testUserCanRegister()
    {
        $this->markTestIncomplete();
    }

    public function testUserCanLogIn()
    {
        $this->markTestIncomplete();
    }

    public function testUserCanNotLogInWithBadLogin()
    {
        $this->markTestIncomplete();
    }

    public function testUserCanNotLogInWithBadPassword()
    {
        $this->markTestIncomplete();
    }

    public function testUserCanLogout()
    {
        $this->markTestIncomplete();
    }

    /**
     * We should be able to recognize user which is not registered
     */
    public function testRegistrationFreeIdentificationWorks()
    {
        $this->markTestIncomplete();
    }

   /**
     * After user registration we should attach created so far bouts and messages
     * to his account.
     */
    public function testRegistrationFreeItemsAreAttachedToUserAccount()
    {
        $this->markTestIncomplete();
    }

}

