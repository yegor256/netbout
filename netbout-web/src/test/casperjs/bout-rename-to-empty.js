/*globals casper:false */
var boutURL = "";
var boutName = "";

casper.test.begin(
    'Bout attempt to rename to empty should replaced by former name', 7,
    function (test) {
        casper.start();
        casper.then(
            function() {
                this.open(
                    casper.cli.get('home') + '/start',
                    {
                        method: 'GET',
                        headers: {
                            'Accept': 'text/html'
                        }
                    }
                ).then(
                    function(response) {
                        boutURL = response.url;
                        this.open(
                            boutURL,
                            {
                              method: 'GET',
                              headers: {
                                'Accept': 'text/html'
                              }
                            }
                        );
                    }
                )
            }
        );
        casper.then(
            function(response) {
                test.assertHttpStatus(200,'Bout created.');
                test.assertUrlMatch(/^http.*:\/\/.*\/b\//);
                test.assertExists('h1 span.title');
                boutName = this.fetchText('h1 span.title');
                this.evaluate(
                    function() {
                        document.querySelector('h1 span.title')
                            .innerHTML = '';
                    }
                );
                this.sendKeys('h1 span.title', '');
            }
        );
        casper.then(
            function() {
                test.assertHttpStatus(
                    200,
                    'Still alive after renaming to empty'
                );
                test.assertEquals(
                    this.fetchText('h1 span.title'),
                    boutName,
                    "Empty name replaced by former name"
                );
                this.evaluate(
                    function() {
                        document.querySelector('h1 span.title')
                            .innerHTML = '';
                    }
                );
                this.sendKeys('h1 span.title', ' ');
            }
        );
        casper.then(
            function() {
                test.assertHttpStatus(
                    200,
                    'Still alive after renaming to only spaces'
                );
                test.assertEquals(
                    this.fetchText('h1 span.title'),
                    boutName,
                    "Only spaces name replaced by former name"
                );
            }
        );
        casper.run(
            function () {
                test.done();
            }
        );
    }
);
