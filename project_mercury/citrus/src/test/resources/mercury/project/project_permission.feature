Feature: Project permissions feature

  Background:
    Given an account "Alice" is created
    And an account "Bob" is created
    And an account "Chuck" is created
    And "Alice" has created workspace "one"
    And "Alice" has created project "MLAB" in workspace "one"

  Scenario: it should allow a user with contributor or higher permission level to replace a project name
    Given "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "MLAB"
    Then "Bob" can replace project "MLAB" name with "ELAB"
    When "Alice" has revoked "account" "Bob" permission level over project "ELAB"
    Then "Bob" is not allowed to replace project "ELAB" name with "MLAB"

  Scenario: It should not allow a user with a permission level lower than contributor to replace a project name
    Given "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "MLAB"
    And "Bob" has granted "REVIEWER" permission level to "account" "Chuck" over project "MLAB"
    Then "Chuck" is not allowed to replace project "MLAB" name with "ELAB"

  Scenario: It should allow a user with owner permission level to delete a project
    Given "Alice" has granted "OWNER" permission level to "account" "Bob" over project "MLAB"
    Then "Bob" can delete project "MLAB"

  Scenario: It should not allow a user with a permission level lower than owner to delete a project
    Given "Alice" has granted "CONTRIBUTOR" permission level to "account" "Chuck" over project "MLAB"
    Then "Chuck" is not allowed to delete project "MLAB"

  Scenario: It should allow a user with proper permission to share a project with a team
    Given "Alice" has created a "RESEARCH" team
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "MLAB"
    And "Alice" adds "Bob" to the "RESEARCH" team as "REVIEWER"
    And "Alice" adds "Chuck" to the "RESEARCH" team as "REVIEWER"
    And "Alice" has granted "CONTRIBUTOR" permission level to "team" "RESEARCH" over project "MLAB"
    Then "Bob" can replace project "MLAB" name with "ELAB"
    Given "Alice" has revoked "team" "RESEARCH" permission level over project "ELAB"
    Then "Chuck" is not allowed to replace project "ELAB" name with "MLAB"
