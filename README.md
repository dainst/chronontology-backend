# chronontology-connected

Execution of the main method starts an embedded 
jetty server listening on 0.0.0.0:4567.

A file system based datastore is used which is assumed to 
be located at datastore/ relative to the current working directory from
which you started the main. However, by specifying an alternative path
as command line argument, the specified folder gets used as datastore.

## POST /period/:id 

post json to store a period with id

## GET /perios/:id

get json stored for period with id