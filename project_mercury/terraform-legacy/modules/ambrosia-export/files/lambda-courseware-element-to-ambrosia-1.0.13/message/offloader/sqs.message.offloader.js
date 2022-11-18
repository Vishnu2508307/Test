/*global Buffer*/
const AWS = require('aws-sdk');

/**
 * When the content is too big to be included in the sqs message then, write it to s3 and add the key as a message
 * attribute
 */
class SQSMessageOffLoader {
    attributes = {
        S3_KEY: 's3Key',
        LIMIT: 1024 * 128,
        NOTIFICATION_ATTRIBUTE: 'notificationId'
    };

    /**
     * Take in a result notification and returns sqs params. Offload the message body to an s3 bucket when the size
     * is too big.
     *
     * @param resultNotification
     * @returns {Promise<Object>} a promise containing the sqs params object
     */
    async offload(resultNotification) {
        // get the ambrosia snippet size
        const marshalledNotification = JSON.stringify(resultNotification);
        const notificationSize = Buffer.from(marshalledNotification).length
        const messageAttributes = {};
        messageAttributes[this.attributes.NOTIFICATION_ATTRIBUTE] = {
            DataType: 'String',
            StringValue: resultNotification.notificationId
        };

        if (notificationSize >= this.attributes.LIMIT || process.env.SNIPPETS_STORAGE_IN_S3 === 'true') {
            // the notification is too big so we are going to write it to s3
            const key = `${resultNotification.exportId}/${resultNotification.notificationId}/in.json`;
            await toS3(process.env.AMBROSIA_SNIPPET_BUCKET, key, marshalledNotification)
                // re-throw the error
                .catch((err) => {
                    throw err;
                });

            // store the s3 key in the message attributes and return the params
            messageAttributes[this.attributes.S3_KEY] = {
                DataType: 'String',
                StringValue: key
            };

            // delete the snippet from the message body becuase, its already uploaded to S3
            delete resultNotification.ambrosiaSnippet;
            return {
                MessageBody: JSON.stringify(resultNotification),
                MessageAttributes: messageAttributes // set the message attributes
            }
        }

        // return the params with the notification as body
        return {
            MessageBody: marshalledNotification,
            MessageAttributes: messageAttributes
        }
    }
}

/**
 * Write content to the s3 bucket at the specified key
 *
 * @param bucket the bucket to write content to
 * @param key the location in the bucket
 * @param body the body to write to
 * @returns {Promise<PutObjectOutput>} a promise containing the put object output
 */
async function toS3(bucket, key, body) {
    return new AWS.S3()
        .putObject({
            Body: body,
            Bucket: bucket,
            Key: key
        })
        .promise();
}

module.exports = new SQSMessageOffLoader();