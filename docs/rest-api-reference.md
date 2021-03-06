# Rest Api Reference

This document describes the basic behaviour of the rest api
endpoints provided by the connected-backend. **Note** that there
is also dedicated document that focuses [dataset based access
management](dataset-management.md) with the connected backend.


## POST /:type/

Post JSON to store a document of type :type.
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


**Note** that, with the exception of the ***resource***, ***derived*** and
***related*** fields as well as the ***dataset*** field,

as in


```
{
  "resource" : {
    "key" : "value"
  }
  "derived" : {
    "key" : "value"
  }
  "related" : {
    "key" : "value"
  }
  "dataset" : "dataset1"
}
```

(see for [dataset management](dataset-management.md)),

jeremy not only ignores all other fields of the top level of the incoming JSON,
but also removes them before storing the documents.

### Response body

The response body shows the object as it is
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
       "date": "2017-05-29T10:25:37.142+02:00"
   },
   "modified": [
       {
           "user": "karl",
           "date": "2017-05-29T10:30:15.151+02:00"
       }
   ]
}
```

In case of status 400 errors the response body is an empty JSON.

```
{}
```

### Response header

The location response header will contain the `type` and `id` of
the created element, if successful. `type` and `id` are the same
as the `type` and `id` fields in the response body. It will be a string like

```
/period/T7UlxIk8miMQ
```

The `id` consists of 12 random letters and cyphers, i.e. `[a-zA-Z0-9]{12}`.

### Status codes:

```
201 if created successfully.
400 if there is an error during parsing the JSON of the the request body.
403 if there is no permission to create a document within the given dataset.
500 if the object could not be updated or created due to an internal server
    error, such as missing access to a datastore.
```

## POST /update_mapping
Post a new or updated Elastic Search mapping. The datastore configuration has to
be set to **connected mode**. See [here](datastore-configuration-reference.md)
for information on how to configure the datastores.

The request body must contain the mapping in valid JSON. Example:

```
{
  "period": {
    "dynamic" : "true",
    "properties": {
      "id": {
        "type": "string"
      },
      "resource" : {
        "properties":{
          "types":{
            "type": "string",
            "index": "not_analyzed"
          },
          "provenance":
          {
            "type":"string",
            "index": "not_analyzed"
          }
        }
      }
    }
  }
}
```

### Response body

In case of a successful update:

```
{
    "status": "success"
}
```

In case of errors:.

```
{
    "status": "failure"
}
```

### Status codes:

```
200 if mapping was updated successfully.
400 if either the server is running in the wrong datastore mode or the mapping is missing.
401 if authorization is missing
```

## PUT /:type/:id

Used to update an existing document or to create
a document with a pre-determined id. The request body must
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
        "date": "2017-05-29T10:25:37.142+02:00"
    },
    "modified": [
        {
            "user": "karl",
            "date": "2017-05-29T10:30:15.151+02:00"
        },
        {
            "user": "ove",
            "date": "2017-05-30T13:50:48.26+02:00"
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
Also lists information for each datastore individually.

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

## GET /:type/:id?:queryParams

Get the JSON that corresponds to the specified :type and :id.
This is a simple example without :queryParams:

```
GET /typename/x1Xz
```

### direct, version
In **connected** mode there are two optional :queryParams, which are ignored
in **single** mode:

**direct**: When used with the value true, as in

```
GET /typename/x1Xz?direct=true
```
the document is fetched from the main datastore instead of Elasticsearch.

**version**: This parameter can be used to fetch a specific version
of a document, as in

```
GET /typename/x1Xz?version=2
```

This specific version of a document gets fetched from the main datastore. The
direct param gets ignored if ***version*** is used.

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

### from, size
Due to potentially large result sets, one can and should narrow
down the search with the use of :queryParams.

The **size** and **from** query params
are used to only show a result set of size ***size***, starting with the document with the
offset ***from*** of the ordered result set. These params can be used individually or together, but you'll
probably find it most useful when used together, for example for paginating through a result
set. Here are some valid examples of its usage:

```
GET /typename?from=10&size=10
GET /typename?from=0&size=10
GET /typename?from=10
GET /typename?size=3
```

The first example will return the 10 results starting with the result number 10
(the first result has the number 0).

If no size is specified, the default value is 10, i.e. 10 hits are delivered.

### facet, fq
The **facet** query parameter is used to retrieve associated facets for the specified field. Multiple **facet** parameters
can be used in the same query.

```
GET /typename/?q=*&facet=field1&facet=field2
```

The **fq** query parameter is used to restrict the results, returning only results with the specified value in the
specified field. Multiple **fq** parameters can be used in the same query.

```
GET /typename/?q=*&fq=field1:valueField1&fq=field1:valueField2
```

### q
Another means of narrowing down the result set is by use of **query terms**.
The provided features and the syntax of the query language depends on the datastore implementation.
The elasticsearch datastore supports [the query string mini language](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html#query-string-syntax)

Here are some examples:

```
GET /typename?q=*
```

matches all documents, i.e. is the same as `GET /typename` without the **q** parameter.

```
GET /typename/?q=adoption
```

will match documents that have "adoption" as a value of some of their fields.

```
GET /typename/?q=created.user:karl
```

will match documents that have some "user" field where there is a "karl" value.

```
GET /typename/?q=created.user:karl+a:b
```

will combine the search by using a logical "or". It matches documents which
have either a "user" field with the value "karl" or an "a" field with the value "b" (or both).


```
GET /typename/?q=created.user:karl AND a:b
```

will combine the search by using a logical "and". It matches documents which
have a "user" field with the value "karl" AND an "a" field with the value "b".


A search can combine both ***query terms*** and ***size*** params.
For example

```
GET /typename/?q=user:karl+a:b&size=1&from=2
```

Please **note** no matter what search params you use, users will always only see
search results for datasets they are allowed to see. For more information on datasets see
[this](dataset-management.md) document.

### part
The **part** parameter specifies which parts of the JSONs should be delivered.
Examples are:

```
GET /period/?size=1000&part=resource.names,resource.types,resource.provenance
GET /period/?size=1000&q=resource.provenance:chronontology&part=resource
```

By default, complete JSONs will be delivered.

### Response body

The response body is a json object with a top level array field named "results" which contains the json for the
search hits. The attribute "total" gives the total number of resources in the datastore that match the query and
that the user is allowed to access. The "facets" object contains the results for the requested field facets,
including the number of documents that did not have the respective field.

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
                ...
            },
            "modified": [
                {
                    ...
                },
                ...
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
    "total": 23,
    "facets": {
        "field1": {
            "doc_count_error_upper_bound": 0,
            "sum_other_doc_count": 3,
            "buckets": [
                {
                    "key": "field1_value1",
                    "doc_count": 5
                },
                {
                    "key": "field1_value2",
                    "doc_count": 4
                },
                ...
            ]
        },
        "field2": {
            "doc_count_error_upper_bound": 0,
            "sum_other_doc_count": 7,
            "buckets": [
                {
                    "key": "field2_value1",
                    "doc_count": 1
                },
                {
                    "key": "field2_value2",
                    "doc_count": 3
                },
                ...
            ]
        }
    }
}
```

