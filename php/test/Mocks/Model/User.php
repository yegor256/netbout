<?php
/**
 * @version $Id$
 */

class Mocks_Model_User
{
    const EMAIL = 'test@example.com';

    public static function get($email = self::EMAIL, $login = null, $password = null)
    {
        try {
            if ($email !== null) {
                $user = Model_User::findByEmail($email);
            } else {
                $user = Model_User::findByLogin($login);
            }
        } catch (Model_User_NotFoundException $e) {
            $user = Model_User::create($email, $login, $password);
        }

        return new Model_User((int)(string)$user);
    }

}
