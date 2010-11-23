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
 * One user, called ActorVisitor
 *
 * Since class inherits FaZend_User properties, HTTP SESSION ID is managed
 * inside the parent class and should not be touched here. In order to get
 * currently logged in user just use {@link Model_User::me()}, which will
 * return an instance of this class.
 *
 * @property string $email Email address of the user, inherited from {@link FaZend_User}
 * @property string $password Password hash, inherited from {@link FaZend_User}
 * @property Zend_Date $created When account was created, see {@link user.sql}
 *
 * @package Model
 * @see user.sql
 */
class Model_User extends FaZend_User
implements Zend_Acl_Role_Interface
{
    /**
     * Returns the string identifier of the Role
     *
     * @return string
     * @see Zend_Acl_Role_Interface
     */
    public function getRoleId()
    {
        return strval($this);
    }
    
    /**
     * Get current user
     *
     * @return Model_User
     * @see FaZend_User::getCurrentUser()
     **/
    public static function me() 
    {
        return self::getCurrentUser();
    }

    /**
     * Create new user by email or login
     *
     * @param string Email
     * @param string Login
     * @param string Password
     * @return Model_User
     */
    public static function create($email, $login, $password = null)
    {
        if (empty($login) && empty($email)) {
            FaZend_Exception::raise(
                'FaZend_Validator_Exception',
                'One of the identification fields(email, login) must be not empty'
            );
        }

        if (!empty($email)) {
            validate()
                ->emailAddress(
                    $email,
                    array(),
                    "Invalid email format: '{$email}'"
                )
                ->false(
                    self::isEmailBusy($email),
                    "User with this email '{$email}' is already registered"
                );
        }

        if (!empty($login)) {
            validate()
                ->false(
                self::isLoginBusy($login),
                "User with this login '{$login}' is already registered"
            );
        }

        if ($password === null) {
            $password = self::getStrongPassword($email);
        }
        $user = new self();
        $user->login = $login;
        $user->email = $email;
        $user->authToken = self::getPasswordHash($password, $email);
        $user->save();

        logg("New user created with email '%s' and login '%s'", $user->email, $user->login);

        return $user;
    }

    /**
     * Salt password and calculate sha256 hash
     *
     * @param string Password
     * @param string Salt
     * @return string 64 byte hash
     * @see QOS3
     */
    public static function getPasswordHash($password, $salt)
    {
        $passwordParts = str_split($password, (strlen($password) / 2) + 1);
        $hash = hash('sha256', $passwordParts[0] . $salt . $passwordParts[1]);
        return $hash;
    }
    
    /**
     * This email is already busy?
     *
     * @param string Email
     * @return boolean
     */
    public static function isEmailBusy($email) 
    {
        return (bool)self::retrieve()
            ->setSilenceIfEmpty()
            ->where('email = ?', $email)
            ->fetchRow();
    }

    /**
     * This login is already busy?
     *
     * @param string Email
     * @return boolean
     */
    public static function isLoginBusy($login)
    {
        return (bool)self::retrieve()
            ->setSilenceIfEmpty()
            ->where('login = ?', $login)
            ->fetchRow();
    }

    /**
     * Create strong password
     *
     * @param string Email
     * @return string
     * @see create()
     */
    public static function getStrongPassword($email)
    {
        return substr(md5(rand() . $email), 8);
    }

    /**
     * Get user by login
     *
     * @param string Email of the user
     * @return FaZend_User
     */
    public static function findByLogin($login)
    {
        return self::retrieve()
            ->where('login = ?', $login)
            ->setRowClass(self::$_rowClass)
            ->fetchRow();
    }
    
}

