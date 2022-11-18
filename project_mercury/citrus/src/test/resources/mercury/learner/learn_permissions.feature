@Learn
Feature: Check permissions for learn queries

#  Background:
#    Given a workspace account "Alice" is created
#    And "Alice" has created workspace "one"
#    And "Alice" has created a cohort with values
#      | name             | User1's cohort                    |
#      | enrollmentType   | PASSPORT                          |
#      | startDate        | Fri, 17 Aug 2018 00:00:00 GMT     |
#      | endDate          | Sat, 17 Aug 2019 23:59:59 GMT     |
#      | color            | red                               |
#      | bannerPattern    | http://test.com/bannerPattern.jpg |
#      | bannerImage      | http://test.com/bannerImage.jpg   |
#      | workspaceId      | ${one_workspace_id}               |
#      | productId        | A103000103955                     |
# FIXME: this scenario is expecting an IES user, Currently we have no way of provisioning an IES user in citrus... yet
#  Scenario: Enrolled student should be able to query learn cohort
#    Given a student account "Bob" is created
#    Then "Bob" can not query learn cohort due to error with code 403 and message "User does not have permissions to view cohort"
#    When "Alice" has enrolled "Bob" to the created cohort
#    Then "Bob" can query learn cohort successfully

#  TODO This scenario is not true anymore and it will be deleted
#  Scenario: User with permissions over cohort should be able to query learn cohort
#    Given a workspace account "Bob" is created
#    Then "Bob" can not query learn cohort due to error with code 403 and message "User does not have permissions to view cohort"
#    When "Alice" has granted "REVIEWER" permission to "Bob" over the cohort
#    Then "Bob" can query learn cohort successfully
