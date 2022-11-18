let AWS = require('aws-sdk');
const sqs = new AWS.SQS();
const notificationHandler = require('./handler/notification.handler');
const MessageEnricher = require('./message/enricher/message.enricher');
const SQSMessageOffLoader = require('./message/offloader/sqs.message.offloader');
const MessageUtils = require('./common/message.utils');

// Checking environment config
if (!process.env.QUEUE_URL_COURSEWARE_ELEMENT_TO_AMBROSIA_RESULT) {
	throw new Error('missing QUEUE_URL_COURSEWARE_ELEMENT_TO_AMBROSIA_RESULT!');
}
if (!process.env.QUEUE_URL_COURSEWARE_ELEMENT_TO_AMBROSIA_ERROR) {
	throw new Error('missing QUEUE_URL_COURSEWARE_ELEMENT_TO_AMBROSIA_ERROR!');
}

exports.handler = async (event) => {
	// pre-checks.
	if (!event.Records) {
		throw new Error('no records to process');
	}

	// process the incoming records.
	for (const record of event.Records) {

		if (process.env.DEBUG_LOG) {
			console.log(JSON.stringify(record, null, 3));
		}

		if (!record.Sns) {
			throw new Error('sns object not found');
		}

		// prepare variables
		const sns = record.Sns;

		// this line will throw an error when the notificationId is not included
		// as a message attribute. NotificationId is a required field
		const notificationId = MessageUtils.getMessageAttribute(sns, SQSMessageOffLoader.attributes.NOTIFICATION_ATTRIBUTE);


		// Enrich the incoming message
		const message = await MessageEnricher.enrich(JSON.parse(sns.Message), sns);

		// throw early if the notificationId from the message is not the same as the
		// notificationId from the message attributes
		if (message.notificationId !== notificationId) {
			throw new Error(`invalid notificationId. Message has: ${message.notificationId}, Header has: ${notificationId}`);
		}

		try {
			const resultNotification = await notificationHandler.handle(message);
			// run the notification through the offLoader
			const params = await SQSMessageOffLoader.offload(resultNotification);
			// send the sqs message using the offLoader body and attributes params
			await sqs.sendMessage({
				QueueUrl: process.env.QUEUE_URL_COURSEWARE_ELEMENT_TO_AMBROSIA_RESULT,
				MessageBody: params.MessageBody,
				MessageAttributes: params.MessageAttributes
			}).promise()
		} catch (err) {
			// build the error notification
			await sqs.sendMessage({
				MessageBody: JSON.stringify({
					errorMessage: 'error generating the ambrosia snippet',
					cause: err.message,
					exportId: message.exportId,
					notificationId: message.notificationId
				}),
				MessageAttributes: {
					notificationId: {
						DataType: 'String',
						StringValue: notificationId
					}
				},
				QueueUrl: process.env.QUEUE_URL_COURSEWARE_ELEMENT_TO_AMBROSIA_ERROR
			}).promise().catch((reason) => {
				console.error('failed to send sqs message to error queue: ', JSON.stringify(reason))
			});
		}
	}
};
