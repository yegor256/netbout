/*globals require:false, test:false */
casper.test.begin(
    'inbox can list bouts',
    function (test) {
        casper.start(
            'http://localhost:${tomcat.port}/',
            function () {
                test.assertHttpStatus(200);
                test.assertExists('li.bout')
            }
        );
        casper.run(
            function () {
                test.done();
            }
        );
    }
);
