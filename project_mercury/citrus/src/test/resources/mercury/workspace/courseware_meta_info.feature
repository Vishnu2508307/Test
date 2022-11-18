Feature: Set and Fetch courseware meta info

  Background:
    Given an account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway

  Scenario: It should not allow a user without proper permission level to set meta info on a courseware element
    Given an account "Bob" is created
    When "Bob" set meta info on courseware "ACTIVITY" "UNIT_ONE" with key "totalScore" and value "5"
    Then "Bob" is not allow to set meta info on the courseware

  Scenario: It should allow a contributor to set meta info on a courseware element
    Given an account "Bob" is created
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over workspace "one"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    When "Bob" set meta info on courseware "ACTIVITY" "UNIT_ONE" with key "totalScore" and value "5"
    Then courseware "UNIT_ONE" has meta info key "totalScore" with value "5"

  Scenario: It should allow a reviewer to fetch meta info on a courseware element
    Given an account "Bob" is created
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over workspace "one"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    And "Alice" has set meta info on courseware "INTERACTIVE" "SCREEN_ONE" with key "totalScore" and value "5"
    When "Bob" fetches courseware "INTERACTIVE" "SCREEN_ONE" meta info "totalScore"
    Then the courseware meta info "totalScore" has value "5"

  Scenario: It should allow a user with proper permission to fetch the meta info and MGC count via graphql
    Given "Alice" has created a "COMPONENT_ONE" component for the "SCREEN_ONE" interactive
    And "Alice" has created a "COMPONENT_TWO" component for the "SCREEN_ONE" interactive
    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR_ONE" pathway
    And "Alice" has set "COMPONENT_ONE" manual grading configurations with
      | maxScore | 10 |
    And "Alice" has updated the "SCREEN_ONE" interactive config with "bar"
    And an account "Bob" is created
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over workspace "one"
    And "Alice" has set meta info on courseware "INTERACTIVE" "SCREEN_ONE" with key "totalScore" and value "15"
    And "Alice" has set meta info on courseware "INTERACTIVE" "SCREEN_TWO" with key "totalScore" and value "5"
    When "Bob" sends a GraphQL message
    """
    {
      workspace(workspaceId: "${one_workspace_id}") {
        coursewareActivity(activityId: "${UNIT_ONE_id}") {
          coursewarePathways(pathwayId: "${LINEAR_ONE_id}") {
            edges {
              node {
                id
                type
                coursewareWalkables {
                  edges {
                    node {
                      id
                      studentScopeURN
                      coursewareElementType
                      getManualGradingConfigurationsCount
                      coursewareMetaInfo(keys: ["totalScore"]) {
                        value
                      }
                      coursewareWalkableConfigurationFields(fieldNames: ["foo"]) {
                        fieldName
                        fieldValue
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
      "workspace": {
        "coursewareActivity": {
          "coursewarePathways": {
            "edges": [
              {
                "node": {
                  "id": "${LINEAR_ONE_id}",
                  "type": "LINEAR",
                  "coursewareWalkables": {
                    "edges": [
                      {
                        "node": {
                          "id": "${SCREEN_ONE_id}",
                          "studentScopeURN": "@notEmpty()@",
                          "coursewareElementType": "INTERACTIVE",
                          "getManualGradingConfigurationsCount": 1,
                          "coursewareMetaInfo": [{
                            "value": "15"
                          }],
                          "coursewareWalkableConfigurationFields": [
                            {
                              "fieldName": "foo",
                              "fieldValue": "bar"
                            }
                          ]
                        }
                      },
                      {
                        "node": {
                          "id": "${SCREEN_TWO_id}",
                          "studentScopeURN": "@notEmpty()@",
                          "coursewareElementType": "INTERACTIVE",
                          "getManualGradingConfigurationsCount": 0,
                          "coursewareMetaInfo": [{
                            "value": "5"
                          }],
                          "coursewareWalkableConfigurationFields": [
                            {
                              "fieldName": "foo",
                              "fieldValue": null
                            }
                          ]
                        }
                      }
                    ]
                  }
                }
              }
            ]
          }
        }
      }
    }
    """

  Scenario: It should not allow a user without proper permission to fetch the meta info and MGC count via graphql
    Given an account "Bob" is created
    When "Bob" sends a GraphQL message
    """
    {
      workspace(workspaceId: "${one_workspace_id}") {
        coursewareActivity(activityId: "${UNIT_ONE_id}") {
          coursewarePathways(pathwayId: "${LINEAR_ONE_id}") {
            edges {
              node {
                id
                type
                coursewareWalkables {
                  edges {
                    node {
                      id
                      studentScopeURN
                      coursewareElementType
                      getManualGradingConfigurationsCount
                      coursewareMetaInfo(keys: ["totalScore"]) {
                        value
                      }
                      coursewareWalkableConfigurationFields(fieldNames: ["foo"]) {
                        fieldName
                        fieldValue
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
    Then GraphQL should respond with error code 403 and message "User does not have permissions to workspace"

