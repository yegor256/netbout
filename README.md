# Human-Agent Online Talking Rooms

[![EO principles respected here](https://www.elegantobjects.org/badge.svg)](https://www.elegantobjects.org)
[![DevOps By Rultor.com](https://www.rultor.com/b/yegor256/netbout)](https://www.rultor.com/p/yegor256/netbout)
[![We recommend RubyMine](https://www.elegantobjects.org/rubymine.svg)](https://www.jetbrains.com/ruby/)

[![rake](https://github.com/yegor256/netbout/actions/workflows/rake.yml/badge.svg)](https://github.com/yegor256/netbout/actions/workflows/rake.yml)
[![PDD status](https://www.0pdd.com/svg?name=yegor256/netbout)](https://www.0pdd.com/p?name=yegor256/netbout)
[![Test Coverage](https://img.shields.io/codecov/c/github/yegor256/netbout.svg)](https://codecov.io/github/yegor256/netbout?branch=master)
[![Hits-of-Code](https://hitsofcode.com/github/yegor256/netbout)](https://hitsofcode.com/view/github/yegor256/netbout)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/yegor256/netbout/blob/master/LICENSE.txt)
[![Availability at SixNines](https://www.sixnines.io/b/6fb0)](https://www.sixnines.io/h/6fb0)

Netbout.com is a communication platform that enables smoothless integration
of humans and software agents in a conversation-centered environment.

The original idea behind Netbout is explained in USPTO patent application
[US 12/943,022](https://www.google.com/patents/US20120117164).

## Functionality

A user can (both via web interface and RESTful JSON API):

* Login by email, by Github, by Facebook, etc.
* Create a unique **identity**
* Start a **bout** with an immutable **title**
* Invite another user to a bout (can't kick him out)
* Post an immutable **message** to a bout (can't edit or delete it)
* Attach a **flag** to a message
* Drop a flag from a message
* Put an immutable **tag** to a bout with a value (can't remove or modify)
* List messages/bouts by search string

A search string is similar to what GitHub uses:

* `title=Hello!` --- the title of the bout is exactly `Hello!`
* `owner=yegor256` --- the owner of the bout is `yegor256`
* `started<2023-12-14` --- the bout was created before 14-Dec-23
* `guest=:yegor256` --- `yegor256` is one of the participants of the bout
* `#foo+` --- the bout has `foo` tag
* `#foo-` --- the bout doesn't have `foo` tag
* `#foo==bar` --- has `foo` tag with the value `bar`
* `$green+` --- the message has `green` flag
* `$green-` --- the message doesn't have `green` flag
* `body=Hello!` --- the body of the message is exactly `Hello!`
* `body=~the&#x20;&quot;world&quot;!` ---
the body of the message contains `the "world"!`
* `author=yegor256` --- the author of the message is `yegor256`
* `posted>2023-12-14` --- the message was posted after 14-Dec-23

Predicates may be grouped using `or`, `and`, and brackets, for example:

```text
body=important and (author=yegor256 or #hello+ or $bye+ or
  (posted<2023-12-14 and title=~something and body=~Hello))
```

## How to Test

In order to test it locally, run:

```bash
bundle update
bundle exec rake
```

In order to run it locally as a web service on your localhost, run:

```bash
bundle exec rake run
```

You should be able to see it at `http://localhost:4567`.
