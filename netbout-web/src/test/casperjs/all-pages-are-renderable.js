/*globals casper:false */
[
    '/xml/bout.xml',
    '/xml/error.xml',
    '/xml/inbox.xml',
    '/xml/empty-inbox.xml',
    '/xml/login.xml',
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
                        test.assertHttpStatus(200);
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
                        test.assertHttpStatus(404);
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
