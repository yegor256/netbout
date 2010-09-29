--
-- netbout.com
--
-- Redistribution and use in source and binary forms, with or 
-- without modification, are PROHIBITED without prior written 
-- permission from the author. This product may NOT be used 
-- anywhere and on any computer except the server platform of 
-- netbout.com. located at www.netbout.com. If you received this 
-- code occasionally and without intent to use it, please report 
-- this incident to the author by email: privacy@netbout.com
-- 
-- Users table
--
-- This table is called USER because of integration with {@link FaZend_User}
-- component, which is tailored for USER table.
--
-- We can have two types of users
-- 1. user who was registered (has record with login, and optionally email)
--
-- 2. user who was not registered (was invited - has record with email and login IS NULL)
-- When we sent invitation to not existing user (no record in user table for his email/login),
-- we create new record with an email and random password, and send an invitation to him.
-- When user accept the invitation we set a cookie with id and password hash. Thanks for
-- that we can identify him.
--
-- @category Data
-- @package MySQL
-- @see ActorUser
-- @see Model_User
-- @version $Id$
--

CREATE TABLE IF NOT EXISTS `user`
(
    -- MEDIUMINT is used because we will be able to have 16.777.215 users
    -- and it's enough, as we think now
    `id` MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT "Unique ID of the user",
    `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT "Date when the row was created",

    `login` VARCHAR(254) COMMENT "Unique ID of the user, used for login",

    -- We using SHA256 hash with salt, so it's always 64 chars,
    -- @see Model_User::getPasswordHash()
    `password` CHAR(64) NOT NULL COMMENT "Password hash",

    -- VARCHAR(254) is used
    -- @see http://en.wikipedia.org/wiki/E-mail_address
    -- @see http://tools.ietf.org/html/rfc5322
    `email` VARCHAR(254) COMMENT "Optional email address of the user",

    `avatar` VARCHAR(50) COMMENT "Optional avatar src of the user",
    `bio` TEXT COMMENT "Optional text about the user, visible to his contacts",
    `deliveryMethod` SET('email', 'sms') COMMENT "How we should deliver data updates to this user",
    `signature` TEXT COMMENT "To be used in emails sent from this user to others",

    -- Users are identified by ID
    PRIMARY KEY(`id`),

    -- We use INDEX instead UNIQUE, because login or email can be NULL,
    -- so we can have many NULL-s for one column. We will check login and email
    -- uniqueness in PHP part
    INDEX(`login`),
    INDEX(`email`)

)
AUTO_INCREMENT=1
DEFAULT CHARSET=utf8 
ENGINE=InnoDB
COMMENT="List of users";
