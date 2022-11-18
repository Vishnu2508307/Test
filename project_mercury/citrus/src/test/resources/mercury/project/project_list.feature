Feature: Project and activity listing feature

  Background:
    Given an account "Alice" is created
    And an account "Bob" is created
    And an account "Chuck" is created
    And "Alice" has created workspace "one"

  Scenario: It should allow a user to list the created project inside the workspace
    Given "Alice" has created project "TRO" in workspace "one"
    When "Alice" lists all the projects inside workspace "one"
    Then the following projects are listed for workspace "one"
      | TRO |

  Scenario: A user should be able to list all the shared projects within a workspace
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created project "MLAB" in workspace "one"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over workspace "one"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "MLAB"
    Then "Alice" can list the following projects from workspace "one"
      | TRO  |
      | MLAB |
    And "Bob" can list the following projects from workspace "one"
      | MLAB |

  Scenario: A user should not be able to list a deleted project
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created project "MLAB" in workspace "one"
    And "Alice" has deleted project "TRO"
    Then "Alice" can list the following projects from workspace "one"
      | MLAB |

  Scenario: A user should not be able to list projects without being granted a permission level
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over workspace "one"
    Then "Bob" projects list for workspace "one" is empty

  Scenario: A user with contributor or higher permission level over the project should be able to create an activity inside the project
    Given "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    When "Bob" creates activity "ACTIVITY_ONE" inside project "TRO"
    Then activity "ACTIVITY_ONE" is successfully created inside project "TRO"

  Scenario: A user with contributor or higher permission level over the project should be able to delete an activity inside the project
    Given "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    And "Alice" has created activity "ACTIVITY_ONE" inside project "TRO"
    And "Alice" has created activity "ACTIVITY_TWO" inside project "TRO"
    Then "Bob" can list the following activities from project "TRO"
      | ACTIVITY_ONE |
      | ACTIVITY_TWO |
    When "Bob" deletes activity "ACTIVITY_ONE" from project "TRO"
    Then activity "ACTIVITY_ONE" is successfully deleted from project "TRO"
    And "Alice" can list the following activities from project "TRO"
      | ACTIVITY_TWO |

  Scenario: A user should be able to list activities and fetch fields for it
    Given "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "ACTIVITY_ONE" inside project "TRO"
    And "Alice" has created configuration for "ACTIVITY_ONE" with field name "titleField" and field value "titleValue"
    Then "Alice" can list the following activities from project "TRO" with field "titleField" "FOUND"
      | ACTIVITY_ONE |

  Scenario: A user should be able to list activities and but not able to fetch config fields for it
    Given "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "ACTIVITY_ONE" inside project "TRO"
    And "Alice" has created configuration for "ACTIVITY_ONE" with field name "titleField" and field value "titleValue"
    Then "Alice" can list the following activities from project "TRO" with field "title" "NOT_FOUND"
      | ACTIVITY_ONE |

  Scenario: It should allow to fetch list of projects with Permission inside the workspace
    Given "Alice" has created project "TRO" in workspace "one"
    When "Alice" lists all the projects inside workspace "one"
    Then projects are listed for workspace with permission "one"
      | TRO | OWNER |