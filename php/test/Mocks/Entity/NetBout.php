<?php
/**
 * @version $Id$
 */

/**
 * @todo #80:0.5hr To remove any calls to Model_* related classes, use only Zend or FaZend classes
 */
class Mocks_Entity_NetBout
{
    const SUBJECT = 'My test Netbout';

    public static function get($subject = self::SUBJECT, Model_User $user = null)
    {
        /**
         * @todo #80 Replace this call by own implemenetation with FaZend_Db_Table_ActiveRow_*
         *           Need to add also $user with status='creator' to participant table.
         */
        return Mocks_Model_NetBout::get($subject, $user);
    }

}
