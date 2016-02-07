# chronontology-connected

Chronontology provides access to underlying datastores via its REST api.
Detailed information on the REST api can be found [here](docs/rest-api-reference.md).

## Building and testing the application

For the Component and Integration Tests to run, type in

```
gradle clean test
```

To build the application, type in

```
gradle clean jar
```

The binary with dependencies can be found here:

```
build/libs/chronontology-connected-0.1.0-SNAPSHOT.jar
```

## Using the server

The application uses two datastores. The main datastore is
file system based. The connected datastore is an elastisearch instance.

At startup, the application reads the properties from 

```
config.properties
```

You can find one [here](config.properties.template). Make sure you revise the settings before startup!

For the local datastore to work, you have to create subfolders for every type 
inside the directory of the local datastore. An example:

```
datastore/period
```

is the necessary for the abovementioned config.properties to work properly.

In addition to that, you need an elasticsearch instance running.

A type mapping for every type used is needed, so make sure 
you didn't forget to add the mapping to the period type!
 
You'll find it [here](src/main/resources/mapping.json).

To summarize, your directory should look like this:

```
connected-backend-0.1.0-SNAPSHOT.jar
datastore/period
config.properties
```

To run the application, you need the binary, which can get executed like this:

```
java -jar connected-backend-0.1.0-SNAPSHOT.jar
```

Execution of the main method starts an embedded jetty server.






