Feature: Actions feature

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort in workspace "one"
    And "Alice" has created a competency document "SKILLS" in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has registered "SCREEN_ONE" "INTERACTIVE" element to "SCREEN_ONE" student scope

  Scenario: It should allow to add a change scope action and this should change the scope value
    Given "Alice" has created a scenario "CORRECT_SCENARIO_ONE" for the "SCREEN_ONE" interactive with action "CHANGE_SCOPE"
      | description       | a correct scenario   |
      | lifecycle         | INTERACTIVE_EVALUATE |
      | correctness       | correct              |
      | expected          | 8                    |
      | sourceId          | SCREEN_ONE           |
      | studentScopeURN   | SCREEN_ONE           |
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"
    When "Alice" has set "SCREEN_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
    And "Alice" evaluates interactive "SCREEN_ONE" for deployment "DEPLOYMENT_ONE"
    Then the evaluation result has
      | scenarioCorrectness  | correct        |
      | interactiveName      | SCREEN_ONE     |
      | deploymentName       | DEPLOYMENT_ONE |
      | interactiveCompleted | false          |
      | fired_scenarios      | 1              |
    When "Alice" fetches scope entry for cohort in deployment "DEPLOYMENT_ONE"
    Then the following scope entry data is returned
        """
        {"selection":16,"options":{"foo":"bar"}}
        """

  Scenario: A screen should be able to change the scope of a parent activity via change scope action
    Given "Alice" has created a scenario "CORRECT_SCENARIO_ONE" for the "SCREEN_ONE" interactive with action "CHANGE_SCOPE"
      | description       | a correct scenario   |
      | lifecycle         | INTERACTIVE_EVALUATE |
      | correctness       | correct              |
      | expected          | 8                    |
      | sourceId          | SCREEN_ONE           |
      | studentScopeURN   | UNIT_ONE             |
    And "Alice" has registered "SCREEN_ONE" "INTERACTIVE" element to "UNIT_ONE" student scope
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"
    When "Alice" has set "SCREEN_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
    # data must be initialized first
    When "Alice" has set "UNIT_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
    And "Alice" evaluates interactive "SCREEN_ONE" for deployment "DEPLOYMENT_ONE"
    Then the evaluation result has
      | scenarioCorrectness  | correct        |
      | interactiveName      | SCREEN_ONE     |
      | deploymentName       | DEPLOYMENT_ONE |
      | interactiveCompleted | false          |
      | fired_scenarios      | 1              |
    When "Alice" fetches scope entry for cohort "cohort" deployment "DEPLOYMENT_ONE" and activity "UNIT_ONE"
    Then the following activity scope entry data is returned
        """
        {"selection":16,"options":{"foo":"bar"}}
        """

  Scenario: A screen should be able to change the scope of a parent activity via change scope action after plugin updated
    Given "Alice" has registered "SCREEN_ONE" "INTERACTIVE" element to "UNIT_ONE" student scope
    And "Alice" has saved config for "SCREEN_ONE" interactive "titleField"
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"
    Given "Alice" has changed the schema and published course plugin
    Given "Alice" has created a scenario "CORRECT_SCENARIO_ONE" for the "SCREEN_ONE" interactive with action "CHANGE_SCOPE" targeting the new property
      | description       | a correct scenario   |
      | lifecycle         | INTERACTIVE_EVALUATE |
      | correctness       | correct              |
      | expected          | 8                    |
      | sourceId          | SCREEN_ONE           |
      | studentScopeURN   | UNIT_ONE             |
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_TWO"
    # data must be initialized first
    When "Alice" fetches scope entry for cohort "cohort" deployment "DEPLOYMENT_TWO" and activity "UNIT_ONE"
    Then the following activity scope entry data is returned
        """
        {"selection":null,"options":{"foo":"default"}}
        """
    And "Alice" evaluates interactive "SCREEN_ONE" for deployment "DEPLOYMENT_TWO"
    Then the evaluation result has
      | scenarioCorrectness  | correct        |
      | interactiveName      | SCREEN_ONE     |
      | deploymentName       | DEPLOYMENT_TWO |
      | interactiveCompleted | false          |
      | fired_scenarios      | 1              |

