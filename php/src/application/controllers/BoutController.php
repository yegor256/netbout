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
 * Bout controller
 *
 * @package front
 */
class BoutController extends FaZend_Controller_Action
{

    /**
     * Page with bouts where user participate. Bouts displayed here can be
     * filtered by phrase or tag.
     *
     * @return void
     */
    public function searchAction()
    {
        // nothing yet
    }
    
    /**
     * Page with one bout
     *
     * @return void
     * @see views/scripts/bout/show.phtml
     */
    public function showAction()
    {
        // nothing yet
    }
    
    /**
     * Create new bout
     *
     * @return void
     * @see views/scripts/bout/create.phtml 
     */
    public function createAction()
    {
        // nothing yet
    }

    /**
     * Save new message for the bout
     *
     * @return void
     */
    public function saveMessage()
    {
        // nothing yet 
    }
    
}
