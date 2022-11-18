@Learn
Feature: Fetch scope registry in Learn context

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

  Scenario: fetch scope registry for LearnerWalkable
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
                      scopeRegistry {
                        edges {
                          node {
                            deploymentId
                            changeId
                            elementId
                            scopeURN
                            elementType
                            pluginId
                            pluginVersion
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
      }
    }
    """
    Then mercury should respond with a GraphQL response
    """
    {
      "learn":{
        "cohort":{
          "deployment":[
            {
               "activity":{
                  "pathways":[
                     {
                        "walkables":{
                           "edges":[
                              {
                                 "node":{
                                    "id":"${Walkable1_id}",
                                    "scopeRegistry":{
                                       "edges":[
                                          {
                                             "node":{
                                                "deploymentId":"${CourseDeployment_id}",
                                                "changeId":"${CourseDeployment_change_id}",
                                                "elementId":"${Field_id}",
                                                "scopeURN":"${Walkable1_studentScope}",
                                                "elementType":"COMPONENT",
                                                "pluginId":"${plugin_id}",
                                                "pluginVersion":"1.*"
                                             }
                                          }
                                       ]
                                    }
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
