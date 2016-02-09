# Elasticsearch Server Configuration Reference

For ease of use during development and testing, the connected
backend can start an embedded elasticsearch server. To activate this
feature set

```
useEmbeddedES=true
```

This is also the simplest possible configuration for using this embedded server,
because all other properties are optional due to the fact they all have default values.

The first property is the server port. On localhost this is the port the embedded
elasticsearch server will listen to.

```
esServer.port=9200
```

The server port defaults to "9202" if ommitted.

```
esServer.clusterName=myClusterName
```

The clusterName defaults to "chronontology_connected_embedded_es" if omitted.

```
esServer.dataPath=myData
```

The dataPath defaults to "embedded_es_data" if ommitted.

Basically that means the simple configuration

```
useEmbeddedES=true
```

equals

```
useEmbeddedES=true
esServer.port=9202
esServer.dataPath=embedded_es_data
esServer.clusterName=chronontology_connected_embedded_es
```

