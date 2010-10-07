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
-- One stage
--
-- @category Data
-- @package MySQL
-- @see NetBout
-- @see ActorUser
-- @see Model_NetBout_Stage
-- @see Model_NetBout
-- @see Model_Helper
-- @version $Id$
--

CREATE TABLE IF NOT EXISTS `stage`
(
    -- MEDIUMINT is used because we will be able to have 16.777.215 stage links
    -- and it's enough, as we think now
    `id` MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT "Unique ID of the link",
    `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT "Date when the row was created",

    -- MEDIUMINT UNSIGNED is used - @see netBout.id column
    `netBout` MEDIUMINT UNSIGNED NOT NULL COMMENT "Unique ID of the NetBout",

    -- MEDIUMINT UNSIGNED is used - @see helper.id column
    `helper` MEDIUMINT UNSIGNED NOT NULL COMMENT "Helper which will be used in NetBout",

    -- XML, which is understandable only by the helper it will configure Stage
    -- behavior for selected NetBout
    `content` TEXT NOT NULL COMMENT "XML, which is understandable only by the helper",

    -- Links are identified by ID
    PRIMARY KEY(`id`),

    -- Link to the NetBout
    FOREIGN KEY(`netBout`) REFERENCES `netBout`(`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    -- Link to the helper
    FOREIGN KEY(`helper`) REFERENCES `helper`(`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE
)
AUTO_INCREMENT=1
DEFAULT CHARSET=utf8 
ENGINE=InnoDB
COMMENT="List of Stage-s";
