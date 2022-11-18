Feature: List all the collaborators for a cohort

  Background:
    Given workspace accounts are created
      | Alice    |
      | Bob      |
      | Charlie  |
      | Felicity |
    And "Alice" has created teams
      | Marketing |
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort for workspace "one"
    And "Alice" has shared the created cohort with "Bob" as "REVIEWER"
    And "Alice" has shared the created cohort with "Charlie" as "CONTRIBUTOR"
    And "Alice" has shared the created cohort with "Felicity" as "OWNER"
    And "Alice" has granted "CONTRIBUTOR" permission to the "Marketing" team over the cohort

  Scenario: the cohort owner should be able to list the collaborators
    When "Alice" lists the collaborators for the created cohort
    Then the following cohort collaborators are listed
      | Marketing | team    |
      | Alice     | account |
      | Bob       | account |
      | Charlie   | account |
      | Felicity  | account |

  Scenario: a cohort reviewer should be able to list the collaborators
    When "Bob" lists the collaborators for the created cohort
    Then the following cohort collaborators are listed
      | Marketing | team    |
      | Alice     | account |
      | Bob       | account |
      | Charlie   | account |
      | Felicity  | account |

  Scenario: a cohort contributor should be able to list the collaborators
    When "Charlie" lists the collaborators for the created cohort
    Then the following cohort collaborators are listed
      | Marketing | team    |
      | Alice     | account |
      | Bob       | account |
      | Charlie   | account |
      | Felicity  | account |

  Scenario: a user without any kind of permission over the cohort should not be able to list the collaborators
    Given "Felicity" has revoked "Bob"'s permission over the created cohort
    When "Bob" lists the collaborators for the created cohort
    Then the collaborators listing request is not authorized

  Scenario: a user should be able to list a limited numbers of cohort collaborators
    When "Alice" lists "2" cohort collaborators
    Then only 1 team and 1 account out of 5 cohort collaborators are listed

