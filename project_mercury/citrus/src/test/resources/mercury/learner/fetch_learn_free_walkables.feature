@Learn
Feature: Fetch all walkables and components in Learn context on a FREE pathway

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "Course" inside project "TRO"
    And "Alice" has created a "FREE" pathway named "Pathway" for the "Course" activity
    And "Alice" has created a "Walkable1" interactive for the "Pathway" pathway
    And "Alice" has created a "Walkable2" interactive for the "Pathway" pathway
    And "Alice" has created a "Walkable3" interactive for the "Pathway" pathway
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
                            "elementType": "INTERACTIVE"
                          }
                        },
                        {
                          "node": {
                            "id": "${Walkable2_id}",
                            "elementType": "INTERACTIVE"
                          }
                        },
                        {
                          "node": {
                            "id": "${Walkable3_id}",
                            "elementType": "INTERACTIVE"
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
