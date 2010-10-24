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
-- Interlink between TAG and NETBOUT
--
-- @category Data
-- @package MySQL
-- @see NetBout
-- @see Model_NetBout_Tag
-- @see Model_NetBout
-- @see Model_Tag
-- @version $Id$
--

CREATE TABLE IF NOT EXISTS `netBoutTag`
(
    -- MEDIUMINT is used because we will be able to have 16.777.215 tag links
    -- and it's enough, as we think now
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT
        COMMENT "Unique ID of the link",
    `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT "Date when the row was created",

    -- MEDIUMINT UNSIGNED is used - @see netBout.id column
    `netBout` MEDIUMINT UNSIGNED NOT NULL
        COMMENT "Unique ID of the NetBout",

    -- MEDIUMINT UNSIGNED is used - @see tag.id column
    `tag` MEDIUMINT UNSIGNED NOT NULL
        COMMENT "Unique ID of the Tag",

    -- Links are identified by ID
    PRIMARY KEY(`id`),

    -- Link to the NetBout
    FOREIGN KEY(`netBout`) REFERENCES `netBout`(`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    -- Link to the tag
    FOREIGN KEY(`tag`) REFERENCES `tag`(`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    -- One tag can be attached only once to some NetBout
    UNIQUE(`netBout`, `tag`)
)
AUTO_INCREMENT=1
DEFAULT CHARSET=utf8
ENGINE=InnoDB
COMMENT="Interlink between TAG and NETBOUT";
