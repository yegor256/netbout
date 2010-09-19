<?php
/**
 * @version $Id$
 */

class Mocks_Model_User
{
    const EMAIL = 'test@example.com';

    public static function get($email = self::EMAIL)
    {
        try {
            $user = Model_User::findByEmail($email);
        } catch (Model_User_NotFoundException $e) {
            $user = Model_User::create($email);
        }

        return new Model_User((int)(string)$user);
    }

}
