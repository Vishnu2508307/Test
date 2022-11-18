import { Tasks, RunTaskResponse, StopTaskResponse, Task } from 'aws-sdk/clients/ecs';
import { QueueTaskDefinition } from '../types';
import { Ecs } from './aws/ecs';
export declare class TaskHandler {
    ecs: Ecs;
    runningTasks: Tasks;
    constructor(ecs?: Ecs);
    init(): Promise<TaskHandler>;
    stopTask(taskPayload: string): Promise<StopTaskResponse | void>;
    runTask(ingestionPayload: string, taskDefinition: QueueTaskDefinition): Promise<RunTaskResponse>;
    findTaskByIngestionId: (ingestionId: string) => Task | void;
    stopExpiredTasks(): Promise<StopTaskResponse[]>;
}
//# sourceMappingURL=taskHandler.d.ts.map