# Dataset based access management

The connected backend has the ability to provide access control for groups
of documents known as **datasets**. For these datasets, users can be granted permissions
to perform certain operations, like **read** or **modify**.

## Basic rules

In general, if a document is not assigned to any dataset, it can be read by anyone
and modified only by logged in users.

If a documents belongs to a dataset or a new one should be created and assigned to a dataset,
the user needs **editor** grade permissions for that dataset in order to do so. 
For reading documents assigned to a datagroup, the user need **reader** level permissions 
for that dataset. This also applies to search, which is a variation of read. When searching 
for documents, only documents are shown which are either not assigned to any dataset or which
are assigned to datasets the user has **read** permissions.

The user with the user name **admin** has special permissions. Once successfully 
authenticated under the name ***admin*** has the right to do anything, 
regardless of wether or not beeing explicitely assigned rights to any dataset group.

## POST /:typeName/

If enriched with the dataset property, a document gets
assigned to a dataset group, as shown in the example.

```
{
  "a" : "c" 
  "dataset" : "ds1"
}
```

This is only allowed if the user posting to the backend is 
granted **editor** rights for the dataset group under consideration.

## PUT /:typeName/:preDetermindedIdentifier

The same applies to the put endpoint, when used to create an object with
a certain identifier.

```
{
  "a" : "c" 
  "dataset" : "ds1"
}
```

Again, this is only allowed if the user posting to the backend is 
granted **editor** rights for the dataset group under consideration.

## PUT /:typeName/:existingIdentifier

When used to update an existing document, the user must have been granted
**editor** rights for the dataset specified in the json of the request body.

```
{
  "a" : "c" 
  "dataset" : "ds1"
}
```

If the user omits the dataset property, the document gets un-assigned from any previously
assigned dataset group.

```
{
  "a" : "c" 
}
```

**Note** that in both cases, if the last version of the document to be updated
is assigned to an existing dataset group, like
 
```
{
    "a" : "c" 
    "dataset" : "ds0"
}
```
 
 the user must have **editor** rights on both the old and the new dataset group.

## GET /:typeName/:identifier

If enriched with the dataset property, only users with who are granted 
the proper permission rights can access a document

A document

```
{
    "a" : "c" 
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
    "a" : "c" 
    "dataset" : "ds1"
}

{
    "a" : "c" 
}
```

and the user has no permissions for ds1, gives a result set only containing the 
second hit.



