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

/*globals $:false, document:false */

$(document).ready(
    function () {
        "use strict";
        if ($('#rename')[0]) {
            $('h1 span.title')
                .blur(
                    function () {
                        var $input = $('#rename').find("input[name='title']"),
                            previous = $input.val(),
                            entered = $(this).text();
                        if (entered !== previous) {
                            $input.val(entered);
                            $('#rename').submit();
                        }
                    }
                )
                .keydown(
                    function (event) {
                        if (event.keyCode === 13) {
                            $(this).blur();
                        }
                    }
                );
        }
        $(window).scroll(
            function () {
                var $box = $('#messages');
                if ($(window).scrollTop() >= $(document).height() - $(window).height() - 50) {
                    $.ajax(
                        {
                            url: $box.attr('data-tail-href')
                                + '?number=' + $box.attr('data-tail-number'),
                            cache: false,
                            type: 'json',
                            success: function (text) {
                                $box.html($box.html() + text);
                            }
                        }
                    );
                }
            }
        );
    }
);

