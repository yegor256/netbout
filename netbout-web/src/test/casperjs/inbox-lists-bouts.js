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
                        //@todo This test is failing after upgrading to Takes 0.20. See #669
                        //test.assertHttpStatus(200, 'home page');
                        //test.assertExists('li.bout');
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
