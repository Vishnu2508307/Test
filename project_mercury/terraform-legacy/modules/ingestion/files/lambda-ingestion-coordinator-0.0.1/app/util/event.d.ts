import { SQSEvent, SQSRecord } from 'aws-lambda';
export declare const getEventMessage: (event: SQSEvent) => SQSRecord;
export declare const isMessageExist: (message: SQSRecord) => boolean;
export declare const getQueueName: (queueArn: any) => string;
//# sourceMappingURL=event.d.ts.map