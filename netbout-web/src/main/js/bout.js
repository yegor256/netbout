/**
 * Copyright (c) 2009-2015, netbout.com
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

/*globals $:false, document:false, window:false */

function escapeHTML(txt) {
  "use strict";
  return txt.replace(/&/g, '&amp;')
    .replace(/"/g, '&quot;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');
}

$.fn.preventDoubleSubmission = function() {
  $(this).on('submit',function(e){
    var $form = $(this);
    if ($form.data('submitted') === true) {
      e.preventDefault();
    } else {
      $form.data('submitted', true);
    }
  });
  return this;
};

function readMore(retFunction) {
  var $box = $('#messages'), $tail = $('#tail'), more = $box.attr('data-more');
  $box.removeAttr('data-more', '');
  $tail.show();
  $.ajax(
    {
      url: more,
      cache: false,
      dataType: 'xml',
      method: 'GET',
      success: function (data) {
        var appendix = '',
            $data = $(data),
            xml = $data.find('message'),
            html = $data.find('#messages');
        more = '';
        function msgXmlToHtml($msg) {
          return [
            '<div class="message" id="msg',
            $msg.find('number').text(),
            '"><div class="left"><img class="photo" src="',
            $msg.find('link[rel="photo"]').attr('href'),
            '"/>',
            '</div><div class="right"><div class="meta"><strong>',
            escapeHTML($msg.find('author').text()),
            '</strong> said <a href="',
            location.origin,
            location.pathname,
            '#msg',
            $msg.find('number').text(),
            '">',
            escapeHTML($msg.find('timeago').text()),
            '</a> </div><div class="text">',
            $msg.find('html').text(),
            '</div></div></div>'
          ].join('');
        }
        function msgsXmlToHtml() {
          xml.each(
              function (idx, msg) {
                var $msg = $(msg);
                appendix += msgXmlToHtml($msg);
                more = $msg.find('link[rel="more"]').attr('href');
              }
          );
          $tail.removeAttr('id');
          $tail.html(appendix + '<div id="tail"/>');
          $box.attr('data-more', more);
        }
        function msgsHtmlToHtml() {
          html.find('.message').each(
              function (idx, line) {
                var $msg = $(line),
                    msg = $('<div class="message"></div>');
                msg.attr('id', $msg.attr('id'));
                msg.append($msg.find('.left'));
                msg.append($msg.find('.right'));
                appendix += [
                  $('<div></div>').append(msg).html()
                ].join('');
              }
          );
          $tail.removeAttr('id');
          $tail.html(appendix + '<div id="tail"/>');
          $box.attr('data-more', html.attr('data-more'));
        }
        if (xml.length > 0) {
          msgsXmlToHtml();
          if (typeof retFunction !== 'undefined') {
            retFunction(xml.length);
          }
        }else if (html.length > 0) {
          msgsHtmlToHtml();
          if (typeof retFunction !== 'undefined') {
            retFunction(html.length);
          }
        } else {
          retFunction(0);
        }
      },
      error: function () {
        $tail.html('Oops, an error :( Please, try to reload the page');
      }
    }
  );
}

function scrollOrLoad(amountRecords) {
  if (location.hash.trim() && $(location.hash.trim()).length >= 1 &&
      ($(location.hash.trim()).offset().top <
      $(document).height() - $(window).height() - 600)) {
    $(window).scrollTop($(location.hash.trim()).offset().top);
  } else if (location.hash.trim() && amountRecords > 0) {
    readMore(scrollOrLoad);
  } else {
    $(window).scrollTop($(location.hash.trim()).offset().top);
  }
}

$(document).ready(
  function () {
    "use strict";
    var $rename = $('#rename');
    if ($rename[0]) {
      $('h1 span.title')
        .blur(function () {
          var $input = $rename.find("input[name='title']"),
            previous = $input.val(),
            entered = $(this).text();
          if (entered !== previous) {
            $input.val(entered);
            $rename.submit();
          }
        })
        .keydown(function (event) {
          if (event.keyCode === 13) {
            $(this).blur();
          }
        });
    }
    $(window).scroll(
      function () {
        var $box = $('#messages'), $tail = $('#tail'), more = $box.attr('data-more');
        if ($(window).scrollTop() >= $(document).height() - $(window).height() - 600 && more) {
          readMore();
        }
      }
    );
    $('form').preventDoubleSubmission();
    scrollOrLoad(1);
  }
);

