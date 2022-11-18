"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.findExpiredTasks = exports.isRunningTasks = exports.isCancelTask = void 0;
const types_1 = require("../types");
const MAX_TASK_RUNTIME = 1000 * 60 * 60 * 6;
const isCancelTask = (task) => task === types_1.QueueTaskDefinition['ingestion-cancel-submit'];
exports.isCancelTask = isCancelTask;
const isRunningTasks = (tasks) => tasks.taskArns.length > 0;
exports.isRunningTasks = isRunningTasks;
const findExpiredTasks = (tasks) => {
    const nowInMs = new Date().valueOf();
    return tasks === null || tasks === void 0 ? void 0 : tasks.filter((task) => nowInMs - new Date(task.startedAt).valueOf() > MAX_TASK_RUNTIME);
};
exports.findExpiredTasks = findExpiredTasks;
//# sourceMappingURL=task.js.map