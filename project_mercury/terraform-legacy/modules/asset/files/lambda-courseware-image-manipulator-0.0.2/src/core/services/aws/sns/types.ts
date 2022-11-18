import { MessageAttributeMap } from 'aws-sdk/clients/sns';
import { PromiseResult } from 'aws-sdk/lib/request';

export interface ISNS {
	sns: AWS.SNS;
	publish(
		message: any,
		topic: string,
		attributes?: MessageAttributeMap
	): Promise<PromiseResult<AWS.SNS.PublishResponse, AWS.AWSError>>;
}
