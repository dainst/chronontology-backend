# chronontology-connected

## Using the server

Execution of the main method starts an embedded 
jetty server.

The application uses two datastores. The main datastore is
file system based. The connected datastore is an elastisearch instance.

At startup, the application reads the properties from 

  config.properties

Make sure you revise the settings before startup!

A type mapping for every type used is needed, so make sure 
you didn't forget to add the mapping to the period type!
 
  type mapping : src/main/resources/mapping.json

### POST /period/:id 

Post json to store a period with id.

### GET /period/:id

Get json stored for period with id.

### GET /period/:id?direct=true

Get json stored for period with id. the json is retrieved from 
the main storage this time, not from the connected storage.

### GET /period/?q=searchCriteria(?size=1000)

Gets a json object with a top level array field named results which
contains the json for the search hits. Additionally, you can restrict the 
size of the search result set by using the size query parameter.

An example for searchCriteria can be

  /period/?q=a:b
  
which means, the that the field a in all records gets searched for the term b.
If there is a match, the record gets added to the result set.

For searching in all fields, you can issue a search request like

  /period/?q=b

## Testing

For the Component Tests to run, you need an elastic search 
instance on localhost up and running. The index named "jeremy_test" 
is used. Make sure that the index has the type mapping from

  src/test/resources/mapping.json
