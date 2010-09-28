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
 * One NetBout
 *
 * @package Model
 * @see netBout.sql
 */
class Model_NetBout extends FaZend_Db_Table_ActiveRow_netBout implements Zend_Acl_Resource_Interface
{
    /**
     * Returns the string identifier of the Resource
     *
     * @return string
     * @see Zend_Acl_Resource_Interface
     */
    public function getResourceId()
    {
        return 'netBout:' . strval($this);
    }

    /**
     * Create new netBout
     *
     * @param string Subject of the NetBout
     * @param Model_User Author of the NetBout
     * @return Model_NetBout
     */
    public function create($subject, Model_User $user)
    {
        $netBout = new self();
        $netBout->subject = $subject;
        $netBout->user = $user;
        $netBout->save();

        logg("New bout '%s' created by user '#%s'", $subject, $user);
        return $netBout;
    }
}
