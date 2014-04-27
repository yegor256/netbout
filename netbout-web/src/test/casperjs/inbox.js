/*globals require:false, test:false */
var casper = require('casper').create();
var home = casper.cli.get("home");
casper.start(
    home + '/xml/inbox.xml',
    function() {
        test.assertExists('h1');
    }
);
