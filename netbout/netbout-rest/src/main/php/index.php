<?php
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

// Deploy this file to logs.netbout.com (it's in Hostgator platform at
// the moment). REST logger will post all log data to this URL, and this
// script will allow you to see this log online. Just go to
// http://logs.netbout.com

$file = dirname(__FILE__) . '/log-' . date('Y-M-d') . '.txt';
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $handle = fopen($file, 'a+');
    if ($handle === false) {
        echo "can't open file '${file}' for writing";
    } else {
        fwrite($handle, file_get_contents('php://input'));
    }
} else {
    echo "<html><head>
        <meta http-equiv='refresh' content='5; URL=http://logs.netbout.com'/>
        <body style='font-family: monospace; white-space: pre-wrap;'>";
    echo $file . ":\n\n";
    echo htmlspecialchars(`tail -50 ${file} | tac`);
    echo "</body></html>";
}
