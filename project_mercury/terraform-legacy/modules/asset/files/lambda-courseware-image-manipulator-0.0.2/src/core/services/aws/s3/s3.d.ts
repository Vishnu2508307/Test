import * as AWS from 'aws-sdk';
import { IBucket, S3GetParams, S3UploadParams } from './types';
export declare class S3 implements IBucket {
    sdk: typeof AWS;
    s3: AWS.S3;
    constructor(sdk?: typeof AWS);
    get: ({ prefix, key, bucket }: S3GetParams) => Promise<AWS.S3.GetObjectOutput>;
    put: ({ body, prefix, key, bucket }: S3UploadParams) => Promise<AWS.S3.ManagedUpload.SendData>;
}
//# sourceMappingURL=s3.d.ts.map