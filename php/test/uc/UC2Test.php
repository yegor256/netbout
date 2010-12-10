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
    public function setUp()
    {
        parent::setUp();
        /**
         * When response is XML document we must register namespace.
         * CSS selectors does NOT work with XML namespaces, so we use
         * Xpath quries with namespace prefix.
         */
        $this->registerXpathNamespaces(array('x' => 'http://www.w3.org/1999/xhtml'));
    }

    public function resetResponse()
    {
        parent::resetResponse();

        /**
         * We must reset instances, because in other case we have wrong field
         * names in forms when we use some page in test method few times
         */
        FaZend_View_Helper_Forma::cleanInstances();
    }

    /**
     * @todo #80:2hrs Make this test workable
     */
    public function testCreatesNewBout()
    {
        Mocks_Actor_User::logout();

        $response = $this->dispatch('/b/create');

        // check user is redirected to login form
        $this->assertRedirectTo('/u/login');

        /**
         * @todo #88: Login by email is currently NOT supported,
         *            because as identity column we use always login.
         *            Need to be fixed, temporary changed to use login column.
         *            Uncomment line below when this functionality will be ready
         *            and remove next.
         */
        // Mocks_Actor_User::login();
        Mocks_Actor_User::login(null, 'myLogin');

        // check whether form is displayed
        $this->resetRequest()->resetResponse();
        $response = $this->dispatch('/b/create');
        $this->assertResponseCode(200, 'returned response code is NOT equal to 200, why?');

        $this->assertXpath('//x:form[@id="newBoutForm"]', 'new bout form is not displayed, why?');
        $this->assertXpath(
            '//x:form[@id="newBoutForm"]//x:input[@name="subject"]',
            'new netbout form does NOT contain "subject" field, why?'
        );

        // submit form
        $this->resetRequest()->resetResponse();
        $subject = 'My test subject';
        $this->request->setMethod('POST')->setPost(
            array(
                'subject' => $subject,
                'submit'  => 'Start New Bout'
            )
        );
        $this->dispatch('/b/create');

        $row = FaZend_Db_Table_ActiveRow_netbout::retrieve()
            ->where('subject = ?', $subject)
            ->setSilenceIfEmpty()
            ->fetchRow();

        $this->assertTrue($row != false, 'bout was not created, why?');

        // @todo #80 Remove this line when the test is ready
        $this->markTestIncomplete();

        // check we are redirected to bout page and bout is visible for us
        $boutUrl = "/b/{$row['id']}";
        $this->assertRedirectTo($boutUrl, 'we are not redirected to bout page after submit, why?');

        // get bout page content
        $this->resetRequest()->resetResponse();
        $this->dispatch($boutUrl);
        $this->assertResponseCode(200, 'returned response code is NOT equal to 200, why?');
        $this->assertXpathContentContains('//x:h1', $subject, 'bout subject is NOT displayed in H1 tag');
    }

}