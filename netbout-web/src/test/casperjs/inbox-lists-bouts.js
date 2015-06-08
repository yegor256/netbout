/*globals casper:false */
casper.test.begin(
    'inbox can list bouts',
    function (test) {
        casper.start().then(
            function() {
                this.open(
                    casper.cli.get('home'),
                    {
                        method: 'GET',
                        headers: {
                            'Accept': 'text/html'
                        }
                    }
                ).then(
                    function() {
                        test.assertHttpStatus(200, 'home page');
                        test.assertExists('li.bout');
                    }
                )
            }
        );
        casper.run(
            function () {
                test.done();
            }
        );
    }
);
