[Rest API Reference](rest-api-reference.md)

# Dataset based access management

The *connected backend* has the ability to control access to groups
of documents known as **datasets**. For these datasets, users can be granted permissions
to perform the operations **read** or **modify**.

## Basic rules

In general, if a document is not assigned to any dataset, it can be read by anyone
and modified only by logged in users.

If a documents belongs to a dataset or a new one should be created and assigned to a dataset,
the user needs **editor** grade permissions for that dataset in order to do so. 
For reading the documents of any dataset, the user needs **reader** level permissions 
for that dataset. 

A user having the ***editor*** permission level on a dataset can perfom the operations ***read***
and ***modify***, which a user having the ***reader*** permission level can perform the ***read***
operation only.

This also applies to **search**, which in this context is seen as just 
a variation of ***read***. When searching for documents, only documents are shown 
which are either not assigned to any dataset or which
are assigned to any specific dataset or to which the user has **reader** level permissions.

### Special user roles

Datasets can also get marked for **anonymous** access, in which case anyone, even without beeing
authenticated, can read or search for documents in the dataset while there is still user based
access control for ***edit*** operations. ***edit*** operations can never be performed by 
***anonymous*** users.

The user authenticated under the user name **admin** has permissions to perform any operation
on any dataset, regardless of wether or not beeing explicitely assigned rights to any dataset group.

## POST /:typeName/

If enriched with the dataset property, a document gets
assigned to a dataset group, as shown in the example.

```
{
  "resource": {
      "a" : "b"
      },
  "dataset" : "ds1"
}
```

This is only allowed if the user posting to the backend is 
granted **editor** rights for the dataset group under consideration.

If the dataset property is ommited on POST, the document gets created without any specific
dataset assignment. The system then sets the value "none" for the dataset of the document.
This can also be done explicitely by setting "dataset" to "none".

```
{
  "resource": {
      "a" : "b"
  },
  "dataset" : "none"
}
```

## PUT /:typeName/:preDetermindedIdentifier

The same applies to the put endpoint, when used to create an object with
a certain identifier.

```
{
  "resource": {
      "a" : "b"
  },
  "dataset" : "ds1"
}
```

Again, this is only allowed if the user posting to the backend is 
granted **editor** rights for the dataset group under consideration.

## PUT /:typeName/:existingIdentifier

In case PUT is used to create document, its behaviour is exactly like described in
the POST section above.

In case the document is to be updated and the last version of the document 
is already assigned to a dataset, the user needs ***editor*** level permissions for that
dataset in order to change the document.

An existing document

```
{
  "resource": ...
  "dataset" : "ds1",
  ...
}
```

can be changed only if the user has ***editor*** level permissions for the dataset "ds1".

If it should get assigned to any other dataset, 
like when trying to PUT with a request body like

```
{
  "resource": ...
  "dataset" : "ds2",
  ...
}
```

the user needs also ***editor*** level permissions for the dataset "ds2".

If the user omits the dataset property (or sets it to "none"), 
the document gets un-assigned from any specific dataset.

```
PUT
{
  "resource": ...
}
PUT
{
  "resource": ...
  "dataset" : "none"
}
```

## GET /:typeName/:identifier

If enriched with the dataset property, only users with who are granted 
the proper permission rights can access a document

A document

```
{
    "resource": {
        "a" : "b"
    },
    "dataset" : "ds1"
}
```

can only be accessed by a user who is either an **editor** or a **reader** for
the dataset ds1.

## GET /:typeName/

Searching respects the dataset groups insofar as documents, which are assigned to datasets and
for which the actual user has not either **editor** or **reader** permissions, are filtered out.

For example a search for all documents, where all documents are the following two

```
{
    "resource": {
        "a" : "b"
    },
    "dataset" : "ds1"
}

{
    "resource": {
        "a" : "c"
    },
}
```

and the user has no ***reader*** level permissions for "ds1", 
gives a result set only containing the second hit.



