"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    Object.defineProperty(o, k2, { enumerable: true, get: function() { return m[k]; } });
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || function (mod) {
    if (mod && mod.__esModule) return mod;
    var result = {};
    if (mod != null) for (var k in mod) if (k !== "default" && Object.prototype.hasOwnProperty.call(mod, k)) __createBinding(result, mod, k);
    __setModuleDefault(result, mod);
    return result;
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.S3 = void 0;
const AWS = __importStar(require("aws-sdk"));
const mime = __importStar(require("mime/lite"));
const path_1 = require("path");
class S3 {
    constructor(sdk = AWS) {
        this.sdk = sdk;
        this.get = ({ prefix, key, bucket }) => this.s3
            .getObject({
            Bucket: bucket,
            Key: path_1.join(prefix, key),
        })
            .promise();
        this.put = ({ body, prefix, key, bucket }) => this.s3
            .upload({
            Bucket: bucket,
            Body: body,
            Key: path_1.join(prefix, key),
            ContentType: mime.getType(key) || 'image',
        })
            .promise();
        this.s3 = new sdk.S3();
    }
}
exports.S3 = S3;
//# sourceMappingURL=s3.js.map