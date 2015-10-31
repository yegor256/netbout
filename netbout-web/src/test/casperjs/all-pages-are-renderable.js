/*globals casper:false */
[
    '/xsl/bout.xsl',
    '/xsl/login.xsl',
    '/lang/en.xml',
    '/css/style.css',
    '/robots.txt',
    '/js/bout.js',
    '/'
].forEach(
    function (page) {
        casper.test.begin(
            page + ' page can be rendered',
            function (test) {
                casper.start(
                    casper.cli.get('home') + page,
                    function () {
                        test.assertHttpStatus(200, page);
                    }
                );
                casper.run(
                    function () {
                        test.done();
                    }
                );
            }
        );
    }
);

[
    '/page-not-found',
    '/b/not-found',
    '/b/55667788',
    '/xml/absent-always.xml',
    '/xsl/absent-stylesheet.xsl',
    '/css/bout.css',
    '/js/absent-javascript.js'
].forEach(
    function (page) {
        casper.test.begin(
            page + ' page should be NOT-FOUND',
            function (test) {
                casper.start(
                    casper.cli.get('home') + page,
                    function () {
                        test.assertHttpStatus(200, page);
                        casper.waitForText('page not found');
                    }
                );
                casper.run(
                    function () {
                        test.done();
                    }
                );
            }
        );
    }
);
