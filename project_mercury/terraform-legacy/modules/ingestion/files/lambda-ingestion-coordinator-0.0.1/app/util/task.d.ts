import { ListTasksResponse, Tasks } from 'aws-sdk/clients/ecs';
export declare const isCancelTask: (task: string) => boolean;
export declare const isRunningTasks: (tasks: ListTasksResponse) => boolean;
export declare const findExpiredTasks: (tasks: Tasks) => Tasks;
//# sourceMappingURL=task.d.ts.map