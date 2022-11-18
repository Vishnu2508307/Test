Feature: Enrolling/Disenrolling accounts to/from a cohort

  Background:
    Given a workspace account "Alice" is created
    And an ies account "Bob" is provisioned
    And an ies account "Charlie" is provisioned
    And "Alice" has created workspace "one"
    Given "Alice" has created a cohort for workspace "one"

#  Deprecated
#  Scenario: The cohort OWNER should be able to enroll an account to it
#    Given "Alice" enrolls "Bob" to the created cohort
#    Then "Bob" is successfully enrolled

#  Deprecated
#  Scenario: A cohort CONTRIBUTOR should be able to enroll an account to it
#    Given "Alice" has shared the created cohort with "Bob" as "CONTRIBUTOR"
#    When "Bob" enrolls "Charlie" to the created cohort
#    Then "Charlie" is successfully enrolled

#  Deprecated
#  Scenario: A cohort REVIEWER should NOT be able to enroll an account to it
#    Given "Alice" has shared the created cohort with "Bob" as "REVIEWER"
#    When "Bob" enrolls "Charlie" to the created cohort
#    Then "Bob" is not able to "enroll" "Charlie" due to missing permission level

#  Deprecated
#  Scenario: The cohort OWNER should be able to disenroll an account from it
#    Given "Alice" has enrolled "Bob" to the created cohort
#    When "Alice" disenroll "Bob" from the created cohort
#    Then "Bob" is successfully disenrolled

#  Deprecated
#  Scenario: A cohort CONTRIBUTOR should be able to disenroll an account from it
#    Given "Alice" has shared the created cohort with "Bob" as "CONTRIBUTOR"
#    And "Alice" has enrolled "Charlie" to the created cohort
#    When "Bob" disenroll "Charlie" from the created cohort
#    Then "Charlie" is successfully disenrolled

#  Deprecated
#  Scenario: A cohort REVIEWER should NOT be able to disenroll an account from it
#    Given "Alice" has shared the created cohort with "Bob" as "REVIEWER"
#    And "Alice" has enrolled "Charlie" to the created cohort
#    When "Bob" disenroll "Charlie" from the created cohort
#    Then "Bob" is not able to "disenroll" "Charlie" due to missing permission level
