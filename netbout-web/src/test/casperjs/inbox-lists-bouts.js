/*globals casper:false */
casper.test.begin(
    'inbox can list bouts',
    function (test) {
        casper.start(
            casper.cli.get('home'),
            function () {
                test.assertHttpStatus(200);
                test.assertExists('li.bout');
            }
        );
        casper.run(
            function () {
                test.done();
            }
        );
    }
);
