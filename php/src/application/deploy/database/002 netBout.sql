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
-- One NetBout
--
-- @category Data
-- @package MySQL
-- @see NetBout
-- @see Model_NetBout
-- @version $Id$
--

CREATE TABLE IF NOT EXISTS `netBout`
(
    -- MEDIUMINT is used because we will be able to have 16.777.215 NetBout-s
    -- and it's enough, as we think now
    `id` MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT
        COMMENT "Unique ID of the NetBout",
    `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT "Date when the row was created",

    -- MEDIUMINT UNSIGNED is used - @see user.id column
    `user` MEDIUMINT UNSIGNED NOT NULL
        COMMENT "Creator of this NetBout",

    `subject` TEXT NOT NULL
        COMMENT "Mandatory title of the bout, visible to all participants",

    -- NetBout-s are identified by ID
    PRIMARY KEY(`id`),

    -- Author of the NetBout
    FOREIGN KEY(`user`) REFERENCES `user`(`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE
)
AUTO_INCREMENT=1
DEFAULT CHARSET=utf8
ENGINE=InnoDB
COMMENT="List of NetBout-s";
