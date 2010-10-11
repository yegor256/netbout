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
-- One NetBout tag
--
-- @category Data
-- @package MySQL
-- @see Model_Tag
-- @see Model_User
-- @version $Id$
--

CREATE TABLE IF NOT EXISTS `tag`
(
    -- MEDIUMINT is used because we will be able to have 16.777.215 tags
    -- and it's enough, as we think now
    `id` MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT "Unique ID of the tag",
    `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT "Date when the row was created",

    -- MEDIUMINT UNSIGNED is used - @see user.id column
    `user` MEDIUMINT UNSIGNED NOT NULL COMMENT "Who created this tag",

    -- MEDIUMINT UNSIGNED is used - @see tag.id column, NULL here mean tag has no parent
    `parent` MEDIUMINT UNSIGNED COMMENT "Parent tag of this one",

    -- Tag name
    `name` VARCHAR(50) NOT NULL COMMENT "Tag name",

    -- Tags are identified by ID
    PRIMARY KEY(`id`),

    -- Link to the user
    FOREIGN KEY(`user`) REFERENCES `user`(`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    -- Each user can has own set of unique tag names
    UNIQUE(`user`, `name`)
)
AUTO_INCREMENT=1
DEFAULT CHARSET=utf8 
ENGINE=InnoDB
COMMENT="List of users tags";
