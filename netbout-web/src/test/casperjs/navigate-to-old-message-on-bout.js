/*globals casper:false */
var boutURL = "";
var msgId = "";
var msgURL = "";
var repeat = [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20];

casper.test.begin(
    'Index can add a bout.',
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
                test.assertExists('h1.bout span.num');
                this.fillSelectors("form#post-message",{
                    'form#post-message textarea#text' : 'String message'
                });
                this.click('form#post-message input#submit');
            }
        );
        casper.then(
            function(response) {
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
        );
        casper.then(
            function(response) {
                msgId = this.getElementsAttribute(
                    "#messages .message:first-child", 
                    "id"
                );
                msgURL = boutURL + '#' + msgId;
                if (!msgId) {
                    this.die('Message was not inserted.');
                }
                casper.eachThen(repeat,
                    function(count) {
                        this.open(
                            boutURL,
                            {
                              method: 'GET',
                              headers: {
                                'Accept': 'text/html'
                              }
                            }
                        ).then(
                            function (response) {
                                this.fillSelectors("form#post-message",{
                                  'form#post-message textarea#text' : 
                                      'String message -> ' + count.data
                                });
                                this.click('form#post-message input#submit');
                            }
                        );
                    }
                );
            }
        );
        casper.then(
            function(response) {
                this.open(
                    msgURL,
                    {
                      method: 'GET',
                      headers: {
                        'Accept': 'text/html'
                      }
                    }
                );
            }
        );
        casper.then(
            function(response) {
                var divId= '#messages #' + msgId;
                casper.waitWhileSelector(
                    divId,
                    function() {
                      casper.log("Message found.","debug");
                    },
                    function() {
                      this.die('Message not found.');
                    },
                    1000
                );
            }
        );
        casper.then(
            function(response) {
                this.open(
                    msgURL,
                    {
                      method: 'GET',
                      headers: {
                        'Accept': 'text/html'
                      }
                    }
                );
            }
        );
        casper.then(
            function(response) {
                casper.log(response.url,"error");
                var dates = this.getElementsAttribute(
                    "#messages .message a span",
                    "title"
                );
                var lastDate = null;
                for (var i = 0; i < dates.length; i++) {
                    date = Date.parse(dates[i]);
                    if (!lastDate) {
                        lastDate = date;
                    } else {
                        test.assert(
                            (lastDate >= date),
                            "It was loaded an older message before a new one."
                        );
                    }
                    lastDate = date;
                }
            }
        );
        casper.run(
            function () {
                test.done();
            }
        );
    }
);
