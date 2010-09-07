<?php
/**
 * @version $Id: Starter.php 367 2010-08-26 07:50:25Z yegor256@yahoo.com $
 */

/**
 * @see FaZend_Test_Starter
 */
require_once 'FaZend/Test/Starter.php';

class Starter extends FaZend_Test_Starter
{

	protected function _startCleanDb()
	{
        $this->_bootstrap('db');
        $this->_bootstrap('fz_profiler');
        $this->_dropDatabase();
	}
	
}

