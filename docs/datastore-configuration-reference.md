# Datastore Configuration Reference

## Modes

The connected backend can operate in two modes.

In **single** mode there is one datastore, which is of type **elasticsearch**. Datastores
of this type are searchable.

In **connect** mode there are two datastores. 
The first one is the **main** datastore where the original data get stored. 
It is of type **filesystem**. A datastore of this type is not searchable, like datastores
of type ***elasticsearch***, but it has another rather distinctive feature: it collects and 
maintains a version history of documents. The second datastore is the **connect** 
datastore, where data get send to to get enriched by the yet to be implemented connect component. 
It is of type ***elasticsearch***.

When the application runs, you can watch the datastore configuration and status
by performing a GET request to the base route.

```
GET /
```

as is also documented [here](rest-api-reference.md).

The ***connect*** mode is default but can be turned off with using the property

```
useConnect=false
```

## Datestore configuration

As stated earlier, a datastore can be either an ***elasticsearch*** datastore or 
a ***filesystem*** datastore. 

Depending on which datastore you configure, you specify properties either of type

```
datastores.elasticsearch.
```

or

```
datastores.filesystem.
```

The specific properties differ on both datastores.

### Elasticsearch datastore configuration

The url param can be ommitted and defaults to "http://localhost:9202", which is 
also the default of the embedded elasticsearch server.

```
datastore.elasticsearch.url=http://localhost:9200
```

The indexName can be ommitted and defaults to "connect".

```
datastore.elasticsearch.indexName=myIndex
```

The simplest possible configuration for such a datastore is empty.
It will automatically be configured by default to

```
datastore.elasticsearch.indexName=myIndex
datastore.elasticsearch.url=http://localhost:9202
```

### Filesystem datastore configuration

The path param is a relative path to the folder where the data get stored
into. If it does not exist, it will get created at application start.

The path param can be empty and default to "datastore/".

```
datastores.filesystem.path=myDataStorePath/
```

The simplest possible configuration in such a case
would is empty because it then defaults as shown above.

## Simple Configuration examples

The system of default values makes it possible to have very
lean configurations. 

### Connect mode

According to the defaulting rules as described in the paragraphs above 
the simplest possible configuration for connect mode would just be empty.


which equals 

```
useConnect=true
datastore.elasticsearch.indexName=connect
datastore.elasticsearch.url=http://localhost:9202
datastore.filesystem.path=datastore/
```

### Single mode

The simples possible configuration for single mode is

```
useConnect=false
```

which equals

```
useConnect=false
datastore.elasticsearch.indexName=myIndex
datastore.elasticsearch.url=http://localhost:9202
```

