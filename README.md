README.md
=========

IRCTest is a simple Java SE IRC client, used as the prototype for the
eventual Java ME port. As such, while it works and can be used to
connect to and chat on IRC servers, its feature set is limited and it 
lacks many features normally taken for granted.

I've decided to upload this to Github as I'd like to expand it into a
more fully-featured application if possible. It may be that the code is
too old, naïve, and poorly-implemented to do so, but it will nonetheless
be fun to try. Any suggestions, recommendations, or bug fixes would be
welcomed with open arms.

Attributes such as nickname, server address, and channel are hard-coded
in the Java source and (with the exception of nickname) cannot be 
changed without recompiling the application.

It is strongly recommended you change the values of nickname, server, and
port in IRCTest.java before you compile, as the defaults are unlikely to
work and are not guaranteed to be suitable.

To use: compile the application as you would any other at the command line:
	javac IRCTest.java
	
This should result in three files: IRCTest.class, MessageSender.class,
and MessageReceiver.class. Run IRCTest.class at the command line to
start the application:
	java IRCTest
	
IRCTest was written for and tested with Java 6 release 7 on Mac OS X.
It should work fine under Java 5, but has not been tested; it definitely
won't work in 1.4.2 or lower, as it uses Scanner.

The author, Lawrence Matthews, may be reached at lmatthews@roguefox.net.
Permission is hereby granted to amend, alter, and distribute this code in
any form on- or offline, provided attribution is given.
