# Korhal

Korhal is a Brood War AI written in Clojure on top of a custom fork of JNIBWAPI that we use to extract even more information from the Brood War process.

## Getting Started

A virtual machine image is provided to get you up and running. [See the setup page in the wiki.](../../wiki/VM Setup)

[The wiki](../../wiki/Home) also has a decent amount of info about how Korhal works. Check it out before digging in.

## Current Status

Korhal is still in early development and is not combat ready. There's a whole lot of work to be done, so get in touch or file an issue if you'd like to contribute!

### Features

* nREPL server, useful for development
* Brood War API designed for Clojure, ported from JNIBWAPI
* Fully asynchronous AI using Clojure's awesome ref types
* Contracts system keeps track of resources that have been committed to actions but not yet spent
* Can execute build orders given in similar format to how humans write them, e.g. 9 :supply-depot
* Very basic micro routines, e.g. marines attempt to kite zealots

### Example Videos

[Marines kiting some zealots](http://www.youtube.com/watch?v=4cbZM1INAf4)

[Basic build order execution](http://www.youtube.com/watch?v=LnIq5zx1jqw)
