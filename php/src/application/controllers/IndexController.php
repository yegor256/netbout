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

/**
 * Front controller
 *
 * @package front
 */
class IndexController extends FaZend_Controller_Action
{
    
    /**
     * Default action
     *
     * @return void
     */
    public function indexAction() 
    {
        $this->_forward('front');
    }
    
    /**
     * Front
     *
     * @return void
     * @see views/scripts/index/front.phtml
     */
    public function frontAction() 
    {
        
    }
    
    /**
     * Page with one bout
     *
     * @return void
     * @see views/scripts/index/bout.phtml
     */
    public function boutAction() 
    {
        
    }
    
}
