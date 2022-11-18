"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.parseLambdaSnsResizeEvent = void 0;
const parseLambdaSnsResizeEvent = (event) => {
    var _a, _b, _c;
    if ((_c = (_b = (_a = event.Records) === null || _a === void 0 ? void 0 : _a[0]) === null || _b === void 0 ? void 0 : _b.Sns) === null || _c === void 0 ? void 0 : _c.Message) {
        return event.Records.map((record) => JSON.parse(record.Sns.Message));
    }
    return [];
};
exports.parseLambdaSnsResizeEvent = parseLambdaSnsResizeEvent;
//# sourceMappingURL=aws.js.map