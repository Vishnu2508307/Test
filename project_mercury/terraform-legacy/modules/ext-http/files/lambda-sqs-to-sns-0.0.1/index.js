let AWS = require('aws-sdk');
const sns = new AWS.SNS(); // ({ region: 'ap-southeast-2' });

exports.handler = async (event) => {

    // pre-checks.
    if (!event.Records) {
        throw new Error("no records to process");
    }
    if (!process.env.TOPIC_ARN) {
        throw new Error("missing TOPIC_ARN!");
    }

    // process the incoming records.
    for (const record of event.Records) {
        if (process.env.DEBUG_LOG) {
            console.log(JSON.stringify(record, null, 3));
        }

        const {body} = record;

        // publish the SQS body as the "Message" in SNS.
        await sns.publish({
            Message: body,
            TopicArn: process.env.TOPIC_ARN
        }).promise();
    }

    return {};

};
