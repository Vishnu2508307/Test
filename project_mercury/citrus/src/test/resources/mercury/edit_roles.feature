Feature: Adding and removing roles to an account

  Background:
    Given a workspace account "Alice" is created
    And a workspace account "Bob" is created

  Scenario: It should allow a subscription contributor to add a role to an account
    Given "Alice" has shared his subscription with "Bob" as "CONTRIBUTOR"
    When "Bob" "add"s "STUDENT_GUEST" role to "Alice"'s account
    Then the "STUDENT" role is successfully added to "Alice"'s account

  Scenario: It should allow a subscription contributor to remove a role from an account
    Given "Alice" has shared his subscription with "Bob" as "CONTRIBUTOR"
    When "Bob" "remove"s "DEVELOPER" role to "Alice"'s account
    Then the "DEVELOPER" role is successfully removed from "Alice"'s account

  Scenario: It should not allow a user without permission over the subscription to add a role to an account
    When "Bob" "add"s "DEVELOPER" role to "Alice"'s account
    Then the role is not added due to missing permission level

  Scenario: It should not allow a user without permission over the subscription to remove a role from an account
    When "Bob" "remove"s "DEVELOPER" role to "Alice"'s account
    Then the role is not removed due to missing permission level

  Scenario: It should not allow to add an a role that the user already has
    When "Alice" "add"s "DEVELOPER" role to "Alice"'s account
    Then the role is not added because already assigned

  Scenario: It should not allow to add/remove a role higher than the highest role the authenticated user has
    Given "Alice" has shared his subscription with "Bob" as "CONTRIBUTOR"
    When "Bob" "add"s "SUPPORT" role to "Alice"'s account
    Then the role is not added due to a higher role required
    When "Bob" "remove"s "SUPPORT" role to "Alice"'s account
    Then the role is not removed due to a higher role required
