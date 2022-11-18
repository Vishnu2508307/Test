import * as AWS from 'aws-sdk';
import * as mime from 'mime/lite';
import { join } from 'path';

import { IBucket, S3GetParams, S3UploadParams } from './types';

export class S3 implements IBucket {
	s3: AWS.S3;
	constructor(public sdk = AWS) {
		this.s3 = new sdk.S3();
	}

	// Retrieves objects from Amazon S3
	get = ({ prefix, key, bucket }: S3GetParams): Promise<AWS.S3.GetObjectOutput> =>
		this.s3
			.getObject({
				Bucket: bucket,
				Key: join(prefix, key),
			})
			.promise();

	// Adds an object to a bucket.
	put = ({ body, prefix, key, bucket }: S3UploadParams): Promise<AWS.S3.ManagedUpload.SendData> =>
		this.s3
			.upload({
				Bucket: bucket,
				Body: body,
				Key: join(prefix, key),
				ContentType: mime.getType(key) || 'image',
			})
			.promise();
}
