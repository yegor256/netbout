<?php
/**
 * @version $Id: RestControllerTest.php 28 2010-07-25 15:23:31Z yegor256@yahoo.com $
 */

/**
 * @see FaZend_Test_TestCase
 */
require_once 'FaZend/Test/TestCase.php';

class controllers_IndexControllerTest extends FaZend_Test_TestCase
{

    public function testDefaultFrontPage()
    {
        $this->dispatch('/');
        $this->assertController('index');
    }

}

