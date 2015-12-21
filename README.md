play-elastic4s
===========================

[![Build Status](https://travis-ci.org/evojam/play-elastic4s.svg)](https://travis-ci.org/evojam/play-elastic4s)

We've been using [elastic4s](https://github.com/sksamuel/elastic4s) with [Play framework](https://www.playframework.com/) for a while.
Without a convenient way of configuration and injecting ElasticClient instance there was a lot boilerplate work to do.

This module lets you extend your application.conf and get an ElasticSearchClient injectable for free.

## Usage

Provide ES configuration and enable this module in `application.conf`

    elastic4s {
        myCluster {
           default: true
           uri: elasticsearch://host:port
           cluster.name: "mycluster"
        }
    }

    play.modules.enabled += "com.evojam.play.elastic4s.Elastic4sModule"

Then inject where needed, e.g:

    import com.evojam.play.elastic4s.client.ElasticSearchClient
    import com.google.inject.Inject

    class SearchDao @Inject() (elastic: ElasticSearchClient) {
        ...
    }

Note, you may access the underlying `ElasticClient` from `com.sksamuel.elastic4s.ElasticClient` if needed
through `elastic.underlying` given the example above.
