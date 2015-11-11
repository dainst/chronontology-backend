# chronontology-connected

## Using the server

Execution of the main method starts an embedded 
jetty server listening on 0.0.0.0:4567.

A file system based datastore is used as the primary datastore.
It is is assumed to be located at datastore/ relative to the 
current working directory from
which you started the main. However, by specifying an alternative path
as command line argument, the specified folder gets used as datastore.

Elasticsearch is used as the connected datastore. So make sure 
you have an elasticSearch instance running with the following settings:

  cluster name : elasticsearch
  index name : jeremy
  type name : period
  type mapping : src/main/resources/mapping.json
  
Make sure you didn't forget to add the mapping to the period type!

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
