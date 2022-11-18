Feature: Listing competency document collaborators

  Background:
    Given a workspace account "Alice" is created
    And a workspace account "Bob" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a "Developers" team
    And "Alice" has created a competency document "SKILLS" in workspace "one"
    And "Alice" has shared the "SKILLS" document with "Bob" as REVIEWER
    And "Alice" has shared the "SKILLS" document with team "Developers" as CONTRIBUTOR

  Scenario: It should list all the collaborators for a competency document
    When "Alice" lists all the collaborators for document "SKILLS"
    Then the following document collaborators are returned
      | Alice       | account |
      | Bob         | account |
      | Developers  | team    |
