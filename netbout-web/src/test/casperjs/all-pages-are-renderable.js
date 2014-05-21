/*globals require:false, test:false */
[
    '/xml/bout.xml',
    '/xml/error.xml',
    '/xml/inbox.xml',
    '/xml/empty-inbox.xml',
    '/xml/login.xml',
    '/css/style.css',
    '/robots.txt',
    '/js/bout.js',
    '/m/200',
    '/',
].forEach(
    function (page) {
        casper.test.begin(
            page + ' page can be rendered',
            function (test) {
                casper.start(
                    'http://localhost:${tomcat.port}' + page,
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
