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
exports.SNS = exports.SnsTopics = void 0;
const AWS = __importStar(require("aws-sdk"));
exports.SnsTopics = {
    SUCCESS: process.env.TOPIC_ARN_COURSEWARE_ASSET_RESIZE_RESULT,
    ERROR: process.env.TOPIC_ARN_COURSEWARE_ASSET_RESIZE_ERROR,
};
class SNS {
    constructor(sdk = AWS) {
        this.sdk = sdk;
        this.publish = (message, topic, attributes) => {
            console.debug('Sns message: ', {
                message: JSON.stringify(message),
            });
            return this.sns
                .publish({
                TopicArn: topic,
                Message: JSON.stringify(message),
                MessageAttributes: attributes,
            })
                .promise();
        };
        this.sns = new sdk.SNS();
    }
}
exports.SNS = SNS;
//# sourceMappingURL=sns.js.map