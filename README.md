# chronontology-connected

## Using the server

The application uses two datastores. The main datastore is
file system based. The connected datastore is an elastisearch instance.

At startup, the application reads the properties from 

```
config.properties
```

You can find one [here](config.properties). Make sure you revise the settings before startup!

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

To run the application, you need the binary, which can get executed like this:

```
java -jar all-1.0-SNAPSHOT.jar
```

Execution of the main method starts an embedded jetty server.
How to obtain the binary is desribed [here](README.md#building-and-testing-the-application).

## Rest Api Reference

Detailed information on the rest api can be found [here](docs/rest-api-reference.md).

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
build/libs/all-1.0-SNAPSHOT.jar
```



