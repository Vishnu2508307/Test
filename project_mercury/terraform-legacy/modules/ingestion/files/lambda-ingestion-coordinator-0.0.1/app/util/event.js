"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.getQueueName = exports.isMessageExist = exports.getEventMessage = void 0;
const getEventMessage = (event) => {
    var _a;
    const message = (_a = event === null || event === void 0 ? void 0 : event.Records) === null || _a === void 0 ? void 0 : _a[0];
    console.info(message);
    return message;
};
exports.getEventMessage = getEventMessage;
const isMessageExist = (message) => !!(message === null || message === void 0 ? void 0 : message.body);
exports.isMessageExist = isMessageExist;
const getQueueName = (queueArn) => {
    const splitQueue = queueArn.split(':');
    return splitQueue[splitQueue.length - 1];
};
exports.getQueueName = getQueueName;
//# sourceMappingURL=event.js.map