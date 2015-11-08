# chronontology-connected

## Using the server

Execution of the main method starts an embedded 
jetty server listening on 0.0.0.0:4567.

A file system based datastore is used which is assumed to 
be located at datastore/ relative to the current working directory from
which you started the main. However, by specifying an alternative path
as command line argument, the specified folder gets used as datastore.

### POST /period/:id 

Post json to store a period with id.

### GET /period/:id

Get json stored for period with id.

### GET /period/:id?direct=true

Get json stored for period with id. the json is retrieved from 
the main storage this time, not from the connected storage.

## Testing

For the Component Tests to run, you need an elastic search 
instance on localhost up and running. The index named "jeremy_test" 
is used.
