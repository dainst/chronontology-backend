# Rest Api Reference


## POST /:typeName/

Post json to store a a document of type :typeName.
The request body must be valid JSON. For example

```
{
  "a" : "c" 
}
```

### Response body

The response body shows the object how it is
actually stored. It contains additional information
and will look something like this:

```
{
   "a": "c",
   "@id": "/period/T7UlxIk8miMQ",
   "version": 1,
   "created": {
       "user": "karl",
       "date": "2016-02-09T10:21:15.721Z"
   },
   "modified": [
       {
           "user": "karl",
           "date": "2016-02-09T10:21:15.721Z"
       }
   ]
}
```

In case of status 400 errors the response body is an empty JSON.

```
{}
```

### Response header

The location response header will contain the @id of 
the created element, if successful. It is the same
as the @id field of the response body. It will be a string like

```
/period/T7UlxIk8miMQ
```  
  
It contains both the type name as well as a 
base64 encoded random part to which we refer simply as the "id"
in the rest of the document.

### Status codes: 

```
201 if created successfully.
400 if there is an error during parsing the JSON of the the request body. 
500 if object could not be updated or created due to an internal server error,
  like missing access to a datastore.
```

## PUT /:typeName/:id

Used to update an existing document or to create 
a document with a pre-determinded id. The request body must
be valid JSON, for example:

```
{
  "a" : "d" 
}
```

### Response body

```
{
    "a": "d",
    "@id": "/period/T7UlxIk8miMQ",
    "version": 2,
    "created": {
        "user": "karl",
        "date": "2016-02-09T10:21:15.721Z"
    },
    "modified": [
        {
            "user": "karl",
            "date": "2016-02-09T10:21:15.721Z"
        },
        {
            "user": "ove",
            "date": "2016-02-09T10:32:50.702Z"
        }
    ]
}
```

In case of update of an existing document 
the version number will get incremented
and a date will be added to the date modified array. 

In case of status 400 errors the response body is an empty JSON.

```
{}
```

### Status codes: 

```
200 if updated succesfully.
201 if a new document has been created successfully.
400 if there is an error during parsing the JSON of the the request body. 
500 if object could not be updated or created due to an internal server error,
  like missing access to a datastore.
```

## GET /

Get information regarding the server status. 
Also Lists information for each datastore individually.

### Response body

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

### Response body

```
{
    "a": "d",            <= CAN BE MORE THAN THIS
    "@id": "/period/T7UlxIk8miMQ",
    "version": 2,
    "created": {
        "user": "karl",
        "date": "2016-02-09T10:21:15.721Z"
    },
    "modified": [
        {
            "user": "karl",
            "date": "2016-02-09T10:21:15.721Z"
        }
    ]
}
```

Note that in case connect mode is used the JSON can contain 
additional fields added by the iDAI.connect component.

### Status codes:

```
200 if the document has been found.
404 if the document was not found.
```

## GET /:typeName/:id?direct=true

Get json stored for type with name :typeName and id :id. the json is retrieved from 
the main storage this time, not from the connected storage.

### Response body

```
{
    "a": "d",           
    "@id": "/period/T7UlxIk8miMQ",
    "version": 2,
    "created": {
        "user": "karl",
        "date": "2016-02-09T10:21:15.721Z"
    },
    "modified": [
        {
            "user": "karl",
            "date": "2016-02-09T10:21:15.721Z"
        }
    ]
}
```

### Status codes:

```
200 if the document has been found.
404 if the document was not found.
```

## GET /:typeName/:esQueryString

Performs a search specified by :esQueryString 
over the documents of the type named :typeName.


An example could be

```
GET /period/?q=*
```

The simplest version looks like this

```
GET /period/
```

The query string gets handed over to elasticsearch, so it 
should be a valid search string for elasticsearch
and should **not** include the "_search" prefix but everything after it.

### Response body 

The response body is a json object with a top level array 
field named "results" which
contains the json for the search hits. 

```
{
    "results": [
        {
            "a": "c",
            "@id": "/period/T7UlxIk8miMQ",
            "version": 2,
            "created": {
                "user": "karl",
                "date": "2016-02-09T10:21:15.721Z"
            },
            "modified": [
                {
                    "user": "karl",
                    "date": "2016-02-09T10:21:15.721Z"
                }
            ]
        },
        {
            "a": "d",
            "@id": "/period/MG6UPjCMKmk",
            "version": 1,
            "created": {
                "user": "ove",
                "date": "2016-02-09T00:26:25.287Z"
            },
            "modified": [
                {
                    "user": "ove",
                    "date": "2016-02-09T00:26:25.287Z"
                }
            ]
        },
    ]
}
```

### Status codes:

```
200 if there are documents for the bucket.
404 if the bucket does not exist yet.
```
