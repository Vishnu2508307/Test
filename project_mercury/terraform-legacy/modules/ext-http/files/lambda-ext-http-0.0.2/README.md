
## Intro

This project is intended to be executed as an AWS Lambda Function which consumes SNS events. The consumed 
SNS event must contain the options on how to execute an HTTP request on an external endpoint or an error
will be returned to the SNS error topic.

Input to this Lambda function is highly dependent on the underlying library, `request`. The HTTP request 
is executed using the `request(options, ...)` method; see:

* [https://github.com/request/request#requestoptions-callback](https://github.com/request/request#requestoptions-callback)

Note: Some options may not be fully supported at this time, e.g. file operations.

The request/response will be logged, including the headers and propigated (or back to the services) via SNS.

This module will set a custom `User-Agent` as `SPR-aero/1.0`.

Error handling:

1. A non-2xx HTTP Status response from the remote server is not considered an error. It is up to the 
consumer of the events to decide what steps to take next in this situtation.
2. Errors which are lambda related will be logged to Cloudwatch & the respective error handler via SNS.

## Environment variables

In order to decouple where this function writes results to, these topics must be configured as environment variables:

* TOPIC_ARN_EXT_HTTP_RESULT - the topic where results are published
* TOPIC_ARN_EXT_HTTP_ERROR - the topic where errors are published

## Package for AWS Lambda

Perform the following steps in order to create an AWS Lambda .zip file (`lambda-ext-http-x.y.z.zip`)

```bash
npm install
npm pack
```


