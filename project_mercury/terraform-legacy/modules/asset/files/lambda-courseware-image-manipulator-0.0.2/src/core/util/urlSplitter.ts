import { S3GetParams, S3UploadParams } from "../services/aws";
import { IImageResizeMessageBody } from "../types";

// This has occured as we unexpectedly receive message with url missing this.
// Wasn't an easy way to fix it in Mercury so we've just hardcoded this as a solution
export const FIXEDPREFIX = 'assets/';

export type S3Params = {
	getParams: S3GetParams;
	uploadParams: S3UploadParams;
};

export function generateS3Params({ url, size }: IImageResizeMessageBody, bucket: string): S3Params {
	const getParams = generateGetParams(url, bucket);

	const uploadParams = {
		...getParams,
		prefix: convertPrefixSize(getParams.prefix, size),
	};

	return {
		getParams,
		uploadParams,
	};
}

export function generateGetParams(url: string, bucket: string): S3GetParams {
	const { prefix, key } = getS3PrefixKeyFromUrl(url);
	return {
		bucket,
		prefix,
		key,
	};
}

// EXAMPLE URL: https://assets-bronte-dev.pearson.com/assets/ca70fc60-2320-11eb-92be-411e554e518b/original/378fb689a0c23fb94a303c0df7fa376f.jpg
export function getS3PrefixKeyFromUrl(url: string): Partial<S3GetParams> {
	const prefix = url.split('/');
	const key = prefix.pop();

	return {
		prefix: `${FIXEDPREFIX}${prefix.join('/')}`,
		key,
	};
}

// Removes size and adds new size
export function convertPrefixSize(prefix, size) {
	const splitPrefix = prefix.split('/');
	splitPrefix.pop()
	return `${splitPrefix.join('/')}/${size}`;
}
