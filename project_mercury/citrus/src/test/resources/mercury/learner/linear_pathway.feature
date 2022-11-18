Feature: Linear pathway flow and functionality

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway named "LINEAR_ONE" for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created a "SCREEN_THREE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has registered "SCREEN_ONE" "INTERACTIVE" element to "SCREEN_ONE" student scope
    And "Alice" has registered "SCREEN_TWO" "INTERACTIVE" element to "SCREEN_TWO" student scope
    And "Alice" has registered "SCREEN_THREE" "INTERACTIVE" element to "SCREEN_THREE" student scope

    Scenario: It should complete the pathway when an INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE progression type is triggered
      Given "Alice" has created a scenario "SCENARIO_ONE" for the "SCREEN_ONE" interactive with "INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE" action for
        | elementId       | SCREEN_ONE |
        | elementType     | INTERACTIVE  |
        | expected        | 8            |
        | sourceId        | SCREEN_ONE |
        | studentScopeURN | SCREEN_ONE |
      And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"
      And "Alice" has set "SCREEN_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
      When "Alice" evaluates interactive "SCREEN_ONE" for deployment "DEPLOYMENT_ONE"
      Then the evaluation result has
        | scenarioCorrectness  | correct        |
        | interactiveName      | SCREEN_ONE     |
        | deploymentName       | DEPLOYMENT_ONE |
        | interactiveCompleted | true           |
        | fired_scenarios      | 1              |
      When "Alice" fetches the progress for the root activity "UNIT_ONE" for cohort "cohort" deployment "DEPLOYMENT_ONE"
      Then the root activity has completion
        | confidence | 1 |
        | value      | 1 |
