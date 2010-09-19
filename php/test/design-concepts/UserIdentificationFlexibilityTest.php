<?php
/**
 * @version $Id$
 */

/**
 * @see FaZend_Test_TestCase
 */
require_once 'FaZend/Test/TestCase.php';

/**
 * User identification shall be flexible enough
 *
 * Concern: Since netbout.com should be user friendly we should provide enought
 * flexibility in our identification mechanism for not registered users.
 * How we will implement this mechanism with cooperation with standard users accounts?
 *
 * Concept:
 * We should take into account two types of users.
 * 1. Not registered, which want just create netbout, have been invited
 *
 * After user first netbout post/message user we will:
 * - add record to users table with some flag that will inform it's temporary user
 * - set long life cookie to identifiy this user
 *
 * - when this user will register new account, we will attach items created so far by him
 *   to his account
 *
 * @todo #15! TBD What extra features should has registered user,
 *           what limitation we will have for registration-free user?
 *
 * 2. For registration process we will use own implemention of registration with fields
 *    (email, password, password confirmation)
 *
 * @todo #15! Implement this test and make it workable
 */
class concepts_UserIdentificationFlexibilityTest extends FaZend_Test_TestCase
{

   /**
     * We should be able to recognize user which is not registered
     */
    public function testRegistrationFreeIdentificationWorks()
    {
        $this->markTestIncomplete();
    }

   /**
     * We should be able to recognize registered user with DB / OpenID
     */
    public function testRegisteredUserCanBeIdentified()
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