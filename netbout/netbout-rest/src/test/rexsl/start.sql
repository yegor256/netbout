#
# Copyright (c) 2009-2011, netBout.com
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are PROHIBITED without prior written permission from
# the author. This product may NOT be used anywhere and on any computer
# except the server platform of netBout Inc. located at www.netbout.com.
# Federal copyright law prohibits unauthorized reproduction by any means
# and imposes fines up to $25,000 for violation. If you received
# this code occasionally and without intent to use it, please report this
# incident to the author by email.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
# "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
# LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
# A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
# OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
# SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
# LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
# DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
# ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
# OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# @author Yegor Bugayenko (yegor@netbout.com)
# @version $Id$
#

# clean the datbase before start
DELETE FROM namespace WHERE identity != "urn:void:";
DELETE FROM helper;
DELETE FROM alias;
DELETE FROM seen;
DELETE FROM message;
DELETE FROM participant;
DELETE FROM bout;
DELETE FROM identity WHERE name != "urn:void:";

# bumper helper and identity
INSERT INTO identity (name, photo, date) VALUES (
    'urn:test:bumper',
    'http://img.netbout.com/unknown.png',
    '2008-08-30'
);
INSERT INTO helper (identity, url, date) VALUES (
    'urn:test:bumper',
    'file:com.netbout.rest.bumper',
    '2009-09-13'
);

# a few pre-defined identities
INSERT INTO identity (name, photo, date) VALUES (
    'urn:facebook:4466',
    'http://www.topnews.in/light/files/John-Turturro.jpg',
    '2010-11-22'
);
INSERT INTO identity (name, photo, date) VALUES (
    'urn:test:cindy',
    'http://img.netbout.com/unknown.png',
    '2010-11-21'
);
INSERT INTO alias (identity, name, date) VALUES (
    'urn:facebook:4466',
    'John Turturro',
    '2010-11-23'
);

# pre-existing bout
INSERT INTO bout (number, title, date) VALUES (
    555,
    'ура!',
    '2010-11-13'
);
INSERT INTO participant (bout, identity, confirmed, date) VALUES (
    555,
    'urn:facebook:4466',
    1,
    '2010-11-22'
);
INSERT INTO participant (bout, identity, confirmed, date) VALUES (
    555, 'urn:test:cindy', 1, '2010-11-22'
);
INSERT INTO message (bout, date, author, text) VALUES (
    555, '2011-11-15 03:18:34', 'urn:facebook:4466', 'first message'
);
INSERT INTO message (bout, date, author, text) VALUES (
    555, '2011-11-15 04:28:22', 'urn:test:cindy', 'second message'
);
INSERT INTO message (bout, date, author, text) VALUES (
    555, '2011-11-15 05:23:11', 'urn:test:cindy', 'у!'
);
INSERT INTO namespace (name, identity, template, date) VALUES (
    'foo', 'urn:facebook:4466', 'http://localhost/foo', '2010-11-22'
);

# identity with a long bout
INSERT INTO identity (name, photo, date) VALUES (
    'urn:test:leon',
    'http://www.roopevintage.com/blog/wp-content/uploads/2011/04/0124_leon.jpg',
    '2006-11-22'
);
INSERT INTO bout (number, title, date) VALUES (900, 'test', '2007-05-05');
INSERT INTO participant (bout, identity, confirmed, date) VALUES (900, 'urn:test:leon', 1, '2007-05-05');
INSERT INTO bout (number, title, date) VALUES (901, 'test', '2010-01-13');
INSERT INTO participant (bout, identity, confirmed, date) VALUES (901, 'urn:test:leon', 1, '2010-01-13');
INSERT INTO message (bout, date, author, text) VALUES (901, '2011-01-25', 'urn:test:leon', 'hi!');
INSERT INTO bout (number, title, date) VALUES (902, 'test', '2010-01-14');
INSERT INTO participant (bout, identity, confirmed, date) VALUES (902, 'urn:test:leon', 1, '2010-01-14');
INSERT INTO bout (number, title, date) VALUES (903, 'test', '2010-01-15');
INSERT INTO participant (bout, identity, confirmed, date) VALUES (903, 'urn:test:leon', 1, '2010-01-15');
INSERT INTO bout (number, title, date) VALUES (904, 'test', '2010-01-16');
INSERT INTO participant (bout, identity, confirmed, date) VALUES (904, 'urn:test:leon', 1, '2010-01-16');
INSERT INTO bout (number, title, date) VALUES (905, 'test', '2010-01-17');
INSERT INTO participant (bout, identity, confirmed, date) VALUES (905, 'urn:test:leon', 1, '2010-01-17');
INSERT INTO bout (number, title, date) VALUES (906, 'test', '2010-01-18');
INSERT INTO participant (bout, identity, confirmed, date) VALUES (906, 'urn:test:leon', 1, '2010-01-18');
INSERT INTO bout (number, title, date) VALUES (907, 'test', '2010-01-19');
INSERT INTO participant (bout, identity, confirmed, date) VALUES (907, 'urn:test:leon', 1, '2010-01-19');
INSERT INTO bout (number, title, date) VALUES (908, 'test', '2010-01-20');
INSERT INTO participant (bout, identity, confirmed, date) VALUES (908, 'urn:test:leon', 1, '2010-01-20');
