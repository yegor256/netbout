/**
 * Copyright (c) 2009-2011, netBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code occasionally and without intent to use it, please report this
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

String.prototype.escaped = function() {
    return this.replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/'/g, "&apos;")
        .replace(/"/g, "&quot;");
}

/**
 * Pre-configure this page.
 */
var setup = function() {
    var bout = parseInt($('#bout-number').text(), 10);
    $('h1 span.title')
        .blur(
            function() {
                var $input = $("#rename input[name='title']");
                var previous = $input.val();
                var entered = $(this).text();
                if (entered != previous) {
                    $input.val(entered);
                    $("#rename").submit();
                }
            }
        )
        .keydown(
            function() {
                if (arguments[0].keyCode == 13) {
                    $(this).blur();
                }
            }
        );
    $('span.xml-toggle').click(
        function() {
            $(this).parent().parent().find('p.fixed').toggle();
        }
    );
    $('input[name="mask"]').keyup(
        function() {
            var $ul = $('#invite-list');
            $.ajax({
                url: '/f?mask=' + encodeURI($(this).val()) + '&bout=' + bout,
                headers: { 'Accept': 'application/xml' },
                cache: false,
                dataType: 'xml',
                error: function() {
                    $ul.hide();
                    $ul.empty();
                },
                success: function(xml) {
                    $ul.hide();
                    var html = '';
                    $(xml).find('invitee').each(
                        function() {
                            var alias = $(this).find('alias').text();
                            // see http://stackoverflow.com/questions/9834487
                            html += '<li><a href="' + $(this).attr('href').escaped()
                                + '" title="' + alias.escaped() + '">'
                                + (alias.length > 25 ? alias.substr(0, 25) + '...' : alias).escaped()
                                + '</a><img src="' + $(this).find('photo').text().escaped()
                                + '" alt="' + alias.escaped() + '"></img></li>';
                        }
                    );
                    $ul.html(html);
                    if (html.length > 0) {
                        $ul.show();
                    }
                },
            });
        }
    );
}

$(document).ready(setup);

