<?php
/**
 * @version $Id$
 */

/**
 * @todo #80! Implement user creation part in correct way.
 */
class Mocks_Actor_User
{
    const EMAIL = 'test@example.com';

    public static function get($email = self::EMAIL, $login = null, $password = null)
    {
        if ($email !== null) {
            $user = FaZend_Db_Table_ActiveRow_user::retrieve()
                ->where('email = ?', $email)
                ->setSilenceIfEmpty()
                ->fetchRow();
        } else {
            $user = FaZend_Db_Table_ActiveRow_user::retrieve()
                ->where('login = ?', $login)
                ->setSilenceIfEmpty()
                ->fetchRow();
        }

        if (!$user) {
            // @todo #80! Model_User is used because it contain method which hash password
            $user = Model_User::create($email, $login, $password);
        }

        return new FaZend_Db_Table_ActiveRow_user((int)(string)$user);
    }

    /**
     * Login user with the given email address.
     */
    public static function login($email = null)
    {
        $user = Mocks_Model_User::get($email);
        $user->logIn();
    }

    /**
     * Logout the Actor, if it's logged in already. If not, just
     * do nothing.
     */
    public static function logout()
    {
        if (Model_User::isLoggedIn()) {
            Model_User::logOut();
        }
    }

}
