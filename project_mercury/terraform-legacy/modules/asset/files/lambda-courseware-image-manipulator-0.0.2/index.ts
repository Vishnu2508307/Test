import {
	S3,
	SNS,
	parseLambdaSnsResizeEvent,
	SnsTopics,
	generateS3Params,
	getImageMetadata,
	resizeImageToThreshold,
	S3Params,
	FIXEDPREFIX,
	IImageResizeMessageBody,
} from './src/core';

const s3 = new S3();
const sns = new SNS();

export const handler = async (event): Promise<void> => {
	console.debug(event.Records[0].Sns);

	const bucketName = event.Records[0].Sns.MessageAttributes['bucketName']?.Value;
	const imageRecord = parseLambdaSnsResizeEvent(event)[0];
	const s3ImageParams = generateS3Params(imageRecord, bucketName);

	console.info('processing image', { bucket: s3ImageParams.getParams, url: imageRecord.url });

	try {
		const result = await processImage(s3ImageParams, imageRecord);
		const cleanedImageRecord = cleanImageRecord(imageRecord);

		await sns.publish({ ...cleanedImageRecord, ...result }, SnsTopics.SUCCESS);
	} catch (error) {
		console.info(error);
		await sns.publish(
			{
				notificationId: imageRecord.notificationId,
				assetId: imageRecord.assetId,
				cause: error?.message,
				error: JSON.stringify(error),
			},
			SnsTopics.ERROR
		);
	}
};

async function processImage(s3ImageParams: S3Params, imageDetails: IImageResizeMessageBody) {
	try {
		const image = await s3.get(s3ImageParams.getParams);
		const resizedImage = await resizeImageToThreshold(image.Body, imageDetails);
		const imageMetaData = await getImageMetadata(resizedImage);
		const s3Result = await s3.put({
			...s3ImageParams.uploadParams,
			body: resizedImage,
		});

		return {
			url: s3Result.Location.split(FIXEDPREFIX)[1],
			height: imageMetaData.height,
			width: imageMetaData.width,
		};
	} catch (err) {
		throw err;
	}
}

function cleanImageRecord(imageRecord) {
	const record = Object.assign({}, imageRecord);
	delete record.originalWidth;
	delete record.originalHeight;
	delete record.threshold;
	return record;
}
