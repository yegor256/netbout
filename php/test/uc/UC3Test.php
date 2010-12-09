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

class uc_UC3Test extends FaZend_Test_TestCase
{
    /**
     * @todo #80:1hr Make this test workable
     */
    public function testAddsNewMessageToTheBout()
    {
        // @todo #80 Remove this line when the test is ready
        $this->markTestIncomplete();

        $bout = Mocks_Entity_NetBout::get();

        // this URL is used for reading and writing to the Bout (RESTful approach)
        $url = "/b/{$bout['id']}";

        // reading Bout
        $this->dispatch($url);
        $this->assertResponseCode(200, 'returned response code is NOT equal to 200, why?');
        $this->assertQuery('form#newMessageForm', 'new message form is not displayed, why?');
        $this->assertQuery(
            'form#newMessageForm input[name=text]',
            'new message form does NOT contain "text" field, why?'
        );
        $this->assertQuery(
            'form#newMessageForm input[name=bout]',
            'new message form does NOT contain "bout" field, why?'
        );

        // submit new message form
        $this->resetRequest()->resetResponse();
        $text = 'My unique message ' . uniqid('', true);
        $this->getRequest()
            ->setMethod('POST')
            ->setPost(array('text' => $text));
        $this->dispatch($url);
        $this->assertRedirectTo($url, 'we are not redirected to bout page, why?');

        // reading again to find the message just posted
        $this->resetRequest()->resetResponse();
        $this->dispatch($url);
        $this->assertResponseCode(200, 'returned response code is NOT equal to 200, why?');
        $this->assertQueryContentContains('div', $text, 'message text is NOT displayed in div tag');
    }

}