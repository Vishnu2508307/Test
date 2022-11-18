Feature: Test manual grading feature from the instructor perspective

  Background:
    Given an account "Alice" is created
    And an ies account "Bob" is provisioned
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has registered "SCREEN_ONE" "INTERACTIVE" element to "SCREEN_ONE" student scope
    And "Alice" has created a "COMPONENT_ONE" component for the "SCREEN_ONE" interactive
    And "Alice" has created a "COMPONENT_TWO" component for the "SCREEN_ONE" interactive
    And "Alice" has set "COMPONENT_ONE" manual grading configurations with
      | maxScore | 50 |
    And "Alice" has created a scenario "CORRECT_SCENARIO_ONE" for the "SCREEN_ONE" interactive with
      | description       | a correct scenario   |
      | lifecycle         | INTERACTIVE_EVALUATE |
      | correctness       | correct              |
      | expected          | 8                    |
      | sourceId          | SCREEN_ONE           |
      | studentScopeURN   | SCREEN_ONE           |

  Scenario: It should not fetch deleted manual grading configuration
    Given "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"
    And "Alice" deletes "COMPONENT_ONE" manual grading configurations
    And the manual grading configuration is successfully deleted
    And "Alice" publishes "UNIT_ONE" activity to update "DEPLOYMENT_ONE"
    And "UNIT_ONE" activity is successfully published at "DEPLOYMENT_TWO"
    When "Alice" fetches manual grading configurations for deployment "DEPLOYMENT_TWO"
    Then no manual grading configuration are returned
    When "Alice" fetches enrolled student manual grade reports by activity with configurations
      | activityId  | UNIT_ONE   |
      | deploymentId | DEPLOYMENT_TWO |
    Then the manual grade report by activity is empty

  Scenario: It should allow an instructor to fetch all the manual grading component for a deployment
    Given "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"
    And "Bob" autoenroll to cohort "cohort"
    When "Alice" fetches manual grading configurations for deployment "DEPLOYMENT_ONE"
    Then one manual grading configuration is returned with values
      | componentId  | COMPONENT_ONE  |
      | maxScore     | 50             |
      | parentId     | SCREEN_ONE     |
      | parentType   | INTERACTIVE    |
      | deploymentId | DEPLOYMENT_ONE |
    When "Alice" fetches enrolled student manual grade report with configurations
      | componentId  | COMPONENT_ONE  |
      | parentId     | SCREEN_ONE     |
      | parentType   | INTERACTIVE    |
      | deploymentId | DEPLOYMENT_ONE |
    Then the manual grade report has one enrollment with
      | studentId | Bob           |
      | state     | NOT_ATTEMPTED |

  Scenario: It should allow an instructor to create a manual grade for a student
    Given "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"
    And "Bob" autoenroll to cohort "cohort"
    When instructor "Alice" fetches the score for "UNIT_ONE" activity for deployment "DEPLOYMENT_ONE", the score is
      | value  | 0             |
      | reason | NOT_ATTEMPTED |
    When "Alice" fetches the score for "LINEAR_ONE" pathway for deployment "DEPLOYMENT_ONE", the score is
      | value  | 0             |
      | reason | NOT_ATTEMPTED |
    Given "Bob" has set "SCREEN_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
    And "Bob" evaluates interactive "SCREEN_ONE" for deployment "DEPLOYMENT_ONE"
    And the evaluation result has
      | scenarioCorrectness  | correct        |
      | interactiveName      | SCREEN_ONE     |
      | deploymentName       | DEPLOYMENT_ONE |
      | interactiveCompleted | true           |
      | fired_scenarios      | 1              |
    When "Bob" fetches the score for "UNIT_ONE" activity for deployment "DEPLOYMENT_ONE", the score is
      | value  | 10     |
      | reason | SCORED |
    When "Bob" fetches the score for "LINEAR_ONE" pathway for deployment "DEPLOYMENT_ONE", the score is
      | value  | 10     |
      | reason | SCORED |

    When "Alice" fetches "Bob"'s score for "SCREEN_ONE" interactive for deployment "DEPLOYMENT_ONE", the score is
      | value  | 10     |
      | reason | SCORED |
    When "Alice" fetches enrolled student manual grade report with configurations
      | componentId  | COMPONENT_ONE  |
      | parentId     | SCREEN_ONE     |
      | parentType   | INTERACTIVE    |
      | deploymentId | DEPLOYMENT_ONE |
    Then the manual grade report has one enrollment with
      | studentId | Bob                 |
      | state     | INSTRUCTOR_UNSCORED |
    When "Alice" create a manual grade for component "COMPONENT_ONE" and deployment "DEPLOYMENT_ONE" with args
      | studentId | Bob            |
      | score     | 30.5           |
      | attemptId | LATEST_ATTEMPT |
      | operator  | SET            |
    Then the manual grade is successfully created with
      | instructorId | Alice |
      | score        | 30.5  |
    When "Alice" fetches enrolled student manual grade report grades with configurations
      | componentId  | COMPONENT_ONE  |
      | maxScore     | 50             |
      | parentId     | SCREEN_ONE     |
      | parentType   | INTERACTIVE    |
      | deploymentId | DEPLOYMENT_ONE |
    Then the manual grade report with grades has one enrollment with
      | studentId           | Bob               |
      | state               | INSTRUCTOR_SCORED |
      | grades              | 1                 |
      | gradedBy            | Alice             |
      | gradeScore          | 30.5              |
      | scenarioCorrectness | correct           |
    When "Bob" fetches the score for "UNIT_ONE" activity for deployment "DEPLOYMENT_ONE", the score is
      | value  | 40.5   |
      | reason | SCORED |
    When "Bob" fetches the score for "LINEAR_ONE" pathway for deployment "DEPLOYMENT_ONE", the score is
      | value  | 40.5   |
      | reason | SCORED |
    When "Alice" create a manual grade for component "COMPONENT_ONE" and deployment "DEPLOYMENT_ONE" with args
      | studentId | Bob            |
      | score     | 10.5           |
      | attemptId | LATEST_ATTEMPT |
      | operator  | SET            |
    Then the manual grade is successfully created with
      | instructorId | Alice |
      | score        | 10.5  |
    When "Bob" fetches the score for "UNIT_ONE" activity for deployment "DEPLOYMENT_ONE", the score is
      | value  | 20.5   |
      | reason | SCORED |
    When "Bob" fetches the score for "LINEAR_ONE" pathway for deployment "DEPLOYMENT_ONE", the score is
      | value  | 20.5   |
      | reason | SCORED |

    Scenario: It should create student score entries for a manual grade with operator add
      Given "Alice" has set "COMPONENT_TWO" manual grading configurations with
        | maxScore | 50 |
      # perform a duplication
      When "Alice" duplicates "UNIT_ONE" to project "TRO"
      Then the new copy "UNIT_ONE_COPY" is successfully created inside project "TRO"
      When "Alice" fetches the "UNIT_ONE_COPY" activity
      Then "UNIT_ONE_COPY" activity has a "LINEAR_ONE_COPY" pathway
      When "Alice" fetches the "LINEAR_ONE_COPY" pathway
      Then "LINEAR_ONE_COPY" pathway has walkable children
        | SCREEN_ONE_COPY |
      When "Alice" fetches the "SCREEN_ONE_COPY" interactive
      Then "SCREEN_ONE_COPY" interactive has components
        | COMPONENT_ONE_COPY |
        | COMPONENT_TWO_COPY |
      # add a component to create an activity change
      Given "Alice" has created a "COMPONENT_THREE" component for the "SCREEN_ONE_COPY" interactive
      And "Alice" has published "UNIT_ONE_COPY" activity to "DEPLOYMENT_ONE"
      And "Bob" autoenroll to cohort "cohort"
      When instructor "Alice" fetches the score for "UNIT_ONE_COPY" activity for deployment "DEPLOYMENT_ONE", the score is
        | value  | 0             |
        | reason | NOT_ATTEMPTED |
      When "Alice" fetches the score for "LINEAR_ONE_COPY" pathway for deployment "DEPLOYMENT_ONE", the score is
        | value  | 0             |
        | reason | NOT_ATTEMPTED |
      Given "Bob" has set "SCREEN_ONE_COPY" studentScope using element "SCREEN_ONE_COPY" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
      And "Bob" evaluates interactive "SCREEN_ONE_COPY" for deployment "DEPLOYMENT_ONE"
      And the evaluation result has
        | scenarioCorrectness  | correct         |
        | interactiveName      | SCREEN_ONE_COPY |
        | deploymentName       | DEPLOYMENT_ONE  |
        | interactiveCompleted | true            |
        | fired_scenarios      | 1               |
      When "Bob" fetches the score for "UNIT_ONE_COPY" activity for deployment "DEPLOYMENT_ONE", the score is
        | value  | 10     |
        | reason | SCORED |
      When "Bob" fetches the score for "LINEAR_ONE_COPY" pathway for deployment "DEPLOYMENT_ONE", the score is
        | value  | 10     |
        | reason | SCORED |
      When "Alice" fetches enrolled student manual grade report with configurations
        | componentId  | COMPONENT_ONE_COPY |
        | parentId     | SCREEN_ONE_COPY    |
        | parentType   | INTERACTIVE        |
        | deploymentId | DEPLOYMENT_ONE     |
      Then the manual grade report has one enrollment with
        | studentId | Bob                 |
        | state     | INSTRUCTOR_UNSCORED |
      When "Alice" fetches enrolled student manual grade reports by activity with configurations
        | activityId  | UNIT_ONE_COPY   |
        | deploymentId | DEPLOYMENT_ONE |
      Then the manual grade report by activity has one enrollment with
        | COMPONENT_ONE_COPY |
        | COMPONENT_TWO_COPY |
      When "Alice" fetches enrolled student manual grade reports by interactive with configurations
        | interactiveId | SCREEN_ONE_COPY |
        | deploymentId  | DEPLOYMENT_ONE  |
        | studentId     | Bob             |
      Then the manual grade report by interactive has one enrollment with
        | COMPONENT_ONE_COPY |
        | COMPONENT_TWO_COPY |
      When "Alice" create a manual grade for component "COMPONENT_ONE_COPY" and deployment "DEPLOYMENT_ONE" with args
        | studentId | Bob            |
        | score     | 30.5           |
        | attemptId | LATEST_ATTEMPT |
        | operator  | ADD            |
      Then the manual grade is successfully created with
        | instructorId | Alice |
        | score        | 30.5  |
      When "Alice" fetches enrolled student manual grade report grades with configurations
        | componentId  | COMPONENT_ONE_COPY |
        | maxScore     | 50                 |
        | parentId     | SCREEN_ONE_COPY    |
        | parentType   | INTERACTIVE        |
        | deploymentId | DEPLOYMENT_ONE     |
      Then the manual grade report with grades has one enrollment with
        | studentId           | Bob               |
        | state               | INSTRUCTOR_SCORED |
        | grades              | 1                 |
        | gradedBy            | Alice             |
        | gradeScore          | 30.5              |
        | scenarioCorrectness | correct           |
      When "Bob" fetches the score for "UNIT_ONE_COPY" activity for deployment "DEPLOYMENT_ONE", the score is
        | value  | 40.5   |
        | reason | SCORED |
      When "Bob" fetches the score for "LINEAR_ONE_COPY" pathway for deployment "DEPLOYMENT_ONE", the score is
        | value  | 40.5   |
        | reason | SCORED |
