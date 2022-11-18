# Citrus Integration Tests
This module uses the citrus framework with cucumber to test input/output on mercury.

## Running the test
1. Ensure the required database services are running.
2. To run tests:
- **Intellij**
1. Set `Working Directory` to root mercury directory (ex. 'C:\src\mercury') through `Edit Configurations...`
2. Execute the Main class
- **Gradle** 
1. Execute `./gradlew :citrus:integrationTest` or `./gradlew integrationTest` from the root folder. <p/>
To see more more details during tests execution use `--info` : `./gradlew :citrus:integrationTest --info`

### Running a single feature file
- **Intellij**
1. To run a single feature file within Intellij, right click on the file and select the run option. 
2. If the execution fails with an exception click on `edit configurations` and make sure that the glue path points to `mercury.glue`. 
3. Set `Working Directory` to root mercury directory (ex. 'C:\src\mercury') through `Edit Configurations...`
- **Gradle** 
1. Execute `./gradlew :citrus:integrationTest -Ptests=feature_file` from the root folder.
2. To run several specific tests, list feature files using comma: `./gradlew :citrus:integrationTest -Ptests=feature_file1,feature_file2`

### Running feature files in parallel
- **Gradle** 
1. Execute `./gradlew :citrus:integrationTest -Pparallel` from the root folder with default number of threads(= 6).
2. Pass `-PnumberOfThreads=3` parameter if you want to override the default number of threads.

## How to write cucumber scenarios
The following are just recommendations on how to keep the project clean and tidy. They can change in the future when our
knowledge of the frameworks deepens.

### Keep the steps in separate files
A scenario is composed by multiple steps. Steps might belong to different objects. A step could be re-used in more than
one scenario so this enforces re-usability.
### Managing state in a scenario
If a scenario requires two steps from different objects to share a state, manage it using dependency injection.
### Readability
Keep regular expression as simple as possible.

