# lambda-sqs-to-sns

This function is intended to read records from SQS `record.body` and publish it to an SNS `record.Message`. 
The fields in both records are a JSON string.

The reading of the SQS queue is wired outside of this scope. The SNS topic to which the function publishes 
is provided as an environment variable `DEST_TOPIC_ARN`.


## Environment Variables

In order to decouple where this function writes results to, the following topics must be configured as environment variables:

* TOPIC_ARN - the topic where retry notifications are published

