"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.TaskHandler = void 0;
const ecs_1 = require("./aws/ecs");
const util_1 = require("../util");
class TaskHandler {
    constructor(ecs = new ecs_1.Ecs()) {
        this.ecs = ecs;
        this.runningTasks = [];
        this.findTaskByIngestionId = (ingestionId) => { var _a; return (_a = this.runningTasks) === null || _a === void 0 ? void 0 : _a.find((task) => task.tags.find((tag) => tag.value === ingestionId)); };
    }
    async init() {
        const tasks = await this.ecs.listTasks();
        if ((0, util_1.isRunningTasks)(tasks)) {
            const runningTaskDescriptions = await this.ecs.describeTasks(tasks.taskArns);
            this.runningTasks = runningTaskDescriptions.tasks;
        }
        return this;
    }
    stopTask(taskPayload) {
        var _a;
        const ingestionPayload = JSON.parse(taskPayload);
        const ingestionId = (_a = ingestionPayload === null || ingestionPayload === void 0 ? void 0 : ingestionPayload.ingestionSummary) === null || _a === void 0 ? void 0 : _a.id;
        const foundTask = this.findTaskByIngestionId(ingestionId);
        if (foundTask) {
            console.log(`stopping task: ${foundTask}`);
            return this.ecs.stopTask(foundTask.taskArn);
        }
    }
    runTask(ingestionPayload, taskDefinition) {
        console.info(`running task: ${taskDefinition}`);
        return this.ecs.runTask(ingestionPayload, taskDefinition);
    }
    stopExpiredTasks() {
        const expiredTasks = (0, util_1.findExpiredTasks)(this.runningTasks);
        console.info(`stopping expired tasks: ${JSON.stringify(expiredTasks)}`);
        return Promise.all(expiredTasks.map((task) => this.ecs.stopTask(task.taskArn)));
    }
}
exports.TaskHandler = TaskHandler;
//# sourceMappingURL=taskHandler.js.map