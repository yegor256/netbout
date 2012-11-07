Netbout.com, Communication Platform
===================================

Netbout.com is an isolated persistent online conversation between
parties, both people and computers. A conversation is started by a participant
who has the ability to invite others and remove them when necessary.
Every participant can send messages to the conversation, making them visible to others.

<div style="background-image: url(https://dxe6yfv2r7pzd.cloudfront.net/figures/flow.png);
    float: right; margin-left: 2em; margin-bottom: 2em;
    width: 567px; height: 265px;">&nbsp;</div>

Every conversation, called "bout", is asynchronous, meaning that every participant
sends a message to the bout without getting an immediate answer. Instead,
the message gets posted in the bout and becomes visible for all other
participants. There is where Netbout.com resembles a bulletin board
system or online chat. Messages are posted in a chronological order
and are visible to all participants just like messages in an online
blog or forum message, with the most recent on top (or on the bottom).
Every message has a number of attributes attached to it and is visible
to readers, including date and time of publication, name and photo of the author.

Netbout.com solves the problems which other communication mechanisms
(like e-mail,
[SNS](http://en.wikipedia.org/wiki/Social_networking_service),
and conferences) suffer from related to their
lack of persistency, isolation, privacy, and usability:

 * **Persistency.** Every bout is persistent for a lifetime. Its participants
   do not need to archive, protocol, or log anything manually.
 * **Isolation.** Every bout is isolated and is visible only to its participants. A bout
   resembles a private dedicated room for a single conversation. Even
   if the same participants establish another conversation on some
   other subject -- it is another ``room'' and another bout isolated
   from all others.
 * **Privacy.** Netbout.com does not reveal any private details of its participants.
   It does not enable any of its participants to send
   unsolicited messages to each other outside of the established and accepted
   conversation.
 * **Usability.** Netbout.com is an easy-to-use online entity, which is
   created, archived, tagged, read, and understood in seconds for
   a non-computer user.

The concept is pending USPTO patent,
[app. no. 12/943,022](http://www.google.com/patents/US20120117164).
See also [Terms of Use](terms).

Persistency
-----------

Contrary to e-mails, video conferences, and phone calls, A bout
has an endless life cycle. It is absolutely persistent, meaning that
its participants may get back to the conversation any time and the
conversation will stay online. The persistence of a bout does not
depend on the willingness of its participants. None of them can cancel
a bout, thus making it invisible to others. Once a bout is
started and its participants have accepted an invitation to join, nobody
can destroy the bout and remove it from a participant's account.
Such a strict persistence management mechanism distinguishes
Netbout.com from forums, bulletin board systems, and almost all other
communication means, where in most cases an initiator of a conversation controls
its persistency.

Isolation
---------

Netbout.com is n securely isolated communication environment for its
users, where they exchange information messages without
any fear of their disclosure to anyone else. Access to
Netbout.com messages is protected by user name and password,
and can't be shared with anyone except its owner.

<div style="background-image: url(https://dxe6yfv2r7pzd.cloudfront.net/figures/design.png);
    float: right; margin-left: 2em; margin-bottom: 2em;
    width: 453px; height: 489px;">&nbsp;</div>

Privacy
-------

Unlike e-mails, instant messages, phones and other peer-to-peer
communication means, Netbout.com does not disclose contact details
to conversation participants. The only public element
is the unique [URN](http://en.wikipedia.org/wiki/Uniform_resource_name)
of a registered user.
Knowing this URN does not mean that
a new conversation can be established with this user. However it is possible
to send an invitation to this user. Once the invitation is accepted, a new
bout may be established. In other words, users
are hidden behind their Netbout.com URNs, without any fear to be
accidentally or intentionally contacted by an unwanted party.

Usability
---------

The Netbout.com online conversation environment is web hosted and does not
require any software installation or configuration, unlike
web conferences or instant messaging systems. Moreover, the initialization
and closure of a bout is done in seconds, with a simple web
click to the link or a bookmark in a web browser. Netbout.com is
very similar to a "blog" page, where users post their comments
and read replies from other users. The page can be opened very quickly
and does not require any software configuration or installation.

Extendability
-------------

Netbout.com seamlessly integrates communicating parties, which
include humans, computers, and software helpers inside the platform.
"Helper" makes private server-specific data presentable in
a conversation-centric format for end-users. The majority of
data is presentable in a simple message-by-message dialog form. We
develop helpers for new customers, but you can do that yourself as well.

"Helper" tailors the user interface of Netbout.com platform for a specific
style of your particular business application. Moreover, it makes changes
to the functionality.
Helpers extends existing architecture and design in an object oriented
way.

