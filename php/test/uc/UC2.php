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

class uc_UC2Test extends FaZend_Test_TestCase
{
    /**
     * @todo #80:2hrs Make this test workable
     */
    public function testCreatesNewBout()
    {
        // @todo #80 Remove this line when the test is ready
        $this->markTestIncomplete();

        if (Model_User::isLoggedIn()) {
            Model_User::logOut();
        }

        $response = $this->dispatch('/b/create');

        // check user is redirected to login form
        $this->assertRedirectTo('/u/login');

        $user = Mocks_Model_User::get();
        $user->logIn();

        // check whether form is displayed
        $this->resetRequest()->resetResponse();
        $response = $this->dispatch('/b/create');
        $this->assertResponseCode(200, 'returned response code is NOT equal to 200, why?');
        $this->assertQuery('form#newBoutForm', 'new bout form is not displayed, why?');
        $this->assertQuery(
            'form#newBoutForm input[name=subject]',
            'new netbout form does NOT contain "subject" field, why?'
        );

        // submit form
        $this->resetRequest()->resetResponse();
        $subject = 'My test subject';
        $this->request->setMethod('POST')->setPost(
            array('subject', $subject)
        );
        $this->dispatch('/b/create');

        $row = FaZend_Db_Table_ActiveRow_bout::retrieve()
            ->where('subject = ?', $subject)
            ->setSilenceIfEmpty()
            ->fetchRow();
        $this->assertNotNull($row, 'bout was not created, why?');

        // check we are redirected to bout page and bout is visible for us
        $boutUrl = "/b/{$row['id']}";
        $this->assertRedirectTo($boutUrl, 'we are not redirected to bout page after submit, why?');

        // get bout page content
        $this->resetRequest()->resetResponse();
        $this->dispatch($boutUrl);
        $this->assertResponseCode(200, 'returned response code is NOT equal to 200, why?');
        $this->assertQueryContentContains('h1', $subject, 'bout subject is NOT displayed in H1 tag');
    }

}