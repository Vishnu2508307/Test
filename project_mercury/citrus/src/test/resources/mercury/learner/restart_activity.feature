Feature: Restart activity with Linear pathways

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "UNIT_PATHWAY" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "UNIT_PATHWAY" pathway
    And "Alice" has created a "LESSON_PATHWAY" pathway for the "LESSON_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LESSON_PATHWAY" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "LESSON_PATHWAY" pathway
    And "Alice" has registered "SCREEN_ONE" "INTERACTIVE" element to "SCREEN_ONE" student scope
    And "Alice" has registered "SCREEN_TWO" "INTERACTIVE" element to "SCREEN_TWO" student scope
    And "Alice" has created a scenario "CORRECT_SCENARIO_ONE" for the "SCREEN_ONE" interactive with
      | description       | a correct scenario   |
      | lifecycle         | INTERACTIVE_EVALUATE |
      | correctness       | correct              |
      | expected          | 8                    |
      | sourceId          | SCREEN_ONE           |
      | studentScopeURN   | SCREEN_ONE           |
    When "Alice" publishes "UNIT_ONE" activity
    Then "UNIT_ONE" activity is successfully published at "DEPLOYMENT_ONE"

  Scenario: User should be able to restart activity
    When "Alice" opens the "DEPLOYMENT_ONE" deployment
    Then "Alice" gets the "LESSON_ONE" activity with attempts
      | UNIT_ONE   | 1 |
      | LESSON_ONE | 1 |
    When "Alice" opens the "LESSON_ONE" activity for the "DEPLOYMENT_ONE" deployment
    Then "Alice" gets the "SCREEN_ONE" interactive with
      | ATTEMPT    | 1    |
      | SCOPE_DATA | EMPTY |
    When "Alice" has set "SCREEN_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
    And "Alice" evaluates interactive "SCREEN_ONE" for deployment "DEPLOYMENT_ONE"
    Then the evaluation result has
      | scenarioCorrectness  | correct        |
      | interactiveName      | SCREEN_ONE     |
      | deploymentName       | DEPLOYMENT_ONE |
      | interactiveCompleted | true           |
      | fired_scenarios      | 1              |
    When "Alice" opens the "LESSON_ONE" activity for the "DEPLOYMENT_ONE" deployment
    Then "Alice" gets the "SCREEN_TWO" interactive with
      | ATTEMPT    | 1     |
      | SCOPE_DATA | EMPTY |
    When "Alice" does evaluation for "DEPLOYMENT_ONE" and interactive "SCREEN_TWO"
    When "Alice" opens the "DEPLOYMENT_ONE" deployment
    Then "Alice" does not get an activity
    When "Alice" restarts the "UNIT_ONE" activity for "DEPLOYMENT_ONE" deployment
    When "Alice" opens the "DEPLOYMENT_ONE" deployment
    Then "Alice" gets the "LESSON_ONE" activity with attempts
      | UNIT_ONE   | 2 |
      | LESSON_ONE | 1 |
    When "Alice" opens the "LESSON_ONE" activity for the "DEPLOYMENT_ONE" deployment
    Then "Alice" gets the "SCREEN_ONE" interactive with
      | ATTEMPT    | 1     |
      | SCOPE_DATA | EMPTY |
    When "Alice" has set "SCREEN_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
    And "Alice" evaluates interactive "SCREEN_ONE" for deployment "DEPLOYMENT_ONE"
    Then the evaluation result has
      | scenarioCorrectness  | correct        |
      | interactiveName      | SCREEN_ONE     |
      | deploymentName       | DEPLOYMENT_ONE |
      | interactiveCompleted | true           |
      | fired_scenarios      | 1              |
    When "Alice" opens the "LESSON_ONE" activity for the "DEPLOYMENT_ONE" deployment
    Then "Alice" gets the "SCREEN_TWO" interactive
    When "Alice" does evaluation for "DEPLOYMENT_ONE" and interactive "SCREEN_TWO"
    When "Alice" opens the "DEPLOYMENT_ONE" deployment
    Then "Alice" does not get an activity
    When "Alice" restarts the "LESSON_ONE" activity for "DEPLOYMENT_ONE" deployment
    When "Alice" opens the "DEPLOYMENT_ONE" deployment
    Then "Alice" gets the "LESSON_ONE" activity with attempts
      | UNIT_ONE   | 2 |
      | LESSON_ONE | 2 |
    When "Alice" opens the "LESSON_ONE" activity for the "DEPLOYMENT_ONE" deployment
    Then "Alice" gets the "SCREEN_ONE" interactive with
      | ATTEMPT    | 1     |
      | SCOPE_DATA | EMPTY |
