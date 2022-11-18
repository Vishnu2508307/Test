export declare type IngestionPayload = {
    bearerToken: string;
    ingestionSummary: IngestionSummary;
};
export declare type IngestionSummary = {
    id: string;
    projectId: string;
    workspaceId: string;
    rootElementId?: string;
    creatorId: string;
    courseName: string;
    configFields: Object[];
    ambrosiaUrl: string;
    status: IngestionStatus;
    ingestionStats: string[];
};
export declare enum IngestionStatus {
    UPLOADING = 0,
    ANALYZING = 1,
    IMPORTING = 2,
    FAILED = 3,
    COMPLETED = 4,
    UPLOADED = 5,
    UPLOAD_FAILED = 6,
    UPLOAD_CANCELLED = 7,
    DELETED = 8
}
export declare enum QueueTaskDefinition {
    'ingestion-adapter-epub-submit' = "adapter-epub",
    'ingestion-adapter-docx-submit' = "adapter-docx",
    'ingestion-ambrosia-submit' = "ingestion-ambrosia",
    'ingestion-cancel-submit' = "cancel"
}
//# sourceMappingURL=types.d.ts.map