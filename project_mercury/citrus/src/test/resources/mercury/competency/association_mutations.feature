Feature: Association mutations api

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a competency document "SKILLS" in workspace "one"
    And "Alice" has created a document item "ITEM_ONE" for "SKILLS" document
    And "Alice" has created a document item "ITEM_TWO" for "SKILLS" document
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"

  Scenario: Document Owner should be able to create an association
    When "Alice" creates an association "ASSOCIATION_ONE" for "SKILLS" document with
      | originItemId      | ITEM_ONE  |
      | destinationItemId | ITEM_TWO  |
      | associationType   | isChildOf |
    Then the association "ASSOCIATION_ONE" is created successfully

  Scenario: Document Owner should be able to delete an association
    Given "Alice" has created an association "ASSOCIATION_ONE" for "SKILLS" document with
      | originItemId      | ITEM_ONE  |
      | destinationItemId | ITEM_TWO  |
      | associationType   | isChildOf |
    When "Alice" deletes an association "ASSOCIATION_ONE" for "SKILLS" document
    Then the association "ASSOCIATION_ONE" is deleted successfully

  Scenario: User should have Contributor permission level to create an association inside a document
    Given a workspace account "Bob" is created
    And "Alice" has shared the "SKILLS" document with "Bob" as REVIEWER
    When "Bob" creates an association "ASSOCIATION_ONE" for "SKILLS" document with
      | originItemId      | ITEM_ONE  |
      | destinationItemId | ITEM_TWO  |
      | associationType   | isChildOf |
    Then the association is not "created" due to missing permission level
    When "Alice" has shared the "SKILLS" document with "Bob" as CONTRIBUTOR
    And "Bob" creates an association "ASSOCIATION_ONE" for "SKILLS" document with
      | originItemId      | ITEM_ONE  |
      | destinationItemId | ITEM_TWO  |
      | associationType   | isChildOf |
    Then the association "ASSOCIATION_ONE" is created successfully

  Scenario: User should have Contributor permission level to delete an association inside a document
    Given a workspace account "Bob" is created
    And "Alice" has shared the "SKILLS" document with "Bob" as REVIEWER
    Given "Alice" has created an association "ASSOCIATION_ONE" for "SKILLS" document with
      | originItemId      | ITEM_ONE  |
      | destinationItemId | ITEM_TWO  |
      | associationType   | isChildOf |
    When "Bob" deletes an association "ASSOCIATION_ONE" for "SKILLS" document
    Then the association is not "deleted" due to missing permission level
    When "Alice" has shared the "SKILLS" document with "Bob" as CONTRIBUTOR
    And "Bob" deletes an association "ASSOCIATION_ONE" for "SKILLS" document
    Then the association "ASSOCIATION_ONE" is deleted successfully

  Scenario: Document association can not be deleted if it was published
    Given "Alice" has created a cohort in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created an association "ASSOCIATION_ONE" for "SKILLS" document with
      | originItemId      | ITEM_ONE  |
      | destinationItemId | ITEM_TWO  |
      | associationType   | isChildOf |
    And "Alice" has linked to "ACTIVITY" "UNIT_ONE" competency items from document "SKILLS"
      | ITEM_ONE |
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"
    And "Alice" deletes an association "ASSOCIATION_ONE" for "SKILLS" document
    Then GraphQL should respond with error code 400 and message "Association can not be deleted"
