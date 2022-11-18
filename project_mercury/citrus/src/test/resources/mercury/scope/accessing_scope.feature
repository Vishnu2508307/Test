Feature: It should allow to read the scope set by a previous screen

  Background:
    # prepare the structure
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR_ONE" pathway
    And "Alice" has registered "SCREEN_ONE" "INTERACTIVE" element to "SCREEN_ONE" student scope
    # register screen one to the unit activity so it is allowed to write scope data there
    And "Alice" has registered "SCREEN_ONE" "INTERACTIVE" element to "UNIT_ONE" student scope
    And "Alice" has registered "SCREEN_TWO" "INTERACTIVE" element to "SCREEN_TWO" student scope
    # register screen two to the unit activity so it is allowed to write scope data there
    And "Alice" has registered "SCREEN_TWO" "INTERACTIVE" element to "UNIT_ONE" student scope

  Scenario: It should use the scope data set in a previous screen when evaluating the following screen
    # create a scenario for screen one that will read the data from screen one
    Given "Alice" has created a scenario "CORRECT_SCENARIO_ONE" for the "SCREEN_ONE" interactive with
      | description       | a correct scenario   |
      | lifecycle         | INTERACTIVE_EVALUATE |
      | correctness       | correct              |
      | expected          | 8                    |
      | sourceId          | SCREEN_ONE           |
      | studentScopeURN   | SCREEN_ONE           |
    # create a scenario for screen two that will read the data from screen one
    And "Alice" has created a scenario "CORRECT_SCENARIO_TWO" for the "SCREEN_TWO" interactive with
      | description       | a correct scenario   |
      | lifecycle         | INTERACTIVE_EVALUATE |
      | correctness       | correct              |
      | expected          | 8                    |
      | sourceId          | SCREEN_ONE           |
      | studentScopeURN   | SCREEN_ONE           |
    # publish the activity
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"
    # set the scope data for screen one
    And "Alice" has set "SCREEN_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
    # evaluate the screen one
    When "Alice" evaluates interactive "SCREEN_ONE" for deployment "DEPLOYMENT_ONE"
    Then the evaluation result has
      | scenarioCorrectness  | correct        |
      | interactiveName      | SCREEN_ONE     |
      | deploymentName       | DEPLOYMENT_ONE |
      | interactiveCompleted | true           |
      | fired_scenarios      | 1              |
    # evaluate screen two
    When "Alice" evaluates interactive "SCREEN_TWO" for deployment "DEPLOYMENT_ONE"
    Then the evaluation result has
      | scenarioCorrectness  | correct        |
      | interactiveName      | SCREEN_TWO     |
      | deploymentName       | DEPLOYMENT_ONE |
      | interactiveCompleted | true           |
      | fired_scenarios      | 1              |

  Scenario: It should use the scope data set in a parent activity by a previous screen to evaluate the following screen
    # create a scenario for screen two that will read the data from unit one
    Given "Alice" has created a scenario "CORRECT_SCENARIO_TWO" for the "SCREEN_TWO" interactive with
      | description       | a correct scenario   |
      | lifecycle         | INTERACTIVE_EVALUATE |
      | correctness       | correct              |
      | expected          | 8                    |
      | sourceId          | SCREEN_ONE           |
      | studentScopeURN   | UNIT_ONE             |
    # publish the activity
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"
    # set the scope data for unit one
    And "Alice" has set "UNIT_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":{"foo":"bar"}}
        """
    # evaluate screen two
    When "Alice" evaluates interactive "SCREEN_TWO" for deployment "DEPLOYMENT_ONE"
    Then the evaluation result has
      | scenarioCorrectness  | correct        |
      | interactiveName      | SCREEN_TWO     |
      | deploymentName       | DEPLOYMENT_ONE |
      | interactiveCompleted | true           |
      | fired_scenarios      | 1              |

  Scenario: The following screen should be able to read the scope from a previous screen or a parent activity
    # publish the activity
    Given "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"
    # set the scope data for screen one
    And "Alice" has set "SCREEN_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":7, "options":null}
        """
    # set the scope data for unit one
    And "Alice" has set "UNIT_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
        """
        {"selection":8, "options":null}
        """
    # fetch the the scopes and check the values
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
                                "data": "{\"selection\":7, \"options\":null}"
                              }
                            ]
                          }
                        }
                      ]
                    }
                  }
                ],
                "scope": [
                  {
                    "sourceId": "${SCREEN_ONE_id}",
                    "scopeURN": "${UNIT_ONE_studentScope}",
                    "data": "{\"selection\":8, \"options\":null}"
                  },
                  {
                    "sourceId": "${SCREEN_TWO_id}",
                    "scopeURN": "${UNIT_ONE_studentScope}",
                    "data": ""
                  }
                ]
              }
            }
          ]
        }
      }
    }
    """