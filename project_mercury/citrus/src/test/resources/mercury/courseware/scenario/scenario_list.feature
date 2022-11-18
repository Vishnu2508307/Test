Feature: Listing scenarios for specific lifecycle

  Background:
    Given an account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "activity" inside project "TRO"

  Scenario: The user should see the created scenario in a list of scenario in creation order
    When "Alice" has created scenario with
      | name      | scenario 1        |
      | condition | condition         |
      | actions   | actions           |
      | lifecycle | ACTIVITY_EVALUATE |
    Then the list of scenarios for the activity and lifecycle ACTIVITY_EVALUATE contains
      | scenario 1 |
    When "Alice" has created scenario with
      | name      | scenario 2        |
      | condition | condition         |
      | actions   | actions           |
      | lifecycle | ACTIVITY_EVALUATE |
    Then the list of scenarios for the activity and lifecycle ACTIVITY_EVALUATE contains
      | scenario 1 |
      | scenario 2 |

  Scenario: The user should see the list of scenarios filtered by lifecycle
    When "Alice" has created scenario with
      | name      | scenario 1        |
      | condition | condition         |
      | actions   | actions           |
      | lifecycle | ACTIVITY_EVALUATE |
    And "Alice" has created scenario with
      | name      | scenario 2     |
      | condition | condition         |
      | actions   | actions           |
      | lifecycle | ACTIVITY_START |
    Then the list of scenarios for the activity and lifecycle ACTIVITY_EVALUATE contains
      | scenario 1 |
    And the list of scenarios for the activity and lifecycle ACTIVITY_START contains
      | scenario 2 |

  Scenario: The user should see reordered list of scenarios
    When "Alice" has created scenario with
      | name      | scenario 1        |
      | condition | condition         |
      | actions   | actions           |
      | lifecycle | ACTIVITY_EVALUATE |
    And "Alice" has created scenario with
      | name      | scenario 2        |
      | condition | condition         |
      | actions   | actions           |
      | lifecycle | ACTIVITY_EVALUATE |
    And "Alice" has reordered the scenarios for the activity and lifecycle ACTIVITY_EVALUATE
      | scenario 2 |
      | scenario 1 |
    Then the list of scenarios for the activity and lifecycle ACTIVITY_EVALUATE contains
      | scenario 2 |
      | scenario 1 |

  Scenario: Only workspace REVIEWER can fetch list of scenarios
    Given a workspace account "Bob" is created
    Then "Bob" can not fetch list of scenarios for the activity due to message "Unauthorized: Unauthorized permission level" and code 401
    When "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    Then "Bob" can fetch a list of scenarios for the activity successfully
