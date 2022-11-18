"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.handler = void 0;
const core_1 = require("./src/core");
const s3 = new core_1.S3();
const sns = new core_1.SNS();
const handler = async (event) => {
    var _a;
    console.debug(event.Records[0].Sns);
    const bucketName = (_a = event.Records[0].Sns.MessageAttributes['bucketName']) === null || _a === void 0 ? void 0 : _a.Value;
    const imageRecord = core_1.parseLambdaSnsResizeEvent(event)[0];
    const s3ImageParams = core_1.generateS3Params(imageRecord, bucketName);
    console.info('processing image', { bucket: s3ImageParams.getParams, url: imageRecord.url });
    try {
        const result = await processImage(s3ImageParams, imageRecord);
        const cleanedImageRecord = cleanImageRecord(imageRecord);
        await sns.publish({ ...cleanedImageRecord, ...result }, core_1.SnsTopics.SUCCESS);
    }
    catch (error) {
        console.info(error);
        await sns.publish({
            notificationId: imageRecord.notificationId,
            assetId: imageRecord.assetId,
            cause: error === null || error === void 0 ? void 0 : error.message,
            error: JSON.stringify(error),
        }, core_1.SnsTopics.ERROR);
    }
};
exports.handler = handler;
async function processImage(s3ImageParams, imageDetails) {
    try {
        const image = await s3.get(s3ImageParams.getParams);
        const resizedImage = await core_1.resizeImageToThreshold(image.Body, imageDetails);
        const imageMetaData = await core_1.getImageMetadata(resizedImage);
        const s3Result = await s3.put({
            ...s3ImageParams.uploadParams,
            body: resizedImage,
        });
        return {
            url: s3Result.Location.split(core_1.FIXEDPREFIX)[1],
            height: imageMetaData.height,
            width: imageMetaData.width,
        };
    }
    catch (err) {
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
//# sourceMappingURL=index.js.map