Feature: It should allow a student to set the a scope value with valid data

  Background:
    Given a workspace account "Alice" is created
    And an ies account "Bob" is provisioned
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR_ONE" pathway
    And "Alice" has registered "SCREEN_ONE" "INTERACTIVE" element to "UNIT_ONE" student scope
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"

  Scenario: It should not allow the student to set the scope with invalid data
    When "Bob" sets "UNIT_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
    """
    {"selection":"A", "options":{"foo":"bar"}, "invalidProperty":"lol"}
    """
    Then the student scope is not set due to invalid data

  Scenario: It should not allow the student to set the scope if the element was not registered
    When "Bob" sets "UNIT_ONE" studentScope using element "SCREEN_TWO" in deployment "DEPLOYMENT_ONE" with data
    """
    {"selection":"A", "options":{"foo":"bar"}}
    """
    Then the "UNIT_ONE" student scope is not set due to element "SCREEN_TWO" not registered

  Scenario: It should allow the student to set the scope with valid data
    When "Bob" sets "UNIT_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
    """
    {"selection":"A", "options":{"foo":"bar"}}
    """
    Then the student scope is successfully set with data
    """
    {"selection":"A", "options":{"foo":"bar"}}
    """

  Scenario: It should allow the student to set the scope with valid data multiple times
    When "Bob" sets "UNIT_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
    """
    {"selection":"A", "options":null}
    """
    Then the student scope is successfully set with data
    """
    {"selection":"A", "options":null}
    """
    When "Bob" sets "UNIT_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
    """
    {"selection":"B", "options":null}
    """
    Then the student scope is successfully set with data
    """
    {"selection":"B", "options":null}
    """
    When "Bob" sets "UNIT_ONE" studentScope using element "SCREEN_ONE" in deployment "DEPLOYMENT_ONE" with data
    """
    {"selection":"C", "options":null}
    """
    Then the student scope is successfully set with data
    """
    {"selection":"C", "options":null}
    """
    When "Bob" sends a GraphQL message
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
                    "data": "{\"selection\":\"C\", \"options\":null}"
                  }
                ]
              }
            }
          ]
        }
      }
    }
    """


