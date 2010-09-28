<?php
/**
 * @version $Id$
 */

class Mocks_Model_NetBout
{
    const SUBJECT = 'My test Netbout';

    public static function get($subject = self::SUBJECT, Model_User $user = null)
    {
        if ($user === null) {
            $user = Mocks_Model_User::get();
        }
        $netBout = Model_NetBout::create($subject, $user);

        return new Model_NetBout((int)(string)$netBout);
    }

}
