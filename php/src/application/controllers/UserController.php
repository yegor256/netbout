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

/**
 * @see FaZend_Controller_Action
 */
require_once 'FaZend/Controller/Action.php';

/**
 * Manipulations with user account, we should use as most as possible the parent
 * class {@link Fazend_UserController} to avoid duplicated code
 *
 * @package front
 */
class UserController extends Fazend_UserController
{
    
    /**
     * Login
     *
     * @return void
     * @see views/scripts/user/login.phtml
     */
    public function loginAction() 
    {
    }
    
    /**
     * Account management
     *
     * @return void
     * @see views/scripts/user/account.phtml
     */
    public function accountAction() 
    {
        
    }

}
