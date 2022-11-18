"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.handler = void 0;
const util_1 = require("./util");
const services_1 = require("./services");
const types_1 = require("./types");
const handler = async (event, context) => {
    const message = (0, util_1.getEventMessage)(event);
    const taskHandler = await new services_1.TaskHandler().init();
    console.log(`subnets: ${process.env.SUBNETS}`);
    if ((0, util_1.isMessageExist)(message)) {
        const taskDefinition = types_1.QueueTaskDefinition[(0, util_1.getQueueName)(message.eventSourceARN)];
        if ((0, util_1.isCancelTask)(taskDefinition)) {
            await taskHandler.stopTask(message.body);
        }
        else {
            await taskHandler.runTask(message.body, taskDefinition);
        }
    }
    await taskHandler.stopExpiredTasks();
};
exports.handler = handler;
//# sourceMappingURL=index.js.map