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

/*globals $:false, document:false, window:false */

function escapeHTML(txt) {
  return txt.replace(/&/g, '&amp;')
    .replace(/"/g, '&quot;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');
}

$(document).ready(
  function () {
    "use strict";
    $(window).scroll(
      function () {
        var $box = $('#bouts'), $tail = $('#tail'), more = $box.attr('data-more');
        if ($(window).scrollTop() >= $(document).height() - $(window).height() - 50 && more) {
          $box.removeAttr('data-more', '');
          $tail.show();
          $.ajax(
            {
              url: more,
              cache: false,
              dataType: 'xml',
              method: 'GET',
              success: function (data) {
                var appendix = '<ul class="bouts">', more = '';
                $(data).find('bout').each(
                  function (idx, msg) {
                    var $msg = $(msg);
                    appendix += '<li class="bout" id="bout'
                      + $msg.find('number').text() + '"><h1 class="bout"><span class="num'
                      + ($msg.find('unread').text() === '0' ? '' : ' unread') + '">#'
                      + $msg.find('number').text() + '</span><a class="title" href="'
                      + $msg.find('link[rel="open"]').attr('href') + '">'
                      + escapeHTML($msg.find('title').text()) + '</a></h1><div class="friends">';
                    $msg.find('friend').each(
                      function (idx, friend) {
                        var $friend = $(friend);
                        var shift = 57 * idx;
                        appendix += '<div class="friend" style="left:'
                          + shift + 'px;"><img class="photo" alt="'
                          + escapeHTML($friend.find('alias').text()) +'" src="'
                          + $friend.find('link[rel="photo"]').attr('href') + '"/></div>';
                      }
                    );
                    appendix += '</div></li>';
                    more = $msg.find('link[rel="more"]').attr('href');
                  }
                );
                $tail.removeAttr('id');
                $tail.html(appendix + '</ul><div id="tail"/>');
                $box.attr('data-more', more);
              },
              error: function () {
                $tail.html('Oops, an error :( Please, try to reload the page');
              }
            }
          );
        }
      }
    );
  }
);

