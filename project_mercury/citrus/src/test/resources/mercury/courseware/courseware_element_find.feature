Feature: Find a courseware element in a workspace

  Background:
    Given an account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has updated the "SCREEN_ONE" interactive config with "bar"

  Scenario: It should allow a user with reviewer permission to find a courseware element
    Given an account "Bob" is created
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over workspace "one"
    When "Bob" sends a GraphQL message
    """
    {
      workspace(workspaceId: "${one_workspace_id}") {
        id
        name
        description
        # fetch courseware element by id
        getCoursewareElement(elementId: "${SCREEN_ONE_id}") {
          elementId
          elementType
          configurationFields(fieldNames: ["foo"]) {
            fieldName
            fieldValue
          }
        }
      }
    }
    """
    Then mercury should respond with a GraphQL response
    """
    {
      "workspace": {
        "id": "${one_workspace_id}",
        "name": "one",
        "description": "@notEmpty()@",
        "getCoursewareElement": {
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
    }
    """

