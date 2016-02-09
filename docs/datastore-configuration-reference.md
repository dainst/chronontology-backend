# Datastore Configuration Reference

## Modes

The connected backend can operate in two modes.

In **connect** mode there are two datastores. The first datastore (id "0") is the connect datastore,
where data get send to to get enriched by the yet to be implemented connect component. 
The second one (id "1") is the main datastore where the original data get stored.
In **single** mode there is only one datastore (id "0"). 


A datastore with the id 0 is of type elasticsearch in any case.
A additonal datastore with the id 1 in **connect**-mode is of type filesystem.

The "connect" mode is default but can be turned off with using the property

```
useConnect=false
```

## Datestore configuration

A datastore can be either an elasticsearch datastore or 
a filesystem datastore. This can be specified explicitely by
the property

```
datastores.[id].type=elasticsearch
datastores.[id].type=filesystem
```

If ommitted, it will default to "elasticsearch".

Depending on which type you choose for the datastore,
different properties are necessary.

### Elasticsearch datastore configuration

The url param can be ommitted and defaults to "http://localhost:9202", which is 
also the default of the embedded elasticsearch server.

```
datastores.[id].url=http://localhost:9200
```

The indexName can be ommitted and defaults to "connect".

```
datastores.[id].indexName=myIndex
```

The simplest possible configuration for such a datastore is empty.
It will automatically be configured by default to

```
datastores.[id].type=elasticsearch
datastores.[id].indexName=myIndex
datastores.[id].url=http://localhost:9202
```

### Filesystem datastore configuration

The path param is a relative path to the folder where the data get stored
into. If it does not exist, it will get created at application start.

The path param can be empty and default to "datastore/".

```
datastores.[id].path=myDataStorePath/
```

The simplest possible configuration in such a case
would be just

```
datastores.[id].type=filesystem
```

which is necessary, because otherwise it would default to "elasticsearch".

## Simple Configuration examples

The system of default values makes it possible to have very
lean configurations. 

### Connect mode

The simplest possible configuration for connect mode would be

```
datastores.1.type=filesystem
```

which equals 

```
useConnect=true
datastores.0.type=elasticsearch
datastores.0.indexName=myIndex
datastores.0.url=http://localhost:9202
datastores.1.type=filesystem
datastores.1.path=datastore/
```

### Single mode

The simples possible configuration for single mode is

```
useConnect=false
```

which equals

```
useConnect=false
datastores.0.type=elasticsearch
datastores.0.indexName=myIndex
datastores.0.url=http://localhost:9202
```

