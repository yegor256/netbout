<img src="http://img.netbout.com/logo.svg" width="132px"/>

[![Made By Teamed.io](http://img.teamed.io/btn.svg)](http://www.teamed.io)

[![Build Status](https://travis-ci.org/yegor256/netbout.svg?branch=master)](https://travis-ci.org/yegor256/netbout)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.netbout/netbout/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.netbout/netbout)

Netbout.com is a communication platform that enables smoothless integration
of humans and software agents in a conversation-centered environment.

Try it at [www.netbout.com](http://www.netbout.com).

## How to contribute?

Fork the repository, make changes, submit a pull request.
We promise to review your changes same day and apply to
the `master` branch, if they look correct.

Please run Maven (3.1 or higher!) build before submitting a pull request:

```
$ mvn clean install -Pqulice
```

Want to run it locally? Simple as that:

```
$ mvn clean install -Phit-refresh -Dport=8080
```

In a minute the site is ready at `http://localhost:8080`
