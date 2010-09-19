<?php
/**
 * @version $Id: Injector.php 395 2010-08-29 16:07:21Z yegor256@yahoo.com $
 */

/**
 * @see FaZend_Test_Injector
 */
require_once 'FaZend/Test/Injector.php';

class Injector extends FaZend_Test_Injector
{
    protected function _injectDbConnection()
    {
        /**
         * It's important to keep this initialization here, in
         * order to avoid multiple connections to DB during testing.
         */
        $this->_bootstrap('db');
    }

    protected function _injectTestUser()
    {
        $this->_bootstrap('fz_orm');
        $this->_bootstrap('fz_deployer');
        $this->_bootstrap('fz_session');
        FaZend_User::setRowClass('Model_User');
        FaZend_User::setIdentityProperty('__id');
        FaZend_User::setIdentityColumn('id');

        $user = Mocks_Model_User::get();
        $user->logIn();
    }

    protected function _injectLocale()
    {
        // just to test that localization works properly (and visually :)
        Zend_Locale::setDefault('en');
    }

}

