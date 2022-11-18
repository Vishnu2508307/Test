import { S3 } from 'aws-sdk';

export type BaseBucketParams = {
    bucket: string;
    prefix: string;
    key: string;
}

export type S3GetParams = BaseBucketParams;

export type S3UploadParams = BaseBucketParams & {
    body?: any;
};

export interface IBucket {
    s3: AWS.S3;
    get(params: S3GetParams): Promise<S3.GetObjectOutput>;
    put(params: S3UploadParams): Promise<S3.ManagedUpload.SendData>;
}