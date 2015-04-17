/**
 * Copyright (c) 2009-2014, netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netbout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code accidentally and without intent to use it, please report this
 * incident to the author by email.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

/*globals window:false, qbaka:false */

// Qbaka
if (typeof window.qbaka === 'undefined') {
  (function (a, c) {
    a.__qbaka_eh = a.onerror;
    a.__qbaka_reports = [];
    a.onerror = function () {
      a.__qbaka_reports.push(arguments);
      if (a.__qbaka_eh) {
        try {
          a.__qbaka_eh.apply(a, arguments);
        } catch (ex) {
          // ignore
        }
      }
      a.onerror.qbaka = 1;
      a.qbaka = {
        report: function () {
          a.__qbaka_reports.push([arguments, new Error()]);
        },
        customParams: {},
        set: function (a, b) {
          qbaka.customParams[a] = b;
        },
        exec: function (a) {
          try {
            a();
          } catch (ex) {
            qbaka.reportException(ex);
          }
        },
        reportException: function () {
        }
      };
      var b = c.createElement('script'),
        e = c.getElementsByTagName('script')[0],
        d = function () {
          e.parentNode.insertBefore(b, e);
        };
      b.type = 'text/javascript';
      b.async = !0;
      b.src = '//cdn.qbaka.net/reporting.js';
      if ('[object Opera]' === a.opera) {
        c.addEventListener('DOMContentLoaded', d);
      } else {
        d();
      }
      qbaka.key = '00dfa61ef8b0f3f6fe1e97790d64ef16';
      qbaka.options = {autoStacktrace: 1, trackEvents: 1};
    };
  }(window, document));
}

// Pingdom RUM
var _prum = [
  ['id', '5289f7aaabe53def32000000'],
  ['mark', 'firstbyte', (new Date()).getTime()]
];
(function () {
  var s = document.getElementsByTagName('script')[0], p = document.createElement('script');
  p.async = 'async';
  p.src = '//rum-static.pingdom.net/prum.min.js';
  s.parentNode.insertBefore(p, s);
}());

// Google Analytics
var _gaq = _gaq || [];
_gaq.push(['_setAccount', 'UA-1963507-24']);
_gaq.push(['_trackPageview']);
(function () {
  var ga = document.createElement('script'),
    s = document.getElementsByTagName('script')[0];
  ga.type = 'text/javascript';
  ga.async = true;
  ga.src = ('https:' === document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
  s.parentNode.insertBefore(ga, s);
}());

