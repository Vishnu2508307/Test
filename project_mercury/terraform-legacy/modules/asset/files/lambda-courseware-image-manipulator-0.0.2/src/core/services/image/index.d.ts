/// <reference types="node" />
import { IImageResizeMessageBody } from '../../types';
export declare function resizeImageToThreshold(image: AWS.S3.GetObjectOutput['Body'], imageRecord: IImageResizeMessageBody): Buffer;
export declare const getImageMetadata: (image: Buffer) => any;
//# sourceMappingURL=index.d.ts.map