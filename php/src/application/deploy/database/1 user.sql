--
-- Users table
--
-- This table is called USER because of integration with {@link FaZend_User}
-- component, which is tailored for USER table.
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
    `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT "Date of user registration",

    `login` VARCHAR(254) COMMENT "Unique ID of the user, used for login",

    -- We using SHA256 hash with salt, so it's always 64 chars,
    -- @see Model_User::getPasswordHash()
    `password` CHAR(64) NOT NULL COMMENT "Password hash",

    -- VARCHAR(254) is used
    -- @see http://en.wikipedia.org/wiki/E-mail_address
    -- @see http://tools.ietf.org/html/rfc5322
    `email` VARCHAR(254) COMMENT "Optional email address of the user",

    `bio` TEXT COMMENT "optional text about the user, visible to his contacts",
    `signature` TEXT COMMENT "to be used in emails sent from this user to others",

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
