Feature: Create a competency document

  Scenario: User should be able to create a competency document
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    When "Alice" sends a GraphQL message
    """
    mutation {
      competencyDocumentCreate(input: {workspaceId: "${one_workspace_id}", title: "Chemistry Knowledge Map"}) {
        document {
          documentId
          title
          createdAt
          lastChangedAt
          workspaceId
        }
      }
    }
    """
    Then mercury should respond with a GraphQL response
    """
    {
      "competencyDocumentCreate": {
        "document": {
          "documentId": "@notEmpty()@",
          "title": "Chemistry Knowledge Map",
          "createdAt": "@notEmpty()@",
          "lastChangedAt": null,
          "workspaceId": "${one_workspace_id}"
        }
      }
    }
    """

  Scenario: User should have contributor permissions over workspace to create document
    Given a workspace account "Alice" is created
    And a workspace account "Bob" is created
    And "Alice" has created workspace "one"
    And "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"
    When "Bob" sends a GraphQL message
    """
    mutation {
      competencyDocumentCreate(input: {workspaceId: "${one_workspace_id}", title: "Chemistry Knowledge Map"}) {
        document {
          documentId
        }
      }
    }
    """
    Then GraphQL should respond with error code 403 and message "User does not have permissions to change workspace"
    When "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    When "Bob" sends a GraphQL message
    """
    mutation {
      competencyDocumentCreate(input: {workspaceId: "${one_workspace_id}", title: "Chemistry Knowledge Map"}) {
        document {
          documentId
        }
      }
    }
    """
    Then mercury should respond with a GraphQL response
    """
    {
      "competencyDocumentCreate": {
        "document": {
          "documentId": "@notEmpty()@"
        }
      }
    }
    """
