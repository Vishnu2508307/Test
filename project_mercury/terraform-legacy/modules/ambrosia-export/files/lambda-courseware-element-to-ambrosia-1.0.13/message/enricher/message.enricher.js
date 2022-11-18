const AWS = require('aws-sdk');
const MessageUtils = require('../../common/message.utils');
/**
 * This class is responsible for enriching the message content with the payload that was stored on s3.
 */
class MessageEnricher {
    attributes = { S3_KEY: 's3Key' };

    /**
     * Enrich the incoming message content when the body is empty by reading the content from s3
     *
     * @param message the incoming sns message
     * @param sns the sns record
     * @returns {Promise<Object>} a promise with the enriched message object
     */
    async enrich(message, sns) {
        if (isEmpty(message)) {
        	// load it from S3
        	const s3Key = MessageUtils.getMessageAttribute(sns, this.attributes.S3_KEY);

        	if (!s3Key) {
        	    throw new Error('s3Key not defined in message attributes');
            }

        	const response = await fromS3(process.env.AMBROSIA_SNIPPET_BUCKET, s3Key)
                // re-throw the error
        		.catch((err) => {
        			throw  err
        		});

        	// return the enriched message
        	return Object.assign(message, JSON.parse(response));
        }

        // there is nothing to enrich, simply return the message
        return message;
    }
}

/**
 * Read content from s3 in a promise fashion.
 *
 * @param bucket the bucket to read the content from
 * @param key the content location in the bucket
 * @returns {Promise<String>} returns a promise that will return either the content or an error
 */
async function fromS3(bucket, key) {
    // create the promise
    return new Promise((res, rej) => {
        // initialise the s3 component
       new AWS.S3()
           // read the object
           .getObject({
               Bucket: bucket,
               Key: key
           }, (err, data) => {
               // reject when an error occurs
               if (err) {
                   return rej(err);
               }
               // resolve with the data body
               return res(data.Body.toString());
           }) ;
    });
}


/**
 * Check if the object is empty
 *
 * @param obj
 * @returns {boolean}
 */
function isEmpty(obj) {
    return Object.keys(obj).length === 0;
}

module.exports = new MessageEnricher();