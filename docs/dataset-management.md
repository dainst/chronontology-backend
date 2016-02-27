# Dataset based access management

Before going into detail with the behaviour of the endpoints regarding
dataset based access management, it is important to notice that a user 
successfully authenticated under the name **admin** has the right to 
do anything, regardless if beeing assigned rights to any dataset group.

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