### Status codes:

```
200 if there are documents for the bucket.
404 if the bucket does not exist yet.
```

## DELETE /:type/:deletedId/:replacementId

Used for removing duplicates. This marks the document specified by `:deletedId` as deleted and sets the document specified by 
`:replacementId` as its replacement.

**Note** that in order to ignore documents marked as deleted while searching, you have to update your Elastic Search 
mapping and define the fields `deleted` and `replacedBy`. 

For example:

```
{
    "period": {
        "dynamic" : false,
        "properties": {

            "resource" : {
                "type": "object",
                "properties":{
                (..)
                }
            },
            (..)
            "created": {
                (..)
            },
            "deleted": {
                "type": "boolean"
            },
            "replacedBy": {
                "type": "string"
            },
            "modified": {
                (..)
            }
        }
    }
}
```

### Response body

For the request `http://localhost:4567/period/0ORH5IjCY2oU/0vl9gt9NnfEs`, the document marked as deleted would be 
returned as follows:
 
```
{
    "resource": {
        "names": {
            "de": [
                "Kreide"
            ],
            "en": [
                "Cretaceous"
            ]
        },
        (..)
    },
    "version": 2,
    "created": {
        "user": "admin",
        "date": "2017-06-15T12:11:25.239+02:00"
    },
    "modified": [
        {
            "user": "admin",
            "date": "2017-06-15T12:11:36.149+02:00"
        }
    ],
    "deleted": true,
    "replacedBy": "0vl9gt9NnfEs"
}
```

## Status codes


```
200 if the document was successfully marked as deleted and the replacement was set.
400 if both document IDs are equal (document replacing itself).
400 if the replacement document is itself already marked as deleted.
404 if one of the specified documents does not exist.
```