# Om.next to Datomic

This is a project, in the spirit of [www.reactrails.com](http://www.reactrails.com), which shows a full Clojure/ClojureScript stack that implements a consist outlook toward functional programming, immutable data, and composable abstractions.

It shows [Om.next](https://github.com/omcljs/om/wiki/Quick-Start-%28om.next%29), the wrapper for React, on the front-end. It uses [Transit](https://github.com/cognitect/transit-format) to communicate the data over the wire. And [Datomic](http://www.datomic.com/) as the database.

## Kicking the Tires

To run it, you need to [install leiningen](http://leiningen.org/), the build tool and dependency manager for Clojure.

Clone this directory and cd into it, then type:

    lein run

Open a browser to http://localhost:8080 and you are in business!

## Development

The development experience on the front-end is pretty sweet where the browser automatically updates whenever you save a source file.

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To create a production build run:

    lein cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL. 
