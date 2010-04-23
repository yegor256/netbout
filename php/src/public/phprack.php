<?php
/**
 * netbout.com
 *
 * Redistribution and use in source and binary forms, with or 
 * without modification, are PROHIBITED without prior written 
 * permission from the author. This product may NOT be used 
 * anywhere and on any computer except the server platform of 
 * netbout.com. located at www.netbout.com. If you received this 
 * code occasionally and without intent to use it, please report 
 * this incident to the author by email: privacy@netbout.com
 *
 * @author Yegor Bugayenko <yegor256@yahoo.com>
 * @copyright Copyright (c) netbout.com, 2010
 * @version $Id$
 */

global $phpRackConfig;
$phpRackConfig = array(
    'auth' => array(
        'username' => 'egor',
        'password' => 'netbout',
    ),
    'dir' => dirname(__FILE__) . '/../rack-tests',
);

define('APPLICATION_PATH', realpath(dirname(__FILE__) . '/../application'));
include dirname(__FILE__) . '/../library/phpRack/bootstrap.php';
