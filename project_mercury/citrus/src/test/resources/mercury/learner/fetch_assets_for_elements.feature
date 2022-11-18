@Learn
Feature: Fetch learn activity in Learn context

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "Course" inside project "TRO"
    And "Alice" has uploaded special "Image" asset once
    And "Alice" has added "Image" asset to "Course" activity
    And "Alice" has created a "Pathway" pathway for the "Course" activity
    And "Alice" has created a "Walkable1" activity for the "Pathway" pathway
    And "Alice" has created a "Walkable2" interactive for the "Pathway" pathway
    And "Alice" has created a "Field" component for the "Walkable2" interactive
    And "Alice" has added "Image" asset to "Walkable1" activity
    And "Alice" has added "Image" asset to "Walkable2" interactive
    And "Alice" has added "Image" asset to "Field" component
    And "Alice" has published "Course" activity to "CourseDeployment"
    And a support role account "Charlie" exists
    And a mycloud account "Charlie" is provisioned

  Scenario: User should fetch learn activity along with first level assets through RTM
    When "Charlie" sends a GraphQL message and authenticate via my cloud
    """
    {
      learn {
        cohort(cohortId: "${cohort_id}") {
          id
          name
          deployment(deploymentId: "${CourseDeployment_id}") {
            activity {
              id
              assets {
                edges {
                  node {
                    urn
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
          "id": "${cohort_id}",
          "name": "Citrus cohort",
          "deployment": [
            {
              "activity": {
                "id": "${Course_id}",
                "assets": {
                  "edges": [
                  {
                    "node": {
                      "urn": "@notEmpty()@"
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
    """

  Scenario: User should fetch learn activity along with second level assets through RTM
    When "Charlie" sends a GraphQL message and authenticate via my cloud
    """
    {
      learn {
        cohort(cohortId: "${cohort_id}") {
          id
          name
          deployment(deploymentId: "${CourseDeployment_id}") {
            activity {
              id
              assets {
                edges {
                  node {
                    urn
                  }
                }
              }
              pathways {
                walkables {
                  edges {
                    node {
                      id
                      assets {
                        edges {
                          node {
                            urn
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
          "id": "${cohort_id}",
          "name": "Citrus cohort",
          "deployment": [
            {
              "activity": {
                "id": "${Course_id}",
                "assets": {
                  "edges": [
                  {
                    "node": {
                      "urn": "@notEmpty()@"
                    }
                  }
                  ]
                },
                "pathways": [
                  {
                    "walkables": {
                      "edges": [
                        {
                          "node": {
                            "id": "${Walkable1_id}",
                            "assets": {
                              "edges": [
                                {
                                  "node": {
                                    "urn": "@notEmpty()@"
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

  Scenario: User should fetch learn activity along with component level assets through RTM
    When "Charlie" sends a GraphQL message and authenticate via my cloud
    """
    {
      learn {
        cohort(cohortId: "${cohort_id}") {
          id
          name
          deployment(deploymentId: "${CourseDeployment_id}") {
            activity {
              id
              assets {
                edges {
                  node {
                    urn
                  }
                }
              }
              pathways {
                walkables {
                  edges {
                    node {
                      components {
                        assets {
                          edges {
                            node {
                              urn
                            }
                          }
                        }
                      }
                      id
                      assets {
                        edges {
                          node {
                            urn
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
          "id": "${cohort_id}",
          "name": "Citrus cohort",
          "deployment": [
            {
              "activity": {
                "id": "${Course_id}",
                "assets": {
                  "edges": [
                  {
                    "node": {
                      "urn": "@notEmpty()@"
                    }
                  }
                  ]
                },
                "pathways": [
                  {
                    "walkables": {
                      "edges": [
                        {
                          "node": {
                            "id": "${Walkable1_id}",
                            "assets": {
                              "edges": [
                                {
                                  "node": {
                                    "urn": "@notEmpty()@"
                                  }
                                }
                              ]
                            },
                            "components": []
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
