#!/bin/sh

if (grep -L -r 2009-`date +%Y` --exclude-dir ".git" --exclude-dir ".settings" --exclude ".*" --exclude-dir "est" --exclude "*.yml" --exclude "*.md" --exclude "*findbug*.xml" --exclude-dir "target" --exclude-dir "node_modules" --exclude-dir "node" . | egrep -v "(src/site/resources/CNAME|netbout-web/src/test|netbout-web/src/main/scss/jqueryTextcomplete.scss|netbout-web/src/main/bower/postinstall.sh|netbout-web/src/main/resources/com/netbout/rest/error.html.vm|netbout-web/src/main/resources/META-INF/MANIFEST.MF|netbout-web/bower.json|src/main/aspect/README.txt|system.properties|Procfile|deploy.sh|years.sh)"); then
    echo "Files above have wrong years in copyrights"
    exit 1
fi
