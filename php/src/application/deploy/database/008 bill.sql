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
-- One bill
--
-- @category Data
-- @package MySQL
-- @see Model_Bill
-- @see Model_User
-- @version $Id$
--

CREATE TABLE IF NOT EXISTS `bill`
(
    -- MEDIUMINT is used because we will be able to have 16.777.215 NetBout-s
    -- and it's enough, as we think now
    `id` MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT "Unique ID of the bill",
    `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT "Date when the row was created",

    -- MEDIUMINT UNSIGNED is used - @see user.id column
    `user` MEDIUMINT UNSIGNED NOT NULL COMMENT "Who should pay this bill",

    -- Paid amount, DECIMAL(10,2) is used to avoid rounding errors
    `amount` DECIMAL(10,2) NOT NULL COMMENT "Amount to pay",

    -- Links are identified by ID
    PRIMARY KEY(`id`),

    -- Link to the user
    FOREIGN KEY(`user`) REFERENCES `user`(`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE
)
AUTO_INCREMENT=1
DEFAULT CHARSET=utf8
ENGINE=InnoDB
COMMENT="Monetary value to be paid to an owner of ActorHelper";
