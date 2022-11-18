"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.convertPrefixSize = exports.getS3PrefixKeyFromUrl = exports.generateGetParams = exports.generateS3Params = exports.FIXEDPREFIX = void 0;
exports.FIXEDPREFIX = 'assets/';
function generateS3Params({ url, size }, bucket) {
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
exports.generateS3Params = generateS3Params;
function generateGetParams(url, bucket) {
    const { prefix, key } = getS3PrefixKeyFromUrl(url);
    return {
        bucket,
        prefix,
        key,
    };
}
exports.generateGetParams = generateGetParams;
function getS3PrefixKeyFromUrl(url) {
    const prefix = url.split('/');
    const key = prefix.pop();
    return {
        prefix: `${exports.FIXEDPREFIX}${prefix.join('/')}`,
        key,
    };
}
exports.getS3PrefixKeyFromUrl = getS3PrefixKeyFromUrl;
function convertPrefixSize(prefix, size) {
    const splitPrefix = prefix.split('/');
    splitPrefix.pop();
    return `${splitPrefix.join('/')}/${size}`;
}
exports.convertPrefixSize = convertPrefixSize;
//# sourceMappingURL=urlSplitter.js.map