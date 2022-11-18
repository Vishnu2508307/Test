import { S3GetParams, S3UploadParams } from "../services/aws";
import { IImageResizeMessageBody } from "../types";
export declare const FIXEDPREFIX = "assets/";
export declare type S3Params = {
    getParams: S3GetParams;
    uploadParams: S3UploadParams;
};
export declare function generateS3Params({ url, size }: IImageResizeMessageBody, bucket: string): S3Params;
export declare function generateGetParams(url: string, bucket: string): S3GetParams;
export declare function getS3PrefixKeyFromUrl(url: string): Partial<S3GetParams>;
export declare function convertPrefixSize(prefix: any, size: any): string;
//# sourceMappingURL=urlSplitter.d.ts.map