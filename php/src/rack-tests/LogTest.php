<?php
/**
 * @version $Id: LogTest.php 135 2010-08-04 16:14:28Z yegor256@yahoo.com $
 */

class LogTest extends PhpRack_Test
{

    protected function _init()
    {
        $this->setAjaxOptions(
            array(
                'autoStart' => false, // don't reload it from start
            )
        );
    }

    public function testShowLog()
    {
        $this->assert->disc->file
            ->tailf(APPLICATION_PATH . '/../../netbout.log'); 
    }

}   