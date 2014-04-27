var casper = require('casper').create();
var home = casper.cli.get "home";
casper.start(
    home + '/xml/error.xml',
    function() {
        test.assertExists('h1');
    }
);
