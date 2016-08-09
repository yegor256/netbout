/*globals casper:false */
casper.test.begin(
    'inbox can list bouts',
    function (test) {
        casper.start().then(
            function() {
                this.open(
                    casper.cli.get('home') + "/search?q=invalidsearch",
                    {
                        method: 'GET',
                        headers: {
                            'Accept': 'text/html'
                        }
                    }
                ).then(
                    function() {
                        test.assertHttpStatus(200, 'search results page status is not 200!');
                        test.assert(
                            document.querySelector("#noresmsg").textContent ===
                            'No bouts found for query "invalidsearch"'
                        )
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
