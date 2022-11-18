@Learn
Feature: Fetch scope in Learn context

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "Course" inside project "TRO"
    And "Alice" has created a "Pathway" pathway for the "Course" activity
    And "Alice" has created a "Walkable1" interactive for the "Pathway" pathway
    And "Alice" has created a "Field" component for the "Walkable1" interactive
    And "Alice" has saved config for "Field" component
    """
    {"selection":["Answer 2"], "items":["Answer 2", "Choice 1"]}
    """
    And "Alice" has registered "Field" "COMPONENT" element to "Course" student scope
    And "Alice" has registered "Field" "COMPONENT" element to "Walkable1" student scope
    And "Alice" has created a "Walkable2" interactive for the "Pathway" pathway
    And "Alice" has created a "Field2" component for the "Walkable2" interactive
    And "Alice" has published "Course" activity to "CourseDeployment"

  Scenario: Scope should be initialized from the config and output schema
    When "Alice" sends a GraphQL message
     """
    {
      learn {
        cohort(cohortId: "${cohort_id}") {
          deployment(deploymentId: "${CourseDeployment_id}") {
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
                            "id": "${Walkable1_id}",
                            "scope": [
                              {
                                "sourceId": "${Field_id}",
                                "scopeURN": "${Walkable1_studentScope}",
                                "data": "{\"selection\":[\"Answer 2\"],\"options\":{\"foo\":\"default\"}}"
                              }
                            ]
                          }
                        }
                      ]
                    }
                  }
                ],
                "scope": [{
                  "sourceId": "${Field_id}",
                  "scopeURN": "${Course_studentScope}",
                  "data": "{\"selection\":[\"Answer 2\"],\"options\":{\"foo\":\"default\"}}"
                }]
              }
            }
          ]
        }
      }
    }
    """
