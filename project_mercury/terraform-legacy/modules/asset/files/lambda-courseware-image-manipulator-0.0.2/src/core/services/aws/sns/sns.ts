import * as AWS from 'aws-sdk';
import { MessageAttributeMap } from 'aws-sdk/clients/sns';
import { PromiseResult } from 'aws-sdk/lib/request';

import { ISNS } from './types';

export const SnsTopics = {
	SUCCESS: process.env.TOPIC_ARN_COURSEWARE_ASSET_RESIZE_RESULT,
	ERROR: process.env.TOPIC_ARN_COURSEWARE_ASSET_RESIZE_ERROR,
};

export class SNS implements ISNS {
	sns: AWS.SNS;
	constructor(public sdk = AWS) {
		this.sns = new sdk.SNS();
	}

	publish = (
		message: any,
		topic: string,
		attributes?: MessageAttributeMap
	): Promise<PromiseResult<AWS.SNS.PublishResponse, AWS.AWSError>> => {
		console.debug('Sns message: ', {
			message: JSON.stringify(message),
		});
		return this.sns
			.publish({
				TopicArn: topic,
				Message: JSON.stringify(message),
				MessageAttributes: attributes,
			})
			.promise();
	};
}
