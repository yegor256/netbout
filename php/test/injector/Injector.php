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

    protected function _injectLocale()
    {
        // just to test that localization works properly (and visually :)
        // Zend_Locale::setDefault('ru');
    }

}

