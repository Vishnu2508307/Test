Feature: Move interactive to another pathway

  Background:
    Given a workspace account "Alice" is created
    And a workspace account "Bob" is created
    And "Alice" has created and published course plugin
    And "Alice" has created workspace "one"
    And "Bob" has created workspace "two"
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Bob" has created project "MLAB" in workspace "two"
    And "Bob" has created activity "UNIT_TWO" inside project "MLAB"
    And "Bob" has created a "LINEAR_TWO" pathway for the "UNIT_TWO" activity

  Scenario: User should have Contributor permission over destination pathway to move interactive
    Given "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Bob" has created a "SCREEN_TWO" interactive for the "LINEAR_TWO" pathway
    When "Alice" moves "SCREEN_ONE" interactive to "LINEAR_TWO" pathway at position 0
    Then the interactive move fails with message "Unauthorized: User does not have required permissions on the pathway" and code 401

  Scenario: User should have Contributor permission over origin interactive to move to pathway
    Given "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Bob" has created a "SCREEN_TWO" interactive for the "LINEAR_TWO" pathway
    Then "Bob" can not move "SCREEN_ONE" interactive to "LINEAR_TWO" pathway at position 0 due to error: code 401 message "Unauthorized: Unauthorized permission level"
    When "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    Then "Bob" can move "SCREEN_ONE" interactive to "LINEAR_TWO" pathway at position 0
    When "Bob" fetches the "LINEAR_TWO" pathway
    Then "LINEAR_TWO" pathway has walkable children
      | SCREEN_ONE |
      | SCREEN_TWO |
    And the "LINEAR_ONE" pathway has empty walkable children

  Scenario: user should be able to move interactive to another pathway
    Given "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Bob" has created a "SCREEN_TWO" interactive for the "LINEAR_TWO" pathway
    And "Bob" has granted "CONTRIBUTOR" permission level to "account" "Alice" over project "MLAB"
    When "Alice" moves "SCREEN_ONE" interactive to "LINEAR_TWO" pathway at position 0
    Then the "SCREEN_ONE" interactive has been successfully moved to "LINEAR_TWO" pathway
    When "Alice" fetches the "LINEAR_TWO" pathway
    Then "LINEAR_TWO" pathway has walkable children
      | SCREEN_ONE |
      | SCREEN_TWO |
    And the "LINEAR_ONE" pathway has empty walkable children
