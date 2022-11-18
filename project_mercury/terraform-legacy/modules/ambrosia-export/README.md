
# Running this locally.

Much of the External HTTP services depend on AWS services. Some research was performed to find some libraries to 
be able to run this module locally, but most failed because the SNS messages flying around are required to be
signed. There were no third-party libraries which were sending the SNS events with these fields and/or properly signed.

In order to work around this, this directory contains a small subset of terraform files that will allow a
developer to stand up the necessary AWS infrastructure with ease.

The default setup will process messages the same way that the production setup is expected to. If needed, you can run 
failure cases to test the dead letter queue functionality, by replacing one or both the lambdas with the commented out 
"failure lambda" blocks. They are:

1. aws_lambda_function.ext-http-processor (lines ~103-105)
2. aws_lambda_function.ext-http-retry-processor (lines ~131-134)


## General requirements to operate this

1. terraform installed (version irrelevant as only used by you locally)
2. a clone & compiled binary [aero-service-lambda-ext-http-processor](https://gitlab.com/pearsontechnology/gpt/aero/aero-service-lambda-courseware-element-to-ambrosia-processor) residing relative to this directory, e.g. `../../../../../aero-service-lambda-courseware-element-to-ambrosia-processor/lambda-courseware-element-to-ambrosia-0.0.1.zip`.
3. a clone & compiled binary [aero-service-lambda-sqs-to-sns](https://gitlab.com/pearsontechnology/gpt/aero/aero-service-lambda-sqs-to-sns) residing relative to this directory, e.g. `../../../../../aero-service-lambda-sqs-to-sns/lambda-sqs-to-sns-0.0.1.zip`.
4. a clone & compiled binary [aero-service-lambda-fail](https://gitlab.com/pearsontechnology/gpt/aero/aero-service-lambda-fail) residing relative to this directory, e.g. `../../../../../aero-service-lambda-fail/aero-service-lambda-fail-1.0.0.zip`.
5. Some way to expose your local on a port, try `ngrok`.


## Go!

There are 2 main endpoints that interact with the services.

1. SNS Topic `<ENV>-courseware-element-to-ambrosia-submit`
2. SQS Queue `<ENV>-courseware-element-to-ambrosia-retry-delay`


### Step 1

Startup `ngrok` or similar service, grab your external URL.


### Step 2

Start a Local server using `-D` args (replace `<ENV>` with your name, e.g. homer):

```
-Dcourseware-element-to-ambrosia.submitTopicNameOrArn=<ENV>-courseware-element-to-ambrosia-submit
-Dcourseware-element-to-ambrosia.delayQueueNameOrArn=<ENV>-courseware-element-to-ambrosia-retry-delay
```

### Step 3

Using terraform vars,

* `ENV` - this is the ENV to add the the topics and queue names. Easiest route, just use your name.
* `base_endpoint` - this is the base URL to use to communicate back to your local server.

Run:

```
AWS_PROFILE=LOCAL terraform apply -var 'ENV=homer' -var 'base_endpoint=https://external_url'
```


## Teardown!

1. Shutdown your `ngrok` tunnel or you will be tracked down.
2. Shutdown your LOCAL server.
3. Run a terraform destroy.

```
AWS_PROFILE=LOCAL terraform destroy -var 'ENV=homer' -var 'base_endpoint=https://external_url'
```

