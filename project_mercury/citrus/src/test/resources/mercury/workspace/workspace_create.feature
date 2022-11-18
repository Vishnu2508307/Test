Feature: Create a workspace

  Background:
    Given a workspace account "Alice" is created
    And "Alice" provides "Bob" with a new account in the subscription as
      | DEVELOPER  |
      | AERO_INSTRUCTOR |

  Scenario: A workspace user with owner permission level over the subscription should be able to create a workspace
    When "Alice" creates workspace "one"
    Then workspace "one" is successfully created

  Scenario: A workspace user with no permission level over the subscription should not be able to create a workspace
    When "Bob" creates workspace "two"
    Then the workspace is not created due to missing permission level

  Scenario: A workspace user with reviewer permission level should not be able to create a workspace
    Given "Alice" has shared his subscription with "Bob" as "REVIEWER"
    When "Bob" creates workspace "two"
    Then the workspace is not created due to missing permission level

  Scenario: A workspace user with contributor permission level over the subscription should be able to create a workspace
    Given "Alice" has shared his subscription with "Bob" as "CONTRIBUTOR"
    When "Bob" creates workspace "two"
    Then workspace "two" is successfully created

  Scenario: A workspace user with contributor or higher permission level over the workspace should be able to update the workspace
    Given "Alice" has created workspace "one"
    And "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    When "Bob" tries updating workspace "one" with
      | description | a simple description |
      | name | one |
    Then workspace "one" is successfully updated with
      | description | a simple description |

  Scenario: A workspace user with reviewer or no permission level over the workspace should not be able to update the workspace
    Given "Alice" has created workspace "one"
    And "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"
    When "Bob" tries updating workspace "one" with
      | description | a simple description |
      | name | one |
    Then the workspace is not updated due to missing permission level
