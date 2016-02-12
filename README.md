play-elastic4s
===========================

[![Build Status](https://travis-ci.org/evojam/play-elastic4s.svg)](https://travis-ci.org/evojam/play-elastic4s)

We've been using [elastic4s](https://github.com/sksamuel/elastic4s) with [Play framework](https://www.playframework.com/) for a while.
Without a convenient way of configuration and injecting ElasticClient instance there was a lot boilerplate work to do.

This module lets you extend your application.conf and get an ElasticSearchClient injectable for free.

## Installation

Current stable version:

```scala
resolvers += Resolver.sonatypeRepo("releases")
libraryDependencies += "com.evojam" % "play-elastic4s_2.11" % "0.1.0"
```

Current snapshot:

```scala
resolvers += Resolver.sonatypeRepo("snapshots")
libraryDependencies += "com.evojam" % "play-elastic4s_2.11" % "0.1.0-SNAPSHOT"
```

## Quick start

Provide ES configuration and enable this module in `application.conf`. You may list multiple Elasticsearch client
configuration blocks, and multiple index definitions in the indexTypes block. They will be available for injection
with the @Named annotation, as shown in the example below.

    elastic4s {
        myCluster {
           default: true
           uri: "elasticsearch://host:port"
           cluster.name: "mycluster"
        }
        indexTypes {
            myIndex {
                index: "myIndexName"
                type: "myDocType"
            }
            otherIndex {
                index: "otherIndexName"
                type: "otherDocType"
            }
        }
    }

    play.modules.enabled += "com.evojam.play.elastic4s.Elastic4sModule"

Then inject where needed, e.g:

    import javax.inject.Inject
    import javax.inject.Named

    import com.evojam.play.elastic4s.client.ElasticSearchClient
    import com.sksamuel.elastic4s.IndexType

    class MyClass @Inject() (elastic: ElasticSearchClient, @Named("myIndex") myIndex: IndexType) {

        def search(query: SearchDefinition): Future[List[MyRecord]] =
            elastic.search(query)
                .collect[MyRecord]

        def index(doc: MyRecord): Future[Boolean] =
            elastic.index(myIndex, doc)

    }

Note, you may access the underlying `ElasticClient` from `com.sksamuel.elastic4s.ElasticClient` if needed
through `elastic.underlying` given the example above.

## CRUD

_Examples below assume similar class structure as above, i.e having an Injected client and `indexType`._

Simple [get](https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-get.html) by the document's `id`

    def get(id: String): Future[Option[MyRecord]] =
        elastic.get(id, myIndex)
          .collect[MyRecord]

Similar request as above, however this time using the [get api](https://github.com/sksamuel/elastic4s/blob/master/guide/get.md) from [elastic4s](https://github.com/sksamuel/elastic4s/) for more advanced querying.

    def get(docId: String, versionNumber: Int): Future[Option[MyRecord]] =
        elastic.get(get id docId from myIndex version versionNumber)
          .collect[MyRecord]

A [multiget](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-multi-get.html) request for a list of `id`'s

    def getAll(ids: List[String]): Future[List[MyRecord]] =
        elastic.bulkGet(ids, myIndex)
           .collect[MyRecord]

As in the `get` request we can also use the [get api](https://github.com/sksamuel/elastic4s/blob/master/guide/get.md). In this get query we only want to retrieve the fields "firstName" and "lastName" from the fetched documents.

    def getAll(ids: List[String]): Future[List[MyRecord]] =
        elastic.bulkGet(ids.map(docId => get id docId from myIndex fields List("firstName", "lastName"))
            .collect[MyRecord]