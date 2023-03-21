

# Fee


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **String** |  |  [optional]
**studentId** | **String** |  |  [optional]
**remainingAmount** | **Integer** |  |  [optional]
**status** | [**StatusEnum**](#StatusEnum) |  |  [optional]
**updatedAt** | **java.time.Instant** |  |  [optional]
**type** | [**TypeEnum**](#TypeEnum) |  |  [optional]
**comment** | **String** |  |  [optional]
**totalAmount** | **Integer** |  |  [optional]
**creationDatetime** | **java.time.Instant** |  |  [optional]
**dueDatetime** | **java.time.Instant** |  |  [optional]



## Enum: StatusEnum

Name | Value
---- | -----
UNPAID | &quot;UNPAID&quot;
PAID | &quot;PAID&quot;
LATE | &quot;LATE&quot;



## Enum: TypeEnum

Name | Value
---- | -----
TUITION | &quot;TUITION&quot;
HARDWARE | &quot;HARDWARE&quot;



