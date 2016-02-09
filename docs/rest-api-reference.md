# Rest Api Reference


## POST /:typeName/

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

The request body must be valid JSON.

### Status codes: 

```
    201 if created successfully.
    500 if there is an error. Like for example if the request body 
      could not get JSON-parsed properly
```

## PUT /:typeName/:id

Used to update an existing document. The version number will get incremented
and a date will be added to the date modified array. If the document does not exist yet, 
it will be created. 

The request body must be valid JSON.

### Status codes: 

```
    200 if updated succesfully.
    201 if a new document has been created successfully.
    500 if there is an error. Like for example if the request body 
      could not get JSON-parsed properly
```

## GET /

Get information regarding the server status. 
Also Lists information for each datastore individually.

The response body will look similiar to this:

```
  { 
    "datastores" : [
      { "type" : "main", "status" : "ok" },
      { "type" : "connect", "status" : "down" }
    ]
  }
```

### Status codes:

```
    200 if Server is running and all datastores are connected and running.
    404 if Server is running but at least one datastore is not available.
```

## GET /:typeName/:id

Get json stored for type with name :typeName and id :id.

## GET /:typeName/:id?direct=true

Get json stored for type with name :typeName and id :id. the json is retrieved from 
the main storage this time, not from the connected storage.

## GET /:typeName/:esQueryString

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
