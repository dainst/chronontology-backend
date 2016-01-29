# chronontology-connected

## Using the server

Execution of the main method starts an embedded 
jetty server.

The application uses two datastores. The main datastore is
file system based. The connected datastore is an elastisearch instance.

At startup, the application reads the properties from 

```
  config.properties
```

Make sure you revise the settings before startup!

A type mapping for every type used is needed, so make sure 
you didn't forget to add the mapping to the period type!
 
You'll find it [here](src/main/resources/mapping.json).

### POST /:typeName/

Post json to store a a document of type :typeName.
After posting, the location header will contain the id of 
the created element, if successful. The id will be a string like

```
  /period/TAvlBuaAasWM
```  
  
That means, it will contain both the type name as well as a 
base64 encoded random part.

The response body will contain the json in the form it got send
to the stores. This means that it will be enriched by id an date information.

```
  Status codes: 
    201 if created successfully.
```

### PUT /:typeName/:id

Used to update an existing document. The version number will get incremented
and a date will be added to the date modified array.

### GET /:typeName/:id

Get json stored for type with name :typeName and id :id.

### GET /:typeName/:id?direct=true

Get json stored for type with name :typeName and id :id. the json is retrieved from 
the main storage this time, not from the connected storage.

### GET /:typeName/:esQueryString

Performs a search specified by :esQueryString 
over the documents of the type named :typeName.
Gets a json object with a top level array field named results which
contains the json for the search hits. 

An example could be

```
GET /period/?q=*
```

The query string gets handed over to elasticsearch, so it 
should be a valid search string for elasticsearch
and should **not** include the "_search" prefix but everything after it.

## Building and testing the application

For the Component and Integration Tests to run, you need an elastic search 
instance on localhost up and running. The index named "jeremy_test" 
is used and its types and type mappings get created before and deleted after the tests automatically.

To run the tests, type in
```
gradle clean test
```

To build the application, type in

```
gradle clean packageJar
```

The binary with dependencies can be found here:

```
java -jar build/libs/all-1.0-SNAPSHOT.jar
```

To run it, you need to place the binary into an empty directory of your choice, 
create an empty datastore directory, and place a [config.properties](config.properties) next to the jar-file.

