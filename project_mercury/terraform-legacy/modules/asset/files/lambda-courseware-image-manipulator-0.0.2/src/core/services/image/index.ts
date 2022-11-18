const sharp = require('sharp');

import { IImageResizeMessageBody } from '../../types';

export function resizeImageToThreshold(
	image: AWS.S3.GetObjectOutput['Body'],
	imageRecord: IImageResizeMessageBody
): Buffer {
	const largerDimension = getLargerImageDimension(imageRecord);
	return sharp(image).resize({
		[largerDimension]: imageRecord.threshold,
	}).withMetadata().toBuffer();
}

const getLargerImageDimension = ({
	originalHeight,
	originalWidth,
}: IImageResizeMessageBody): string => (originalHeight > originalWidth ? 'height' : 'width');

export const getImageMetadata = (image: Buffer) =>
	sharp(image)
		.metadata()
		.then((metadata) => {
			return {
				width: metadata.width,
				height: metadata.height,
			};
		});
