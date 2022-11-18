Feature: Graph pathway flow and functionality

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "GRAPH" pathway named "GRAPH_ONE" for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "GRAPH_ONE" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "GRAPH_ONE" pathway
    And "Alice" has created a "SCREEN_THREE" interactive for the "GRAPH_ONE" pathway
    And "Alice" has registered "SCREEN_ONE" "INTERACTIVE" element to "SCREEN_ONE" student scope
    And "Alice" has registered "SCREEN_TWO" "INTERACTIVE" element to "SCREEN_TWO" student scope
    And "Alice" has registered "SCREEN_THREE" "INTERACTIVE" element to "SCREEN_THREE" student scope

  Scenario: It should return the configured starting walkable when starting a graph pathway
    Given "Alice" has created a scenario "SCENARIO_ONE" for the "SCREEN_ONE" interactive with "INTERACTIVE_COMPLETE_AND_GO_TO" action for
      | elementId       | SCREEN_THREE |
      | elementType     | INTERACTIVE |
      | expected        | 8           |
      | sourceId        | SCREEN_ONE  |
      | studentScopeURN | SCREEN_ONE  |
    And "Alice" has created a scenario "SCENARIO_TWO" for the "SCREEN_THREE" interactive with "INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE" action for
      | elementId       | SCREEN_THREE |
      | elementType     | INTERACTIVE  |
      | expected        | 8            |
      | sourceId        | SCREEN_THREE |
      | studentScopeURN | SCREEN_THREE |
    And "Alice" has configured "SCREEN_TWO" interactive as starting walkable for "GRAPH_ONE" graph pathway
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"
    When "Alice" fetches the walkables for the first pathway in cohort "cohort" and deployment "DEPLOYMENT_ONE"
    Then "SCREEN_TWO" walkable is supplied

  Scenario: It should return the first child walkable when starting walkable is not configured
    Given "Alice" has created a scenario "SCENARIO_ONE" for the "SCREEN_ONE" interactive with "INTERACTIVE_COMPLETE_AND_GO_TO" action for
      | elementId       | SCREEN_THREE |
      | elementType     | INTERACTIVE |
      | expected        | 8           |
      | sourceId        | SCREEN_ONE  |
      | studentScopeURN | SCREEN_ONE  |
    And "Alice" has created a scenario "SCENARIO_TWO" for the "SCREEN_THREE" interactive with "INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE" action for
      | elementId       | SCREEN_THREE |
      | elementType     | INTERACTIVE  |
      | expected        | 8            |
      | sourceId        | SCREEN_THREE |
      | studentScopeURN | SCREEN_THREE |
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"
    When "Alice" fetches the walkables for the first pathway in cohort "cohort" and deployment "DEPLOYMENT_ONE"
    Then "SCREEN_ONE" walkable is supplied

  Scenario: It should return the expected current walkable after an interactive complete and go to action was processed
    And "Alice" has created a scenario "SCENARIO_ONE" for the "SCREEN_ONE" interactive with "INTERACTIVE_COMPLETE_AND_GO_TO" action for
      | elementId       | SCREEN_THREE |
      | elementType     | INTERACTIVE |
      | expected        | 8           |
      | sourceId        | SCREEN_ONE  |
      | studentScopeURN | SCREEN_ONE  |
    And "Alice" has created a scenario "SCENARIO_TWO" for the "SCREEN_THREE" interactive with "INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE" action for
      | elementId       | SCREEN_THREE |
      | elementType     | INTERACTIVE  |
      | expected        | 8            |
      | sourceId        | SCREEN_THREE |
      | studentScopeURN | SCREEN_THREE |
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
    When "Alice" fetches the walkables for the first pathway in cohort "cohort" and deployment "DEPLOYMENT_ONE"
    Then "SCREEN_THREE" walkable is supplied
    Given "Alice" has set "SCREEN_THREE" studentScope using element "SCREEN_THREE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
    And "Alice" evaluates interactive "SCREEN_THREE" for deployment "DEPLOYMENT_ONE"
    And the evaluation result has
      | scenarioCorrectness  | correct         |
      | interactiveName      | SCREEN_THREE    |
      | deploymentName       | DEPLOYMENT_ONE  |
      | interactiveCompleted | true            |
      | fired_scenarios      | 1               |
    When "Alice" fetches the progress for the root activity "UNIT_ONE" for cohort "cohort" deployment "DEPLOYMENT_ONE"
    Then the root activity has completion
      | confidence | 1 |
      | value      | 1 |

    Scenario: It should never automatically complete a graph pathway 100%
      And "Alice" has created a scenario "SCENARIO_ONE" for the "SCREEN_ONE" interactive with "INTERACTIVE_COMPLETE_AND_GO_TO" action for
        | elementId       | SCREEN_THREE |
        | elementType     | INTERACTIVE |
        | expected        | 8           |
        | sourceId        | SCREEN_ONE  |
        | studentScopeURN | SCREEN_ONE  |
      And "Alice" has created a scenario "SCENARIO_TWO" for the "SCREEN_THREE" interactive with "INTERACTIVE_COMPLETE_AND_GO_TO" action for
        | elementId       | SCREEN_TWO   |
        | elementType     | INTERACTIVE  |
        | expected        | 8            |
        | sourceId        | SCREEN_THREE |
        | studentScopeURN | SCREEN_THREE |
      And "Alice" has created a scenario "SCENARIO_THREE" for the "SCREEN_TWO" interactive with "INTERACTIVE_COMPLETE_AND_GO_TO" action for
        | elementId       | SCREEN_ONE  |
        | elementType     | INTERACTIVE |
        | expected        | 8           |
        | sourceId        | SCREEN_TWO  |
        | studentScopeURN | SCREEN_TWO  |
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
      And "Alice" has set "SCREEN_THREE" studentScope using element "SCREEN_THREE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
      When "Alice" evaluates interactive "SCREEN_THREE" for deployment "DEPLOYMENT_ONE"
      Then the evaluation result has
        | scenarioCorrectness  | correct        |
        | interactiveName      | SCREEN_THREE   |
        | deploymentName       | DEPLOYMENT_ONE |
        | interactiveCompleted | true           |
        | fired_scenarios      | 1              |
      And "Alice" has set "SCREEN_TWO" studentScope using element "SCREEN_TWO" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
      When "Alice" evaluates interactive "SCREEN_TWO" for deployment "DEPLOYMENT_ONE"
      Then the evaluation result has
        | scenarioCorrectness  | correct        |
        | interactiveName      | SCREEN_TWO     |
        | deploymentName       | DEPLOYMENT_ONE |
        | interactiveCompleted | true           |
        | fired_scenarios      | 1              |
      When "Alice" fetches the progress for the root activity "UNIT_ONE" for cohort "cohort" deployment "DEPLOYMENT_ONE"
      Then the root activity has completion
        | confidence | 0.95 |
        | value      | 0.95 |

    Scenario: It should triggered a default action interactive repeat when there are no scenarios
      Given "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"
      When "Alice" evaluates interactive "SCREEN_ONE" for deployment "DEPLOYMENT_ONE"
      Then the evaluation result has
        | scenarioCorrectness  | correct            |
        | interactiveName      | SCREEN_ONE         |
        | deploymentName       | DEPLOYMENT_ONE     |
        | interactiveCompleted | false              |
        | fired_scenarios      | 0                  |
        | triggeredActionsSize | 1                  |
        | defaultProgression   | INTERACTIVE_REPEAT |

    Scenario: It should create a new attempt and scope when returned to an interactive by the pathway
      Given "Alice" has created a scenario "SCENARIO_ONE" for the "SCREEN_ONE" interactive with "INTERACTIVE_COMPLETE_AND_GO_TO" action for
        | elementId       | SCREEN_TWO  |
        | elementType     | INTERACTIVE |
        | correctness     | incorrect   |
        | expected        | 8           |
        | sourceId        | SCREEN_ONE  |
        | studentScopeURN | SCREEN_ONE  |
      And "Alice" has created a scenario "SCENARIO_TWO" for the "SCREEN_ONE" interactive with "INTERACTIVE_COMPLETE_AND_GO_TO" action for
        | elementId       | SCREEN_THREE  |
        | elementType     | INTERACTIVE   |
        | correctness     | correct       |
        | expected        | 9             |
        | sourceId        | SCREEN_ONE    |
        | studentScopeURN | SCREEN_ONE    |
      And "Alice" has created a scenario "SCENARIO_THREE" for the "SCREEN_TWO" interactive with "INTERACTIVE_COMPLETE_AND_GO_TO" action for
        | elementId       | SCREEN_ONE  |
        | elementType     | INTERACTIVE |
        | expected        | 7           |
        | sourceId        | SCREEN_TWO  |
        | studentScopeURN | SCREEN_TWO  |
      And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"
      And "Alice" has set "SCREEN_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
      When "Alice" evaluates interactive "SCREEN_ONE" for deployment "DEPLOYMENT_ONE"
      Then the evaluation result has
        | scenarioCorrectness  | incorrect      |
        | interactiveName      | SCREEN_ONE     |
        | deploymentName       | DEPLOYMENT_ONE |
        | interactiveCompleted | true           |
        | fired_scenarios      | 1              |
      And "Alice" has set "SCREEN_TWO" studentScope using element "SCREEN_TWO" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":7, "options":{"foo":"bar"}}
        """
      When "Alice" evaluates interactive "SCREEN_TWO" for deployment "DEPLOYMENT_ONE"
      Then the evaluation result has
        | scenarioCorrectness  | correct        |
        | interactiveName      | SCREEN_TWO     |
        | deploymentName       | DEPLOYMENT_ONE |
        | interactiveCompleted | true           |
        | fired_scenarios      | 1              |
      When "Alice" sends a GraphQL message
      """
      {
        learn {
          cohort(cohortId: "${cohort_id}") {
            deployment(deploymentId: "${DEPLOYMENT_ONE_id}") {
              activity {
                pathways {
                  walkables {
                    edges {
                      node {
                        id
                        scope {
                          sourceId
                          scopeURN
                          data
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
      """
        Then mercury should respond with a GraphQL response
      """
      {
        "learn": {
          "cohort": {
            "deployment": [
              {
                "activity": {
                  "pathways": [
                    {
                      "walkables": {
                        "edges": [
                          {
                            "node": {
                              "id": "${SCREEN_ONE_id}",
                              "scope": [
                                {
                                  "sourceId": "${SCREEN_ONE_id}",
                                  "scopeURN": "${SCREEN_ONE_studentScope}",
                                  "data": ""
                                }
                              ]
                            }
                          }
                        ]
                      }
                    }
                  ]
                }
              }
            ]
          }
        }
      }
      """
