Feature: Subscribe/unsubscribe to a cohort changes

  Background:
    Given workspace accounts are created
      | Alice   |
      | Bob     |
      | Charlie |
    And "Alice" is logged in to the default client
    And "Bob" is logged via a "bob" client
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort with values
      | name           | Alice's cohort      |
      | enrollmentType | OPEN                |
      | workspaceId    | ${one_workspace_id} |
    And "Alice" has shared the created cohort with "Bob" as "REVIEWER"

  Scenario: User producing the event should not get a broadcast message
    Given "Charlie" is logged via a "charlie" client
    And "Alice" has shared the created cohort with "Charlie" as "REVIEWER"
    And "Alice" subscribes to "cohort" events
    And "Bob" subscribes to "cohort" events via a "bob" client
    And "Charlie" subscribes to "cohort" events via a "charlie" client
    When "Alice" has updated the cohort
      | name           | Alice's updated cohort |
      | enrollmentType | PASSPORT               |
    Then "Bob" should receive a cohort "COHORT_CHANGED" notification via a "bob" client
    And "Charlie" should receive a cohort "COHORT_CHANGED" notification via a "charlie" client
    But "Alice" should not receive any cohort notification

  Scenario: A subscribed user should be notified when a cohort is updated
    Given "Bob" subscribes to "cohort" events via a "bob" client
    When "Alice" has updated the cohort
      | name           | Alice's updated cohort |
      | enrollmentType | PASSPORT               |
    Then "Bob" should receive a cohort "COHORT_CHANGED" notification via a "bob" client

#  This scenario is deprecated and it will be removed soon.
#  Scenario: A subscribed user should be notified when a new user is enrolled to a cohort
#    Given "Bob" subscribes to "cohort" events via a "bob" client
#    When "Alice" has enrolled "Charlie" to the created cohort
#    Then "Bob" should receive a cohort "ENROLLED" notification via a "bob" client
#
#  This scenario is deprecated and it will be removed soon
#  Scenario: A subscribed user should be notified when a user is disenrolled from a cohort
#    Given "Alice" has enrolled "Charlie" to the created cohort
#    And "Bob" subscribes to "cohort" events via a "bob" client
#    When "Alice" has disenrolled "Charlie" from the created cohort
#    Then "Bob" should receive a cohort "DISENROLLED" notification via a "bob" client

  Scenario: A subscribed user should be notified when a cohort is shared with new user
    Given "Bob" subscribes to "cohort" events via a "bob" client
    When "Alice" has shared the created cohort with "Charlie" as "CONTRIBUTOR"
    Then "Bob" should receive a cohort "COHORT_GRANTED" notification via a "bob" client

  Scenario: A subscribed user should be notified when a cohort permission is revoked for some user
    Given "Alice" has shared the created cohort with "Charlie" as "CONTRIBUTOR"
    And "Bob" subscribes to "cohort" events via a "bob" client
    When "Alice" has revoked "Charlie"'s permission
    Then "Bob" should receive a cohort "COHORT_REVOKED" notification via a "bob" client

  Scenario: A subscribed user should be notified when a cohort is archived
    Given "Bob" subscribes to "cohort" events via a "bob" client
    When "Alice" has archived the created cohort
    Then "Bob" should receive a cohort "COHORT_ARCHIVED" notification via a "bob" client
    When "Alice" has unarchived the created cohort
    Then "Bob" should receive a cohort "COHORT_UNARCHIVED" notification via a "bob" client

  Scenario: A user can subscribe/unsubscribe to cohort with CONTRIBUTOR permission
    Given "Alice" has shared the created cohort with "Bob" as "CONTRIBUTOR"
    Given "Bob" subscribes to "cohort" events via a "bob" client
    Given "Bob" unsubscribe to "cohort" events via a "bob" client

  Scenario: A user can not subscribe/unsubscribe to cohort without permission
    Given "Charlie" is logged via a "Charlie" client
    Given "Charlie" cannot subscribes to "cohort" events via a "Charlie" client due to missing permission level
    Given "Charlie" cannot unsubscribe to "cohort" events via a "Charlie" client due to missing permission level
