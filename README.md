play-elastic4s
===========================

We'v been using [elastic4s](https://github.com/sksamuel/elastic4s) with [Play framework](https://www.playframework.com/) for a while.
Without convenient way of configuration and injecting ElasticClient instance there was a lot boilerplate work to do.

Extend your application.conf and get ElasticClient instance injectable for free, eg.:

    elastic4s {
        clusters {
            myCluster {
               default: true
               uri: elasticsearch://host:port
               cluster.name: "mycluster"
            }
        }
        indexAndTypes {
            book {
                index: "library"
                type: "book"
            }
        }
    }
