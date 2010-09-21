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
-- One payment
--
-- @category Data
-- @package MySQL
-- @see Model_Payment
-- @see Model_User
-- @version $Id$
--

CREATE TABLE IF NOT EXISTS `payment`
(
    -- INT UNSIGNED is used, because we have MEDIUMINT UNSIGNED in netBout.id column
    -- and here will be stored some records per each one NETBOUT
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT "Unique ID of the payment",
    `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT "Date when the row was created",

    -- MEDIUMINT UNSIGNED is used - @see user.id column
    `user` MEDIUMINT UNSIGNED NOT NULL COMMENT "Who made this payment",

    -- Paid amount, DECIMAL(10,2) is used to avoid rounding errors
    `amount` DECIMAL(10,2) NOT NULL COMMENT "Paid amount",

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
COMMENT="Online payment made by the user to the SUD through ActorBank";
