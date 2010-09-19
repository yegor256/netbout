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

    -- We using SHA256 hash with salt, so it's always 64 chars,
    -- @see Model_User::getPasswordHash()
    `password` CHAR(64) NOT NULL COMMENT "Password hash",

    -- VARCHAR(254) is used - see http://en.wikipedia.org/wiki/E-mail_address
    `email` VARCHAR(254) COMMENT "Email address of the user",

    -- Users are identified by ID
    PRIMARY KEY(`id`),

    INDEX(`email`)

) 
AUTO_INCREMENT=1
DEFAULT CHARSET=utf8 
ENGINE=InnoDB
COMMENT="List of users";
