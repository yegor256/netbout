<?php
/**
 * @version $Id$
 */

require_once 'FaZend/Test/TestCase.php';

class Model_UserTest extends FaZend_Test_TestCase 
{

    public function testWeCanCheckUserEmailIsBusy()
    {
        $email = 'busytest@example.com';
        $this->assertFalse(Model_User::isEmailBusy($email));
        
        $user = Mocks_Model_User::get($email);
        $this->assertTrue(Model_User::isEmailBusy($email));
    }

}

