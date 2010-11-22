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
-- One message in a NetBout
--
-- We will NOT use here FULLTEXT index because we have really personal search
-- engine. Each user is able to search ONLY in NetBout-s message where
-- he is participant. Because of that we must first found messages for which
-- user has access. We use for that participant table to get netbout
-- id-s where we can search.
--
-- The best possibility is two column index on (netBout and text)
-- because we must first search by netBout and after in found messages, but
-- MySQL doesn't support this kind of index - we can NOT include int field IN
-- FULLTEXT index and additionally we must migrate to MyISAM. FULLTEXT index
-- also affect insert speed and when we use it for search other indexes are ignored.
-- In our case when we intensively use other integer index, this behavior can
-- cause much performance degradation, so we must use other solution described below.
--
-- With user access control we have great selectivity, because our DB can have
-- 10mln messages but user participated in NetBout-s which summary have
-- 10000 messages (100 NetBout-s 100 messages in each), so we will limit our searches
-- to about 0.1% of our message table size, what is really great.
--
-- Finally we will use fast integer index(netBout) + LIKE on text column.
-- Thanks for that we can handle about 10000 messages per user, if we will need
-- more we must migrate to more efficient search engines like SPHINX, but for current
-- requirements this solution has really good performance.
--
-- @category Data
-- @package MySQL
-- @see Model_NetBout_Message
-- @version $Id$
--

CREATE TABLE IF NOT EXISTS `message`
(
    -- INT UNSIGNED is used, because we have MEDIUMINT UNSIGNED in netBout.id column
    -- and here will be stored some records per each one NETBOUT
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT
        COMMENT "Unique ID of the message",
    `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT "Date when the row was created",

    -- MEDIUMINT UNSIGNED is used - @see netBout.id column
    `netBout` MEDIUMINT UNSIGNED NOT NULL
        COMMENT "Unique ID of the NetBout",

    -- MEDIUMINT UNSIGNED is used - @see user.id column
    `user` MEDIUMINT UNSIGNED DEFAULT NULL
        COMMENT "Author of the message",

    -- MEDIUMINT UNSIGNED is used - @see stage.id column
    `stage` MEDIUMINT UNSIGNED DEFAULT NULL
        COMMENT "Stage which added this message",

    -- if user is NOT NULL we have here BoutText.
    -- if stage is NOT NULL we have here XML, which is understandable only by
    -- the helper so render process will be handled by it.
    `text` LONGTEXT NOT NULL
        COMMENT "Message text or XML content parseable by Helper only",

    -- Messages are identified by ID
    PRIMARY KEY(`id`),

    -- Author of the message
    FOREIGN KEY(`user`) REFERENCES `user`(`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    -- Stage which added this message
    FOREIGN KEY(`stage`) REFERENCES `stage`(`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    -- Link to the NetBout
    FOREIGN KEY(`netBout`) REFERENCES `netBout`(`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE
)
AUTO_INCREMENT=1
DEFAULT CHARSET=utf8
ENGINE=InnoDB
COMMENT="List of messages for NetBout-s";
