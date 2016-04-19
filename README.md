# autogram

Transition between two words, automagically!

## Overview

I've always had this strange desire to replicate Keynote's text animations - especially the one where words will "slide" into another.  When I was trying to find a small problem to try tackling with ClojureScript, I knew this was the perfect fit.  Behold, in all it's glorious styling, the demo:

[bendyorke.github.io/autogram/](http://bendyorke.github.io/autogram/)

## Setup

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

To create a production build run:

    lein do clean, cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL. 
