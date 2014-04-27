var casper = require('casper').create();
var home = casper.cli.get "home";
casper.start(
    home + '/xml/login.xml',
    function() {
        test.assertExists('h1');
    }
);
