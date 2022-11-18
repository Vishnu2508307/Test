package com.smartsparrow.export.data;

import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;

/**
 * Used whenever we get an ambrosia export request
 */
public class ExportRequest {

        protected UUID exportId;
        protected UUID elementId;
        protected CoursewareElementType elementType;
        protected UUID accountId;
        protected UUID projectId;
        protected UUID workspaceId;
        protected ExportStatus status;

        protected UUID rootElementId;
        protected String metadata;

        public UUID getExportId() {
                return exportId;
        }

        public ExportRequest setExportId(final UUID exportId) {
                this.exportId = exportId;
                return this;
        }

        public UUID getElementId() {
                return elementId;
        }

        public ExportRequest setElementId(final UUID elementId) {
                this.elementId = elementId;
                return this;
        }

        public CoursewareElementType getElementType() {
                return elementType;
        }

        public ExportRequest setElementType(final CoursewareElementType elementType) {
                this.elementType = elementType;
                return this;
        }

        public UUID getAccountId() {
                return accountId;
        }

        public ExportRequest setAccountId(final UUID accountId) {
                this.accountId = accountId;
                return this;
        }

        public UUID getProjectId() {
                return projectId;
        }

        public ExportRequest setProjectId(final UUID projectId) {
                this.projectId = projectId;
                return this;
        }

        public UUID getWorkspaceId() {
                return workspaceId;
        }

        public ExportRequest setWorkspaceId(final UUID workspaceId) {
                this.workspaceId = workspaceId;
                return this;
        }

        public ExportStatus getStatus() {
                return status;
        }

        public ExportRequest setStatus(final ExportStatus status) {
                this.status = status;
                return this;
        }

        public UUID getRootElementId() {
                return rootElementId;
        }

        public ExportRequest setRootElementId(final UUID rootElementId) {
                this.rootElementId = rootElementId;
                return this;
        }

        public String getMetadata() {
                return metadata;
        }

        public ExportRequest setMetadata(final String metadata) {
                this.metadata = metadata;
                return this;
        }
}
