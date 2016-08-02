# Rest Api Reference

This document describes the basic behaviour of the rest api 
endpoints provided by the connected-backend. **Note** that there
is also dedicated document that focuses [dataset based access 
management](dataset-management.md) with the connected backend.


## POST /:type/

Post json to store a a document of type :type.
The request body must be valid JSON. It can be empty

```
{}
```

which is sufficient for creating the document. 
To store client data within the document, there is the **resource** field,
which itself can be empty or host any further user defined fields. Examples:
 
```
{
  "resource" : {}
}

{
  "resource" : {
    "a" : "b"
  } 
}
```

**Note** that there are some reserved terms. In order to avoid confusion,
they should not be used by clients directly since the system writes 
them automatically. At the moment these are `id` and `type`. 
The user might set them, but they will get overwritten in any case.
 
Thus do not use:

```
{
  "resource" : {
    "id" : ...
  } 
}
```

 
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

jeremy not only ignores all other fields of the top level of the incoming JSON, 
but also removes them before storing the documents. 

### Response body

The response body shows the object how it is
actually stored. It contains additional information
and will look something like this:

```
{
   "resource": {
       "id": "T7UlxIk8miMQ",
       "type" : "typename",
       "a" : "b"
   },
   "dataset" : "none",
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

The location response header will contain the `id` of 
the created element, if successful. It is the same
as the `id field of the response body. It will be a string like

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

**Note** that as with POST, using the `id` field should be avoided
since the `id` gets set automatically by the system.

### Response body

```
{
    "resource": { 
        "id": "T7UlxIk8miMQ",
        "type" : "typename",
        "a" : "b" 
    },
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
        "id": "T7UlxIk8miMQ",
        "type" : "typename",
        "a" : "b"
    },        
    "dataset" : "none",
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

## GET /:type?:queryParams

Performs a search over all documents of the :type.
 
The simplest version, which returns all documents, looks like this

```
GET /typename
```

However due to potentially large result sets, one can and should narrow
down the search with the use of :queryParams. 

The **size** and **from** query params
are used to only show a result set of size ***size***, starting with the document with the
offset ***from*** of the ordered result set. These params can be used solo or together, but you'll
probably find it most useful when used together, for example for paginating through a result 
set. Here are some valid examples of its usage:


```
GET /typename?size=10&offset=10
GET /typename?from=0&size=10
GET /typename?from=10&size=10
GET /typename?from=10
GET /typename?size=3
```

The first example will return the 10 results starting from the 10th result.

Another means of narrowing down the result set is by use of **query terms**.
The provided features and the syntax of the query language depends on the datastore implementation.
The elasticsearch datastore supports [the query string mini language](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html#query-string-syntax)

Here are some examples:

```
GET /typename/?q=a
```

will match documents that have "a" as a value of some of their fields.

```
GET /typename/?q=user:karl
```

will match documents who have some "user" field where there is a "karl" value.

```
GET /typename/?q=user:karl+a:b
```

will combine the search by using a logical "or". It matches documents which
have either a "user" field with the value "karl" or an "a" field with the value "b" (or both).

A search can combine both ***query terms*** and ***size*** params.
For example


```
GET /typename/?q=user:karl+a:b&size=1&from=2
```

Please **note** no matter what search params you use, a user will always only see 
search results for datasets he is allowed to see. For more information on datasets see
[this](dataset-management.md) document.


### Response body 

The response body is a json object with a top level array 
field named "results" which
contains the json for the search hits and an attribute "total"
that gives the total number of resources in the datastore that
match the query and that the user is allowed to access.

```
{
    "results": [
        {
            "resource": {
                "id": "T7UlxIk8miMQ",
                "type" : "typename",
                "a" : "b"
            },
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
                "id": "MG6UPjCMKmk",
                "type" : "typename",
                "a" : "d"
            },
            "dataset" : "dataset1",
            "version": 1,
            ...
        },
        {
            "resource": ...,
            ...
        },
        ...
    ],
    "total": 23
}
```

### Status codes:

```
200 if there are documents for the bucket.
404 if the bucket does not exist yet.
```
