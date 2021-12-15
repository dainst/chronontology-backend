# chronontology-backend

The chronontology backend (formerly known as jeremy) provides access to underlying datastores via its REST api.
Detailed information on the REST api can be found [here](docs/rest-api-reference.md).

## Prerequisites

[SDKMAN!|https://sdkman.io/]

```
sdk env install
```

## Building and testing the application

For the Component and Integration Tests to run, type in

```
gradle clean test
```

To build the application, type in

```
gradle clean shadowJar
```

The binary with dependencies can be found here:

```
build/libs/jeremy.jar
```

For development the application can be run with
```
gradle run
```

## Using the server

The application can be used in two modes. In "connect" and in "single" mode.
While in "single" mode a single datastore is used, in "connect" mode two datastores
are used, one for storing the authoritative versions and one for sharing these
data with other applications, so that they can get enriched by the yet to be developed
"connect" component.

Also make sure to check out the Rest Api [Reference](docs/rest-api-reference.md)!

### Configuring the application

Depending on which configuration you want, and also for configuring other application details,
you have to have a config file at application startup.

```
config.properties
```

You can find one [here](config.properties.template).
Make sure you revise the settings before startup!

#### Datastore configurations

For information on how to configure the abovementioned datastores, have a look at the
[datastore configuration reference](docs/datastore-configuration-reference.md).

In any case, the application will need access to at least one elasticsearch instance in order
to work. You can either use an external elasticsearch instance on any machine you have
access to or you can launch the application with an embedded elasticsearch server. Information
on how to configure the application can be found at the
[elasticsearch server configuration reference](docs/elasticsearch-server-configuration-reference.md).

Note that you may need type mappings for every type used. So make sure
you didn't forget to add the mapping to the period type!

#### Logging to file system

(Un-)comment the appropriate lines in `./src/main/resources/log4j.properties` and make sure the server has write access to the specified file.

### Starting the application

To run the application, you need the binary, which can get executed like this:

```
java -jar jeremy.jar
```

Execution of the main method starts an embedded jetty server.
