<?php
/**
 *
 * @version $Id$
 */

/**
 *
 * @see FaZend_Test_TestCase
 */
require_once 'FaZend/Test/TestCase.php';

/**
 * @todo #80:2hrs Make this test workable
 */
class uc_UC6Test extends FaZend_Test_TestCase
{
    public function testDisplaysLoginForm()
    {
        // @todo #80 Remove this line when the test is ready
        $this->markTestIncomplete();

        $response = $this->dispatch('/u/login');
        $this->assertResponseCode(200, 'returned response code is NOT equal to 200, why?');
        $this->assertQuery('form#loginForm', 'login form is NOT displayed, why?');
        $this->assertQuery('form#loginForm input[name=login]', 'login form does NOT contain "login" field, why?');
        $this->assertQuery('form#loginForm input[name=authToken]', 'login form does NOT contain "authToken" field, why?');
    }

    public function testRecognizesCorrectUserEmailCredentials()
    {
        // @todo #80 Remove this line when the test is ready
        $this->markTestIncomplete();

        $password = 'mypassword';
        $user = Mocks_Actor_User::get('my@test.com', null, $password);
        $this->_sendLoginForm($user->email, $password);
        $this->assertRedirectTo('/u/account');
    }

    public function testRecognizesCorrectUserLoginCredentials()
    {
        // @todo #80 Remove this line when the test is ready
        $this->markTestIncomplete();

        $password = 'mypassword';
        $user = Mocks_Actor_User::get(null, 'mylogin', $password);
        $this->_sendLoginForm($user->login, $password);
        $this->assertRedirectTo('/u/account');
    }

    public function testRecognizesWrongUserEmailCredentials()
    {
        // @todo #80 Remove this line when the test is ready
        $this->markTestIncomplete();

        $password = 'mypassword';
        $user = Mocks_Actor_User::get('my@test.com', null, $password);
        $this->_sendLoginForm($user->login, 'wrongpassword');
        $this->assertNotRedirect();
        $this->assertResponseCode(200, 'returned response code is NOT equal to 200, why?');
        $this->assertQuery('form#loginForm', 'user form is not displayed again, why?');
    }

    public function testRecognizesWrongUserLoginCredentials()
    {
        // @todo #80 Remove this line when the test is ready
        $this->markTestIncomplete();

        $password = 'mypassword';
        $user = Mocks_Actor_User::get(null, 'mylogin', $password);
        $this->_sendLoginForm($user->login, 'wrongpassword');
        $this->assertNotRedirect();
        $this->assertResponseCode(200, 'returned response code is NOT equal to 200, why?');
        $this->assertQuery('form#loginForm', 'user form is not displayed again, why?');
    }

    protected function _sendLoginForm($login, $authToken)
    {
        $this->request->setMethod('POST')->setPost(
            array(
                'login'     => $login,
                'authToken' => $authToken
            )
        );

        $this->dispatch('/u/login');
    }

}