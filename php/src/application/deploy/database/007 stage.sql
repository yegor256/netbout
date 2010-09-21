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
-- One Stage
--
-- @category Data
-- @package MySQL
-- @see Model_Stage
-- @version $Id$
--

CREATE TABLE IF NOT EXISTS `stage`
(
    -- MEDIUMINT is used because we will be able to have 16.777.215 stages
    -- and it's enough, as we think now
    `id` MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT "Unique ID of the stage",
    `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT "Date when the row was created",

    `name` TEXT COMMENT "Mandatory title of the stage",
    `description` TEXT COMMENT "Mandatory description of the stage",

    -- Stages are identified by ID
    PRIMARY KEY(`id`)
)
AUTO_INCREMENT=1
DEFAULT CHARSET=utf8
ENGINE=InnoDB
COMMENT="List of Stage-s";
