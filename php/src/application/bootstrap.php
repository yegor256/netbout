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
 * @version $Id: global.css 3 2010-04-23 21:02:37Z yegor256@yahoo.com $
 */

// tpc2.com colors
define('TPC_DARKGRAY', '#5a5a5a');
define('TPC_GRAY', '#7d8f9b');
define('TPC_LIGHTGRAY', '#e7e9ea');
define('TPC_BLACK', '#122632');
define('TPC_BLUE', '#0093d0');
define('TPC_RED', '#e31b23');
define('TPC_GREEN', '#54b948');
define('TPC_ORANGE', 'orange');
define('TPC_YELLOW', '#fff454');
define('TPC_WHITE', '#ffffff');

// fazend blue colors
define('FZ_BLUE1', '#062B35'); // dark blue, almost black
define('FZ_BLUE2', '#134558'); // dark blue, in logo
define('FZ_BLUE3', '#2276A4'); // blue, in logo
define('FZ_BLUE4', '#92BECE'); // light blue

/**
 * Bootstraper
 *
 * @package application
 */
class Bootstrap extends FaZend_Application_Bootstrap_Bootstrap
{

    /**
     * Initialize it.
     *
     * This method has to be the first in bootstrap!
     *
     * @return void
     */
    protected function _initAll()
    {
        if (function_exists('mb_internal_encoding')) {
            mb_internal_encoding('UTF-8');
        }
        $this->bootstrap('fz_logger');
        $this->bootstrap('fz_starter');
    }
    
    /**
     * Init non-explicit ORM mapping rules
     *
     * @return void
     */
    protected function _initOrmMapping()
    {
        $this->bootstrap('fz_orm');
        $converters = array(
            'dates' => array(
                'regexs' => array(
                    '/^.*?\.(?:created)$/',
                ),
                'converter' => FaZend_Callback::factory('new Zend_Date(${a1}, Zend_Date::ISO_8601);')
            ),
        );

        foreach ($converters as $converter) {
            foreach ($converter['regexs'] as $regex) {
                FaZend_Db_Table_ActiveRow::addMapping(
                    $regex,
                    $converter['converter']
                );
            }
        }
    }

    /**
     * Initialize FaZend_User
     *
     * @return void
     */
    protected function _initFaZendUser()
    {
        $this->bootstrap('fz_orm');
        FaZend_User::setRowClass('Model_User');
        FaZend_User::setIdentityProperty('__id');
        FaZend_User::setIdentityColumn('id');
        FaZend_User::setCredentialProperty('authToken');
        FaZend_User::setCredentialColumn('authToken');
    }
    
    /**
     * Emailer reconfigure for the specific language.
     *
     * @return void
     */
    protected function _initEmailer()
    {
        $defs = array_keys(Zend_Locale::getDefault());
        $lang = array_shift($defs);
        $folder = APPLICATION_PATH . '/views/emails';

        $this->bootstrap('fz_email');
        $email = FaZend_Email::getDefaultEmail();
        $email->setFolders(
            array(
                $folder . '/en',
                $folder . '/' . $lang
            )
        );
    }

}

