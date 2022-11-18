@Learn
Feature: Fetch single walkable and components in Learn context for a LINEAR pathway

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "Course" inside project "TRO"
    And "Alice" has created a "Pathway" pathway for the "Course" activity
    And "Alice" has created a "Walkable1" interactive for the "Pathway" pathway
    And "Alice" has saved config for "Walkable1" interactive "interactive config"
    And "Alice" has created a "Walkable2" activity for the "Pathway" pathway
    And "Alice" has saved configuration for "Walkable2" activity
    And "Alice" has created a "Field" component for the "Walkable1" interactive
    And "Alice" has saved config for "Field" component "component config"
    And "Alice" has published "Course" activity to "CourseDeployment"

  Scenario: User should be able to fetch learn activity through RTM
    When "Alice" sends a GraphQL message
     """
    {
      learn {
        cohort(cohortId: "${cohort_id}") {
          deployment(deploymentId: "${CourseDeployment_id}") {
            activity {
              pathways {
                id
                walkables {
                  edges {
                    node {
                      id
                      elementType
                      config
                      configurationFields(fieldNames: ["foo"]) {
                        fieldName
                        fieldValue
                      }
                      plugin {
                        entryPoints {
                          entryPointData
                          entryPointPath
                          contentType
                          context
                        }
                        manifest {
                          configurationSchema
                        }
                      }
                      components {
                        id
                        config
                        plugin {
                          entryPoints {
                            entryPointData
                            entryPointPath
                            contentType
                            context
                          }
                          manifest {
                            configurationSchema
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
      "learn": {
        "cohort": {
          "deployment": [
            {
              "activity": {
                "pathways": [
                  {
                    "id": "${Pathway_id}",
                    "walkables": {
                      "edges": [
                        {
                          "node": {
                            "id": "${Walkable1_id}",
                            "config": "@notEmpty()@",
                            "configurationFields": [
                              {
                                "fieldName": "foo",
                                "fieldValue": "@notEmpty()@"
                              }
                            ],
                            "elementType": "INTERACTIVE",
                            "plugin": {
                              "entryPoints": [
                                {
                                  "entryPointData": "some random content",
                                  "entryPointPath": "@notEmpty()@",
                                  "contentType": "javascript",
                                  "context": "LEARNER"
                                }
                              ],
                              "manifest": {
                                "configurationSchema": "@notEmpty()@"
                              }
                            },
                            "components": [
                              {
                                "id": "${Field_id}",
                                "config": "component config",
                                "plugin": {
                                  "entryPoints": [
                                    {
                                      "entryPointData": "some random content",
                                      "entryPointPath": "@notEmpty()@",
                                      "contentType": "javascript",
                                      "context": "LEARNER"
                                    }
                                  ],
                                  "manifest": {
                                    "configurationSchema": "@notEmpty()@"
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
          ]
        }
      }
    }
    """
