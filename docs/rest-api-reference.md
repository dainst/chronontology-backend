# Rest Api Reference

This document describes the basic behaviour of the rest api 
endpoints provided by the connected-backend. **Note** that there
is also dedicated document that focuses [dataset based access 
management](dataset-management.md) with the connected backend.


## POST /:type/

Post json to store a a document of type :type.
The request body must be valid JSON and should contain
at least a **resource** field. For example

```
{
  "resource" : "c" 
}
```

or 

```
{
  "resource" : {
    "a" : "b"
  } 
}
```

The ***resource*** field is meant to contain the 
resources content as specified by a client.
 
**Note** that, with the exception of the ***resource*** and ***dataset*** fields,

as in


```
{
  "resource" : {
    "a" : "b"
  } 
  "dataset" : "dataset1"
}
```

(see for [dataset management](dataset-management.md)),

connected-backend not only ignores all other fields of the incoming JSON, 
but also removes them before storing the documents. 

### Response body

The response body shows the object how it is
actually stored. It contains additional information
and will look something like this:

```
{
   "resource": {
       "a" : "b"
   },
   "dataset" : "none",
   "@id": "/typename/T7UlxIk8miMQ",
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
/typename/T7UlxIk8miMQ
```  
  
It contains both the type name as well as a 
base64 encoded random part to which we refer simply as the "id"
in the rest of the document.

### Status codes: 

```
201 if created successfully.
400 if there is an error during parsing the JSON of the the request body. 
403 if there is no permission to create a document within the given dataset.
500 if object could not be updated or created due to an internal server error,
  like missing access to a datastore.
```

## PUT /:type/:id

Used to update an existing document or to create 
a document with a pre-determinded id. The request body must
be valid JSON, for example:

```
{
  "resource" : { "a" : "b" } 
}
```

### Response body

```
{
    "resource": { "a" : "b" },
    "@id": "/typename/T7UlxIk8miMQ",
    "version": 2,
    "dataset" : "none",
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
403 if there is no permission to create a document within or update a document to a dataset, or
  there is no permission to change a document which is already assigned to a dataset.
500 if object could not be updated or created due to an internal server error,
  like missing access to a datastore.
```

## GET /

Get information regarding the server status. 
Also Lists information for each datastore individually.

See [here](datastore-configuration-reference.md) for information on how to configure the datastores. 

### Response body

The response body will look similiar to this:

```
{ 
    "datastores" : [
      { "role" : "connect", "type" : "elasticsearch", "status" : "down" },
      { "role" : "main" , "type" : "filesystem", "status" : "ok" }
    ]
}
```

### Status codes:

```
200 if Server is running and all datastores are connected and running.
404 if Server is running but at least one datastore is not available.
```

## GET /:type/\\:id\\:queryParams

Get json stored for type with name :type and id :id.
This is a simple example without :queryParams:

```
GET /typename/x1Xz
```

In **connected** mode, there are two possible optional :queryParams.
These are ignored, if in **single** mode.
The first query param is **direct**. When used with the value true, as in

```
GET /typename/x1Xz?direct=true
```

the document is fetched from the main datastore.

The second query param is **version**, which can be used to fetch a specific version
of a document, as in

```
GET /typename/x1Xz?version=2
```

This specific version of a document gets fetched from the main datastore. The direct param
gets ignored if ***version*** is used.

### Response body

```
{
    "resource": {
        "a" : "b"
    },        
    "dataset" : "none",
    "@id": "/typename/T7UlxIk8miMQ",
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

Note that in case connect mode is used the JSON can contain 
additional fields added by the iDAI.connect component.

### Status codes:

```
200 if the document has been found.
403 if there is no permission to see a document which is assigned to a dataset.
404 if the document or the specified version of a document was not found.
```

## GET /:type/:queryParams

Performs a search over all documents of the :type.
 
The simplest version, which returns all documents, looks like this

```
GET /typename/
```

However due to potentially large result sets, one can and should narrow
down the search with the use of :queryParams. 

The **size** and **from** query params
are used to only show a result set of size ***size***, starting with the document with the
id ***from*** of an ordered result set. These params can be used solo or together, but you'll
probably find it most useful when used together, for example for paginating through a result 
set. Here are some valid examples of its usage:


```
GET /typename/?size=10&offset=10
GET /typename/?from=0&size=10
GET /typename/?from=10&size=10
GET /typename/?from=10
GET /typename/?size=3
```

The first example will return the 10 results starting from the 10th result.

Another means of narrowing down the result set is by use of **query terms**.
These are related to the elasticsearch uri search usage. Here are some examples:

```
GET /typename/?a
```

will match documents that have "a" as a value of some of their fields.

```
GET /typename/?user:karl
```

will match documents who have some "user" field where there is a "karl" value.

Queries of the second type can be chained like

```
GET /typename/?user:karl&a:b
```

This will combine the search by using a logical "or". It matches documents who
have either a "user" field with the value "karl" or an "a" field with the value "b" (or both).

A search can combine both ***query terms*** and ***size*** params.
For example


```
GET /typename/?user:karl&a:b&size=1&from=2
```

Please **note** no matter what search params you use, a user will always only see 
search results for datasets he is allowed to see. For more information on datasets see
[this](dataset-management.md) document.


### Response body 

The response body is a json object with a top level array 
field named "results" which
contains the json for the search hits. 

```
{
    "results": [
        {
            "resource": {
                "a" : "b"
            },
            "@id": "/typename/T7UlxIk8miMQ",
            "version": 2,
            "dataset" : "none",
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
                    "date": "2016-02-10T11:30:16.423Z"
                }
            ]
        },
        {
            "resource": {
                "a" : "d"
            },
            "@id": "/typename/MG6UPjCMKmk",
            "dataset" : "dataset1",
            "version": 1,
            ...
        },
        {
            "resource": ...,
            ...
        },
        ...
    ]
}
```

### Status codes:

```
200 if there are documents for the bucket.
404 if the bucket does not exist yet.
```
