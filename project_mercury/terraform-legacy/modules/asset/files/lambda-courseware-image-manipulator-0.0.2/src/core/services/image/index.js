"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.getImageMetadata = exports.resizeImageToThreshold = void 0;
const sharp = require('sharp');
function resizeImageToThreshold(image, imageRecord) {
    const largerDimension = getLargerImageDimension(imageRecord);
    return sharp(image).resize({
        [largerDimension]: imageRecord.threshold,
    }).withMetadata().toBuffer();
}
exports.resizeImageToThreshold = resizeImageToThreshold;
const getLargerImageDimension = ({ originalHeight, originalWidth, }) => (originalHeight < originalWidth ? 'height' : 'width');
const getImageMetadata = (image) => sharp(image)
    .metadata()
    .then((metadata) => {
    return {
        width: metadata.width,
        height: metadata.height,
    };
});
exports.getImageMetadata = getImageMetadata;
//# sourceMappingURL=index.js.map