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
-- One ActorHelper
--
-- @category Data
-- @package MySQL
-- @see Model_Helper
-- @see ActorHelper
-- @version $Id$
--

CREATE TABLE IF NOT EXISTS `helper`
(
    -- MEDIUMINT is used because we will be able to have 16.777.215 helpers
    -- and it's enough, as we think now
    `id` MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT "Unique ID of the helper",
    `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT "Date when the row was created",

    `name` TEXT COMMENT "That identifies the helper uniquely in the SUD",
    `key` TEXT COMMENT "Some secret code for access through RestApi",

    -- DECIMAL(10,2) is used to avoid rounding errors
    `price` DECIMAL(10,2) COMMENT "Fixed monetary value, to be paid by ActorUser for every stage rented by the helper (can be zero)",

    -- Helpers are identified by ID
    PRIMARY KEY(`id`)
)
AUTO_INCREMENT=1
DEFAULT CHARSET=utf8
ENGINE=InnoDB
COMMENT="List of ActorHelper-s";
