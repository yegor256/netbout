<?php
/**
 * @version $Id$
 */

/**
 * @todo #80: Currently is just proxy for Mocks_Model_NetBout due to time limitation.
 *            Implement this part in correct way.
 */
class Mocks_Entity_NetBout
{
    const SUBJECT = 'My test Netbout';

    public static function get($subject = self::SUBJECT, Model_User $user = null)
    {
        return Mocks_Model_NetBout::get($subject, $user);
    }

}
