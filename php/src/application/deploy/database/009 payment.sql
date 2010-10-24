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
-- When user should be charged for some stage we will insert to this table record with
-- 1. amount = negative amount of ActorHelper cost
-- 2. stage for which payment is for
-- 3. debit = user.id who rent specified stage and should be charged
-- 4. credit = user.id ActorHelper used for the stage
--
-- When an user will pay for rented ActorHelper we will insert similar row with positive amount.
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
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT
        COMMENT "Unique ID of the payment",
    `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT "Date when the row was created",

    -- MEDIUMINT UNSIGNED is used - @see user.id column
    `debit` MEDIUMINT UNSIGNED NOT NULL
        COMMENT "Who should be debited",

    -- MEDIUMINT UNSIGNED is used - @see user.id column
    `credit` MEDIUMINT UNSIGNED NOT NULL
        COMMENT "Who will receive funds",

    -- Paid amount, DECIMAL(10,2) is used to avoid rounding errors
    -- Can be positive or negative(mean we create request for payment)
    `amount` DECIMAL(10,2) NOT NULL
        COMMENT "Paid amount",

    -- MEDIUMINT UNSIGNED is used - @see stage.id column
    `stage` MEDIUMINT UNSIGNED NOT NULL
        COMMENT "What this payment is for",

    `description` TEXT NOT NULL
        COMMENT "Detailed explanation of the transaction",

    -- Links are identified by ID
    PRIMARY KEY(`id`),

    -- Link to the credited user
    FOREIGN KEY(`credit`) REFERENCES `user`(`id`)
        ON UPDATE CASCADE,

    -- Link to the debited user
    FOREIGN KEY(`debit`) REFERENCES `user`(`id`)
        ON UPDATE CASCADE,

    -- Link to the stage
    FOREIGN KEY(`stage`) REFERENCES `stage`(`id`)
        ON UPDATE CASCADE
)
AUTO_INCREMENT=1
DEFAULT CHARSET=utf8
ENGINE=InnoDB
COMMENT="Online payment made by the user to the SUD through ActorBank";
