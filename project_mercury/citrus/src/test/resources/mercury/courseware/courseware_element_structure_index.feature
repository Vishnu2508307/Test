Feature: Fetch the courseware element structure flatten index for an element

  Background:
    Given an account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created a "WIDGET_ONE" component for the "SCREEN_ONE" interactive
    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created a "INPUT_ONE" component for the "SCREEN_TWO" interactive

  Scenario: It should allow a user with proper permission to fetch a courseware root element structure flatten index
    And an account "Bob" is created
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over workspace "one"
    When "Bob" sends a GraphQL message
    """
    {
      workspace(workspaceId: "${one_workspace_id}") {
        coursewareElementIndex(elementId: "${UNIT_ONE_id}", elementType: ACTIVITY) {
          elementId
          type
          parentId
          topParentId
        }
      }
    }
    """

    Then mercury should respond with a GraphQL response
    """
    {
      "workspace": {
        "coursewareElementIndex": [
          {
            "elementId": "${UNIT_ONE_id}",
            "type": "ACTIVITY",
            "parentId": null,
            "topParentId": "${UNIT_ONE_id}"
          },
          {
            "elementId": "${LINEAR_ONE_id}",
            "type": "PATHWAY",
            "parentId": "${UNIT_ONE_id}",
            "topParentId": "${UNIT_ONE_id}"
          },
          {
            "elementId": "${SCREEN_ONE_id}",
            "type": "INTERACTIVE",
            "parentId": "${LINEAR_ONE_id}",
            "topParentId": "${UNIT_ONE_id}"
          },
          {
            "elementId": "${WIDGET_ONE_id}",
            "type": "COMPONENT",
            "parentId": "${SCREEN_ONE_id}",
            "topParentId": "${UNIT_ONE_id}"
          },
          {
            "elementId": "${SCREEN_TWO_id}",
            "type": "INTERACTIVE",
            "parentId": "${LINEAR_ONE_id}",
            "topParentId": "${UNIT_ONE_id}"
          },
          {
            "elementId": "${INPUT_ONE_id}",
            "type": "COMPONENT",
            "parentId": "${SCREEN_TWO_id}",
            "topParentId": "${UNIT_ONE_id}"
          }
        ]
      }
    }
    """

  Scenario: It should not allow a user without proper permission to fetch a courseware root element structure flatten index
    Given an account "Bob" is created
    When "Bob" sends a GraphQL message
    """
    {
      workspace(workspaceId: "${one_workspace_id}") {
        coursewareElementIndex(elementId: "${UNIT_ONE_id}", elementType: ACTIVITY) {
          elementId
          type
          parentId
          topParentId
        }
      }
    }
    """

    Then GraphQL should respond with error code 403 and message "User does not have permissions to workspace"
