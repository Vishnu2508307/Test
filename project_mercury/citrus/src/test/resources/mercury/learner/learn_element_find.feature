Feature: Find a learner element in a deployment

  Background:
    Given an account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has saved configuration for "UNIT_ONE" activity
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has updated the "SCREEN_ONE" interactive config with "bar"
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"

  Scenario: User should be able to fetch learn element
    When "Alice" sends a GraphQL message
    """
    {
      learn {
        cohort(cohortId: "${cohort_id}") {
          id
          name
          deployment(deploymentId: "${DEPLOYMENT_ONE_id}") {
            # fetch learner element by id
            getLearnerElement(elementId: "${SCREEN_ONE_id}") {
              elementId
              elementType
              configurationFields(fieldNames: ["foo"]) {
                fieldName
                fieldValue
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
          "id": "${cohort_id}",
          "name": "Citrus cohort",
          "deployment": [
            {
              "getLearnerElement": {
                "elementId": "${SCREEN_ONE_id}",
                "elementType": "INTERACTIVE",
                "configurationFields": [
                  {
                    "fieldName": "foo",
                    "fieldValue": "bar"
                  }
                ]
              }
            }
          ]
        }
      }
    }
    """

