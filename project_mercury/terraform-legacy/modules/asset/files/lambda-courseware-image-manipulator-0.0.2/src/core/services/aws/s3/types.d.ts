import { S3 } from 'aws-sdk';
export declare type BaseBucketParams = {
    bucket: string;
    prefix: string;
    key: string;
};
export declare type S3GetParams = BaseBucketParams;
export declare type S3UploadParams = BaseBucketParams & {
    body?: any;
};
export interface IBucket {
    s3: AWS.S3;
    get(params: S3GetParams): Promise<S3.GetObjectOutput>;
    put(params: S3UploadParams): Promise<S3.ManagedUpload.SendData>;
}
//# sourceMappingURL=types.d.ts.map