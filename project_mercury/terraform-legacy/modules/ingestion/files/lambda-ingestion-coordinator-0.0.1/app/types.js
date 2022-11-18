"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.QueueTaskDefinition = exports.IngestionStatus = void 0;
var IngestionStatus;
(function (IngestionStatus) {
    IngestionStatus[IngestionStatus["UPLOADING"] = 0] = "UPLOADING";
    IngestionStatus[IngestionStatus["ANALYZING"] = 1] = "ANALYZING";
    IngestionStatus[IngestionStatus["IMPORTING"] = 2] = "IMPORTING";
    IngestionStatus[IngestionStatus["FAILED"] = 3] = "FAILED";
    IngestionStatus[IngestionStatus["COMPLETED"] = 4] = "COMPLETED";
    IngestionStatus[IngestionStatus["UPLOADED"] = 5] = "UPLOADED";
    IngestionStatus[IngestionStatus["UPLOAD_FAILED"] = 6] = "UPLOAD_FAILED";
    IngestionStatus[IngestionStatus["UPLOAD_CANCELLED"] = 7] = "UPLOAD_CANCELLED";
    IngestionStatus[IngestionStatus["DELETED"] = 8] = "DELETED";
})(IngestionStatus = exports.IngestionStatus || (exports.IngestionStatus = {}));
var QueueTaskDefinition;
(function (QueueTaskDefinition) {
    QueueTaskDefinition["ingestion-adapter-epub-submit"] = "adapter-epub";
    QueueTaskDefinition["ingestion-adapter-docx-submit"] = "adapter-docx";
    QueueTaskDefinition["ingestion-ambrosia-submit"] = "ingestion-ambrosia";
    QueueTaskDefinition["ingestion-cancel-submit"] = "cancel";
})(QueueTaskDefinition = exports.QueueTaskDefinition || (exports.QueueTaskDefinition = {}));
//# sourceMappingURL=types.js.map