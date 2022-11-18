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
exports.Ecs = void 0;
const AWS = __importStar(require("aws-sdk"));
const config_1 = require("../../../config");
class Ecs {
    constructor(ecs = new AWS.ECS({ apiVersion: '2014-11-13', region: config_1.AWS_REGION })) {
        this.ecs = ecs;
    }
    runTask(ingestionSummary, task) {
        const params = this.getRunTaskParams(ingestionSummary, task);
        return this.ecs.runTask(params).promise();
    }
    listTasks() {
        const params = {
            cluster: config_1.CLUSTER,
            launchType: config_1.LAUNCH_TYPE,
            maxResults: 50,
        };
        return this.ecs.listTasks(params).promise();
    }
    describeTasks(taskArns) {
        const params = {
            cluster: config_1.CLUSTER,
            tasks: taskArns,
            include: ['TAGS'],
        };
        return this.ecs.describeTasks(params).promise();
    }
    stopTask(taskArn) {
        const params = {
            cluster: config_1.CLUSTER,
            task: taskArn,
        };
        return this.ecs.stopTask(params).promise();
    }
    getRunTaskParams(ingestionPayload, task) {
        var _a;
        const parsedSummary = JSON.parse(ingestionPayload);
        return {
            cluster: config_1.CLUSTER,
            launchType: config_1.LAUNCH_TYPE,
            count: 1,
            taskDefinition: task,
            networkConfiguration: {
                awsvpcConfiguration: {
                    subnets: config_1.SUBNETS,
                },
            },
            overrides: {
                containerOverrides: [
                    {
                        name: task,
                        environment: [{ name: 'ingestionSummary', value: ingestionPayload }],
                    },
                ],
            },
            tags: [
                {
                    key: 'ingestionId',
                    value: (_a = parsedSummary === null || parsedSummary === void 0 ? void 0 : parsedSummary.ingestionSummary) === null || _a === void 0 ? void 0 : _a.id,
                },
            ],
        };
    }
}
exports.Ecs = Ecs;
//# sourceMappingURL=index.js.map