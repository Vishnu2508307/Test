import * as AWS from 'aws-sdk';
import { MessageAttributeMap } from 'aws-sdk/clients/sns';
import { PromiseResult } from 'aws-sdk/lib/request';
import { ISNS } from './types';
export declare const SnsTopics: {
    SUCCESS: string;
    ERROR: string;
};
export declare class SNS implements ISNS {
    sdk: typeof AWS;
    sns: AWS.SNS;
    constructor(sdk?: typeof AWS);
    publish: (message: any, topic: string, attributes?: MessageAttributeMap) => Promise<PromiseResult<AWS.SNS.PublishResponse, AWS.AWSError>>;
}
//# sourceMappingURL=sns.d.ts.map