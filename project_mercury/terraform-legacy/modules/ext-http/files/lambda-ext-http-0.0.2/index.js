let AWS = require('aws-sdk');
let processRequest = require('./requestProcessor');
const sns = new AWS.SNS(); // ({ region: 'ap-southeast-2' });

exports.handler = async (event) => {

    // pre-checks.
    if (!event.Records) {
        throw new Error("no records to process");
    }
    if (!process.env.TOPIC_ARN_EXT_HTTP_RESULT) {
        throw new Error("missing TOPIC_ARN_EXT_HTTP_RESULT!");
    }
    if (!process.env.TOPIC_ARN_EXT_HTTP_ERROR) {
        throw new Error("missing TOPIC_ARN_EXT_HTTP_ERROR!");
    }

    // process the incoming records.
    for( const record of event.Records ) {
        if (process.env.DEBUG_LOG) {
            console.log(JSON.stringify(record, null, 3));
        }

        // the "Message" field contains the payload
        let message = record.Sns.Message;
        let payload = JSON.parse(message);
        let requestOptions = payload.params;
        let requestState = payload.state;

        try {
            // make the request.
            let requestResponseLog = await processRequest(requestOptions);

            let response = {
                state: requestState,
                result: requestResponseLog
            };

            if (process.env.DEBUG_LOG) {
                console.log(JSON.stringify(response, null, 3));
            }
            // publish the response
            await sns.publish({
                Message: JSON.stringify(response),
                TargetArn: process.env.TOPIC_ARN_EXT_HTTP_RESULT
            }).promise();
        }
        catch (err) {

            //
            // something bad happened which was not captured
            //
            console.log(err);

            let response = {
                state: requestState,
                error: err.message
            };

            // notify the responsible message topic.
            await sns.publish({
                Message: JSON.stringify(response),
                TopicArn: process.env.TOPIC_ARN_EXT_HTTP_ERROR
            }).promise();

            // Do not rethrow this error, that signals AWS to retry the original message;
            // We do not want the original message to be replayed automatically, we want the Java Services to make that decision.
            //throw err;
        }
    }
};
