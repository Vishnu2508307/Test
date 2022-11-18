Feature: It should evaluate all the scenarios for an ACTIVITY_COMPLETE lifecycle

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "ACTIVITY_ONE" inside project "TRO"
    And "Alice" has created a "GRAPH" pathway named "PATHWAY_ONE" for the "ACTIVITY_ONE" activity
    And "Alice" has created a "INTERACTIVE_ONE" interactive for the "PATHWAY_ONE" pathway
    And "Alice" has created a "ACTIVITY_TWO" activity for the "PATHWAY_ONE" pathway
    And "Alice" has created a "INTERACTIVE_FOUR" interactive for the "PATHWAY_ONE" pathway
    And "Alice" has created a "GRAPH" pathway named "PATHWAY_TWO" for the "ACTIVITY_TWO" activity
    And "Alice" has created a "INTERACTIVE_TWO" interactive for the "PATHWAY_TWO" pathway
    And "Alice" has created a "INTERACTIVE_THREE" interactive for the "PATHWAY_TWO" pathway
    And "Alice" has registered "INTERACTIVE_ONE" "INTERACTIVE" element to "INTERACTIVE_ONE" student scope
    And "Alice" has registered "INTERACTIVE_TWO" "INTERACTIVE" element to "INTERACTIVE_TWO" student scope
    And "Alice" has registered "INTERACTIVE_THREE" "INTERACTIVE" element to "INTERACTIVE_THREE" student scope
    And "Alice" has registered "INTERACTIVE_FOUR" "INTERACTIVE" element to "INTERACTIVE_FOUR" student scope
    And "Alice" has registered "ACTIVITY_TWO" "ACTIVITY" element to "ACTIVITY_TWO" student scope

  Scenario: It should return the expected walkable after an activity complete and go to action was processed
    And "Alice" has created a scenario "SCENARIO_ONE" for the "INTERACTIVE_ONE" interactive with "INTERACTIVE_COMPLETE_AND_GO_TO" action for
      | elementId       | ACTIVITY_TWO    |
      | elementType     | ACTIVITY        |
      | expected        | 8               |
      | sourceId        | INTERACTIVE_ONE |
      | studentScopeURN | INTERACTIVE_ONE |
    And "Alice" has created a scenario "SCENARIO_TWO" for the "INTERACTIVE_TWO" interactive with "INTERACTIVE_COMPLETE_AND_GO_TO" action for
      | elementId       | INTERACTIVE_THREE |
      | elementType     | INTERACTIVE       |
      | expected        | 8                 |
      | sourceId        | INTERACTIVE_TWO   |
      | studentScopeURN | INTERACTIVE_TWO   |
    And "Alice" has created a scenario "SCENARIO_THREE" for the "INTERACTIVE_THREE" interactive with "INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE" action for
      | elementId       | INTERACTIVE_FOUR  |
      | elementType     | INTERACTIVE       |
      | expected        | 8                 |
      | sourceId        | INTERACTIVE_THREE |
      | studentScopeURN | INTERACTIVE_THREE |
    And "Alice" has created a scenario "SCENARIO_FOUR" for the "ACTIVITY_TWO" activity with "ACTIVITY_COMPLETE_AND_GO_TO" action for
      | elementId       | INTERACTIVE_FOUR |
      | elementType     | INTERACTIVE      |
      | expected        | 8                |
      | sourceId        | ACTIVITY_TWO     |
      | studentScopeURN | ACTIVITY_TWO     |
    And "Alice" has created a scenario "SCENARIO_FIVE" for the "INTERACTIVE_FOUR" interactive with "INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE" action for
      | elementId       | INTERACTIVE_FOUR |
      | elementType     | INTERACTIVE      |
      | expected        | 8                |
      | sourceId        | INTERACTIVE_FOUR |
      | studentScopeURN | INTERACTIVE_FOUR |
    And "Alice" has published "ACTIVITY_ONE" activity to "DEPLOYMENT_ONE"
    When "Alice" fetches the walkables for the first pathway in cohort "cohort" and deployment "DEPLOYMENT_ONE"
    Then "INTERACTIVE_ONE" walkable is supplied
    And "Alice" has set "INTERACTIVE_ONE" studentScope using element "INTERACTIVE_ONE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
    When "Alice" evaluates interactive "INTERACTIVE_ONE" for deployment "DEPLOYMENT_ONE"
    Then the evaluation result has
      | scenarioCorrectness  | correct         |
      | interactiveName      | INTERACTIVE_ONE |
      | deploymentName       | DEPLOYMENT_ONE  |
      | interactiveCompleted | true            |
      | fired_scenarios      | 1               |
    When "Alice" fetches the completed walkable history for "PATHWAY_ONE" pathway on deployment "DEPLOYMENT_ONE"
    Then the following completed walkables are returned
      | INTERACTIVE_ONE |
    When "Alice" fetches the walkables for the first pathway in cohort "cohort" and deployment "DEPLOYMENT_ONE"
    Then "ACTIVITY_TWO" walkable is supplied
    #When we are in Activity then the learnspace will display the first available screen
    Given "Alice" has set "INTERACTIVE_TWO" studentScope using element "INTERACTIVE_TWO" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
    And "Alice" evaluates interactive "INTERACTIVE_TWO" for deployment "DEPLOYMENT_ONE"
    And the evaluation result has
      | scenarioCorrectness  | correct         |
      | interactiveName      | INTERACTIVE_TWO |
      | deploymentName       | DEPLOYMENT_ONE  |
      | interactiveCompleted | true            |
      | fired_scenarios      | 1               |
    When "Alice" fetches the walkables for the "ACTIVITY_TWO" activity and "PATHWAY_TWO" pathway in cohort "cohort" and deployment "DEPLOYMENT_ONE"
    Then "INTERACTIVE_THREE" walkable is supplied
    Given "Alice" has set "INTERACTIVE_THREE" studentScope using element "INTERACTIVE_THREE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
    And "Alice" evaluates interactive "INTERACTIVE_THREE" for deployment "DEPLOYMENT_ONE"
    And the evaluation result has
      | scenarioCorrectness  | correct           |
      | interactiveName      | INTERACTIVE_THREE |
      | deploymentName       | DEPLOYMENT_ONE    |
      | interactiveCompleted | true              |
      | fired_scenarios      | 2                 |
    When "Alice" fetches the walkables for the first pathway in cohort "cohort" and deployment "DEPLOYMENT_ONE"
    Then "INTERACTIVE_FOUR" walkable is supplied
    Given "Alice" has set "INTERACTIVE_FOUR" studentScope using element "INTERACTIVE_FOUR" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
    And "Alice" evaluates interactive "INTERACTIVE_FOUR" for deployment "DEPLOYMENT_ONE"
    And the evaluation result has
      | scenarioCorrectness  | correct          |
      | interactiveName      | INTERACTIVE_FOUR |
      | deploymentName       | DEPLOYMENT_ONE   |
      | interactiveCompleted | true             |
      | fired_scenarios      | 1                |
    When "Alice" fetches the completed walkable history for "PATHWAY_ONE" pathway on deployment "DEPLOYMENT_ONE"
    Then the following completed walkables are returned
      | INTERACTIVE_FOUR |
      | INTERACTIVE_ONE  |
    When "Alice" fetches the progress for the root activity "ACTIVITY_ONE" for cohort "cohort" deployment "DEPLOYMENT_ONE"
    Then the root activity has completion
      | confidence | 1 |
      | value      | 1 |

  Scenario: It should return the expected walkable after an activity complete and pathway complete action was processed
    And "Alice" has created a scenario "SCENARIO_ONE" for the "INTERACTIVE_ONE" interactive with "INTERACTIVE_COMPLETE_AND_GO_TO" action for
      | elementId       | ACTIVITY_TWO    |
      | elementType     | ACTIVITY        |
      | expected        | 8               |
      | sourceId        | INTERACTIVE_ONE |
      | studentScopeURN | INTERACTIVE_ONE |
    And "Alice" has created a scenario "SCENARIO_TWO" for the "INTERACTIVE_TWO" interactive with "INTERACTIVE_COMPLETE_AND_GO_TO" action for
      | elementId       | INTERACTIVE_THREE |
      | elementType     | INTERACTIVE       |
      | expected        | 8                 |
      | sourceId        | INTERACTIVE_TWO   |
      | studentScopeURN | INTERACTIVE_TWO   |
    And "Alice" has created a scenario "SCENARIO_THREE" for the "INTERACTIVE_THREE" interactive with "INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE" action for
      | elementId       | INTERACTIVE_FOUR  |
      | elementType     | INTERACTIVE       |
      | expected        | 8                 |
      | sourceId        | INTERACTIVE_THREE |
      | studentScopeURN | INTERACTIVE_THREE |
    And "Alice" has created a scenario "SCENARIO_FOUR" for the "ACTIVITY_TWO" activity with "ACTIVITY_COMPLETE_AND_PATHWAY_COMPLETE" action for
      | elementId       | ACTIVITY_TWO |
      | elementType     | ACTIVITY     |
      | expected        | 8            |
      | sourceId        | ACTIVITY_TWO |
      | studentScopeURN | ACTIVITY_TWO |
    And "Alice" has published "ACTIVITY_ONE" activity to "DEPLOYMENT_ONE"
    When "Alice" fetches the walkables for the first pathway in cohort "cohort" and deployment "DEPLOYMENT_ONE"
    Then "INTERACTIVE_ONE" walkable is supplied
    And "Alice" has set "INTERACTIVE_ONE" studentScope using element "INTERACTIVE_ONE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
    When "Alice" evaluates interactive "INTERACTIVE_ONE" for deployment "DEPLOYMENT_ONE"
    Then the evaluation result has
      | scenarioCorrectness  | correct         |
      | interactiveName      | INTERACTIVE_ONE |
      | deploymentName       | DEPLOYMENT_ONE  |
      | interactiveCompleted | true            |
      | fired_scenarios      | 1               |
    When "Alice" fetches the completed walkable history for "PATHWAY_ONE" pathway on deployment "DEPLOYMENT_ONE"
    Then the following completed walkables are returned
      | INTERACTIVE_ONE |
    When "Alice" fetches the walkables for the first pathway in cohort "cohort" and deployment "DEPLOYMENT_ONE"
    Then "ACTIVITY_TWO" walkable is supplied
    #When we are in Activity then the learnspace will display the first available screen
    Given "Alice" has set "INTERACTIVE_TWO" studentScope using element "INTERACTIVE_TWO" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
    And "Alice" evaluates interactive "INTERACTIVE_TWO" for deployment "DEPLOYMENT_ONE"
    And the evaluation result has
      | scenarioCorrectness  | correct         |
      | interactiveName      | INTERACTIVE_TWO |
      | deploymentName       | DEPLOYMENT_ONE  |
      | interactiveCompleted | true            |
      | fired_scenarios      | 1               |
    When "Alice" fetches the walkables for the "ACTIVITY_TWO" activity and "PATHWAY_TWO" pathway in cohort "cohort" and deployment "DEPLOYMENT_ONE"
    Then "INTERACTIVE_THREE" walkable is supplied
    Given "Alice" has set "INTERACTIVE_THREE" studentScope using element "INTERACTIVE_THREE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
    And "Alice" evaluates interactive "INTERACTIVE_THREE" for deployment "DEPLOYMENT_ONE"
    And the evaluation result has
      | scenarioCorrectness  | correct           |
      | interactiveName      | INTERACTIVE_THREE |
      | deploymentName       | DEPLOYMENT_ONE    |
      | interactiveCompleted | true              |
      | fired_scenarios      | 2                 |
    When "Alice" fetches the walkables for the first pathway in cohort "cohort" and deployment "DEPLOYMENT_ONE"
    Then "ACTIVITY_TWO" walkable is supplied
    When "Alice" fetches the progress for the root activity "ACTIVITY_ONE" for cohort "cohort" deployment "DEPLOYMENT_ONE"
    Then the root activity has completion
      | confidence | 1 |
      | value      | 1 |

  Scenario: It should return the expected walkable after an activity repeat action was processed
    And "Alice" has created a scenario "SCENARIO_ONE" for the "INTERACTIVE_ONE" interactive with "INTERACTIVE_COMPLETE_AND_GO_TO" action for
      | elementId       | ACTIVITY_TWO    |
      | elementType     | ACTIVITY        |
      | expected        | 8               |
      | sourceId        | INTERACTIVE_ONE |
      | studentScopeURN | INTERACTIVE_ONE |
    And "Alice" has created a scenario "SCENARIO_TWO" for the "INTERACTIVE_TWO" interactive with "INTERACTIVE_COMPLETE_AND_GO_TO" action for
      | elementId       | INTERACTIVE_THREE |
      | elementType     | INTERACTIVE       |
      | expected        | 8                 |
      | sourceId        | INTERACTIVE_TWO   |
      | studentScopeURN | INTERACTIVE_TWO   |
    And "Alice" has created a scenario "SCENARIO_THREE" for the "INTERACTIVE_THREE" interactive with "INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE" action for
      | elementId       | INTERACTIVE_FOUR  |
      | elementType     | INTERACTIVE       |
      | expected        | 8                 |
      | sourceId        | INTERACTIVE_THREE |
      | studentScopeURN | INTERACTIVE_THREE |
    And "Alice" has created a scenario "SCENARIO_FOUR" for the "ACTIVITY_TWO" activity with "ACTIVITY_REPEAT" action for
      | elementId       | ACTIVITY_TWO |
      | elementType     | ACTIVITY     |
      | expected        | 8            |
      | sourceId        | ACTIVITY_TWO |
      | studentScopeURN | ACTIVITY_TWO |
    And "Alice" has published "ACTIVITY_ONE" activity to "DEPLOYMENT_ONE"
    When "Alice" fetches the walkables for the first pathway in cohort "cohort" and deployment "DEPLOYMENT_ONE"
    Then "INTERACTIVE_ONE" walkable is supplied
    And "Alice" has set "INTERACTIVE_ONE" studentScope using element "INTERACTIVE_ONE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
    When "Alice" evaluates interactive "INTERACTIVE_ONE" for deployment "DEPLOYMENT_ONE"
    Then the evaluation result has
      | scenarioCorrectness  | correct         |
      | interactiveName      | INTERACTIVE_ONE |
      | deploymentName       | DEPLOYMENT_ONE  |
      | interactiveCompleted | true            |
      | fired_scenarios      | 1               |
    When "Alice" fetches the completed walkable history for "PATHWAY_ONE" pathway on deployment "DEPLOYMENT_ONE"
    Then the following completed walkables are returned
      | INTERACTIVE_ONE |
    When "Alice" fetches the walkables for the first pathway in cohort "cohort" and deployment "DEPLOYMENT_ONE"
    Then "ACTIVITY_TWO" walkable is supplied
    #When we are in Activity then the learnspace will display the first available screen
    Given "Alice" has set "INTERACTIVE_TWO" studentScope using element "INTERACTIVE_TWO" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
    And "Alice" evaluates interactive "INTERACTIVE_TWO" for deployment "DEPLOYMENT_ONE"
    And the evaluation result has
      | scenarioCorrectness  | correct         |
      | interactiveName      | INTERACTIVE_TWO |
      | deploymentName       | DEPLOYMENT_ONE  |
      | interactiveCompleted | true            |
      | fired_scenarios      | 1               |
    When "Alice" fetches the completed walkable history for "PATHWAY_ONE" pathway on deployment "DEPLOYMENT_ONE"
    Then the following completed walkables are returned
      | INTERACTIVE_ONE |
    When "Alice" fetches the walkables for the "ACTIVITY_TWO" activity and "PATHWAY_TWO" pathway in cohort "cohort" and deployment "DEPLOYMENT_ONE"
    Then "INTERACTIVE_THREE" walkable is supplied
    Given "Alice" has set "INTERACTIVE_THREE" studentScope using element "INTERACTIVE_THREE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
    And "Alice" evaluates interactive "INTERACTIVE_THREE" for deployment "DEPLOYMENT_ONE"
    And the evaluation result has
      | scenarioCorrectness  | correct           |
      | interactiveName      | INTERACTIVE_THREE |
      | deploymentName       | DEPLOYMENT_ONE    |
      | interactiveCompleted | true              |
      | fired_scenarios      | 2                 |
    When "Alice" fetches the walkables for the first pathway in cohort "cohort" and deployment "DEPLOYMENT_ONE"
    Then "ACTIVITY_TWO" walkable is supplied
    When "Alice" fetches the walkables for the "ACTIVITY_TWO" activity and "PATHWAY_TWO" pathway in cohort "cohort" and deployment "DEPLOYMENT_ONE"
    Then "INTERACTIVE_TWO" walkable is supplied
