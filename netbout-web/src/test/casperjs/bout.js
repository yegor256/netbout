var casper = require('casper').create();
var home = casper.cli.get "home";
casper.start(
    home + '/xml/bout.xml',
    function() {
        test.assertExists('h1');
    }
);
