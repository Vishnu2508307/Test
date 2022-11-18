Feature: Permissions validation on add/remove assets

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "COURSE" inside project "TRO"
    And "Alice" has uploaded special "Image" asset once
    And a workspace account "Bob" is created
    And "Bob" is logged via a "Bob" client

  Scenario: User should not be able to add asset without CONTRIBUTOR permissions over workspace
    Then "Bob" can not add an asset to "COURSE" activity due to error: code 401 message "Unauthorized: Unauthorized permission level"
    When "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"
    Then "Bob" can not add an asset to "COURSE" activity due to error: code 401 message "Unauthorized: Unauthorized permission level"

  Scenario: User should not be able to remove asset without CONTRIBUTOR permissions over workspace
    Then "Bob" can not remove an asset to "COURSE" activity due to error: code 401 message "Unauthorized: Unauthorized permission level"
    When "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"
    Then "Bob" can not remove an asset to "COURSE" activity due to error: code 401 message "Unauthorized: Unauthorized permission level"
