// Qbaka
window.qbaka || (function(a,c){a.__qbaka_eh=a.onerror;a.__qbaka_reports=[];a.onerror=function(){a.__qbaka_reports.push(arguments);
if(a.__qbaka_eh)try{a.__qbaka_eh.apply(a,arguments)}catch(b){}};
a.onerror.qbaka=1;a.qbaka={report:function(){a.__qbaka_reports.push([arguments, new Error()]);},customParams:{},set:function(a,b){qbaka.customParams[a]=b},exec:function(a){try{a()}catch(b){qbaka.reportException(b)}},reportException:function(){}};
var b=c.createElement("script"),e=c.getElementsByTagName("script")[0],d=function(){e.parentNode.insertBefore(b,e)};
b.type="text/javascript";b.async=!0;b.src="//cdn.qbaka.net/reporting.js";"[object Opera]"==a.opera?c.addEventListener("DOMContentLoaded",d):d();
qbaka.key="00dfa61ef8b0f3f6fe1e97790d64ef16"})(window,document);
qbaka.options={autoStacktrace:1,trackEvents:1};

// Pingdom RUM
var _prum = [['id', '5289f7aaabe53def32000000'],
['mark', 'firstbyte', (new Date()).getTime()]];
(function() {
  var s = document.getElementsByTagName('script')[0],
    p = document.createElement('script');
  p.async = 'async';
  p.src = '//rum-static.pingdom.net/prum.min.js';
  s.parentNode.insertBefore(p, s);
})();

// Google Analytics
var _gaq = _gaq || [];
_gaq.push(['_setAccount', 'UA-1963507-24']);
_gaq.push(['_trackPageview']);
(function() {
  var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
  ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
  var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
})();
