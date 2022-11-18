import * as AWS from 'aws-sdk';
import { DescribeTasksResponse, StopTaskResponse, ListTasksResponse, RunTaskResponse } from 'aws-sdk/clients/ecs';
export declare class Ecs {
    ecs: AWS.ECS;
    constructor(ecs?: AWS.ECS);
    runTask(ingestionSummary: string, task: string): Promise<RunTaskResponse>;
    listTasks(): Promise<ListTasksResponse>;
    describeTasks(taskArns: string[]): Promise<DescribeTasksResponse>;
    stopTask(taskArn: string): Promise<StopTaskResponse>;
    private getRunTaskParams;
}
//# sourceMappingURL=index.d.ts.map