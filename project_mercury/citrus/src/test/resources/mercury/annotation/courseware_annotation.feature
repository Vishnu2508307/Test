Feature: Test the courseware annotation api

  Background:
    Given a workspace account "Alice" is created
    And a workspace account "Bob" is created
    And "Alice" has created workspace "one"
    And a workspace account "Charlie" is created
    And "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"

  Scenario: A user with workspace REVIEWER permission should be able to create a courseware annotation
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    When "Bob" creates a courseware "commenting" annotation for element "SCREEN_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    Then the courseware annotation "ONE" is created succesfully

  Scenario: A user with no permission should not be able to create a courseware annotation
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    When "Charlie" creates a courseware "commenting" annotation for element "SCREEN_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    Then the courseware annotation is not created due to missing permission level

  Scenario: A user should be able to get all the annotation by the root element and motivation
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    Given "Alice" has created a courseware "commenting" annotation "ONE" for element "SCREEN_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a courseware "commenting" annotation "TWO" for element "LINEAR_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    When "Bob" fetches courseware annotation by rootElement "UNIT_ONE" and motivation "commenting"
    Then the following courseware annotations are returned
      | ONE |
      | TWO |
    When "Alice" fetches courseware annotation by rootElement "UNIT_ONE", motivation "commenting" and element "LINEAR_ONE"
    Then the following courseware annotations are returned
      | TWO |
    When "Charlie" fetches courseware annotation by rootElement "UNIT_ONE" and motivation "commenting"
    Then the courseware annotations are not returned due to missing permission level

  Scenario: A user with Contributor permission and identifying motivation should be able to create a courseware annotation
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    When "Bob" creates a courseware "identifying" annotation for element "SCREEN_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    Then the courseware annotation "one" is created succesfully

  Scenario: A user with reviewer permission and identifying motivation should not be able to create a courseware annotation
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has granted "Charlie" with "REVIEWER" permission level on workspace "one"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    When "Charlie" creates a courseware "identifying" annotation for element "SCREEN_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    Then the courseware annotation is not created due to missing permission level

  Scenario: A user should be able to get all the annotation by the root element and for motivation identifying
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    Given "Alice" has created a courseware "identifying" annotation "ONE" for element "SCREEN_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a courseware "identifying" annotation "TWO" for element "LINEAR_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    When "Bob" fetches courseware annotation by rootElement "UNIT_ONE" and motivation "identifying"
    Then the following courseware annotations are returned
      | ONE |
      | TWO |
    When "Alice" fetches courseware annotation by rootElement "UNIT_ONE", motivation "identifying" and element "LINEAR_ONE"
    Then the following courseware annotations are returned
      | TWO |
    When "Charlie" fetches courseware annotation by rootElement "UNIT_ONE" and motivation "identifying"
    Then the courseware annotations are not returned due to missing permission level

  Scenario: A user with Contributor permission and identifying motivation should be able to delete a courseware annotation
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    When "Bob" creates a courseware "identifying" annotation for element "SCREEN_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    Then the courseware annotation "one" is created succesfully
    When "Bob" deletes annotation "one" of courseware
    Then the courseware annotation "one" is deleted succesfully

  Scenario: A user with Reviewer permission and identifying motivation should not be able to delete a courseware annotation
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    When "Bob" creates a courseware "identifying" annotation for element "SCREEN_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    Then the courseware annotation "one" is created succesfully
    When "Charlie" deletes annotation "one" of courseware
    Then the courseware annotation "one" is not deleted due to invalid or missing permission level

    # project courseware annotations

  Scenario: A user with workspace REVIEWER permission should be able to create a courseware annotation
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has granted "REVIEWER" permission level to "account" "Charlie" over project "TRO"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    When "Charlie" creates a courseware "commenting" annotation for element "SCREEN_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    Then the courseware annotation "one" is created succesfully

  Scenario: A user with no permission should not be able to create a courseware annotation
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    When "Charlie" creates a courseware "commenting" annotation for element "SCREEN_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    Then the courseware annotation is not created due to missing permission level

  Scenario: A user should be able to get all the annotation by the root element and motivation
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    Given "Alice" has created a courseware "commenting" annotation "ONE" for element "SCREEN_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a courseware "commenting" annotation "TWO" for element "LINEAR_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    When "Bob" fetches courseware annotation by rootElement "UNIT_ONE" and motivation "commenting"
    Then the following courseware annotations are returned
      | ONE |
      | TWO |
    When "Alice" fetches courseware annotation by rootElement "UNIT_ONE", motivation "commenting" and element "LINEAR_ONE"
    Then the following courseware annotations are returned
      | TWO |
    When "Charlie" fetches courseware annotation by rootElement "UNIT_ONE" and motivation "commenting"
    Then the courseware annotations are not returned due to missing permission level

  Scenario: A user with Contributor permission and identifying motivation should be able to create a courseware annotation
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    When "Bob" creates a courseware "identifying" annotation for element "SCREEN_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    Then the courseware annotation "one" is created succesfully

  Scenario: A user with reviewer permission and identifying motivation should not be able to create a courseware annotation
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has granted "REVIEWER" permission level to "account" "Charlie" over project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    When "Charlie" creates a courseware "identifying" annotation for element "SCREEN_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    Then the courseware annotation is not created due to missing permission level

  Scenario: A user should be able to get all the annotation by the root element and for motivation identifying
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    Given "Alice" has created a courseware "identifying" annotation "ONE" for element "SCREEN_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a courseware "identifying" annotation "TWO" for element "LINEAR_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    When "Bob" fetches courseware annotation by rootElement "UNIT_ONE" and motivation "identifying"
    Then the following courseware annotations are returned
      | ONE |
      | TWO |
    When "Alice" fetches courseware annotation by rootElement "UNIT_ONE", motivation "identifying" and element "LINEAR_ONE"
    Then the following courseware annotations are returned
      | TWO |
    When "Charlie" fetches courseware annotation by rootElement "UNIT_ONE" and motivation "identifying"
    Then the courseware annotations are not returned due to missing permission level

  Scenario: A user with Contributor permission and identifying motivation should be able to delete a courseware annotation
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    When "Bob" creates a courseware "identifying" annotation for element "SCREEN_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    Then the courseware annotation "one" is created succesfully
    When "Bob" deletes annotation "one" of courseware
    Then the courseware annotation "one" is deleted succesfully

  Scenario: A user with Reviewer permission and identifying motivation should not be able to delete a courseware annotation
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    When "Bob" creates a courseware "identifying" annotation for element "SCREEN_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    Then the courseware annotation "one" is created succesfully
    When "Charlie" deletes annotation "one" of courseware
    Then the courseware annotation "one" is not deleted due to invalid or missing permission level

    # RTM courseware annotation

  Scenario: A user with workspace REVIEWER permission should be able to create and update a courseware annotation
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    When "Bob" creates a courseware "commenting" annotation through rtm for element "SCREEN_ONE" of type "INTERACTIVE" and rootElement "UNIT_ONE" with
      | target | {"json":"target"} |
      | body   | {"json":"body"}   |
    Then the courseware annotation "one" is created successfully
    Then "Alice" can list following fields from courseware annotation "one" with motivation "commenting" for element "SCREEN_ONE" of type "INTERACTIVE" and rootElement "UNIT_ONE"
      | motivation | commenting |
      | target     | target     |
      | body       | body       |
    When "Bob" updates a courseware annotation "one" with motivation "commenting" for element "SCREEN_ONE" of type "INTERACTIVE" and rootElement "UNIT_ONE" with
      | target | {"json":"target1"} |
      | body   | {"json":"body1"}   |
    Then the courseware annotation "one" is updated successfully
    Then "Alice" can list following fields from courseware annotation "one" with motivation "commenting" and rootElement "UNIT_ONE"
      | motivation | commenting|
      | target     | target1   |
      | body       | body1     |

  Scenario: A user with workspace REVIEWER permission should be able to create a courseware annotation with supplied id
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    When "Bob" creates a courseware "commenting" annotation with a supplied id through rtm for element "SCREEN_ONE" of type "INTERACTIVE" and rootElement "UNIT_ONE" with
      | target | {"json":"target"} |
      | body   | {"json":"body"}   |
    Then the courseware annotation "one" is created successfully with the supplied id

  Scenario: A user with workspace REVIEWER permission should not be able to create a courseware annotation with same id if id already exists
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Bob" creates a courseware "commenting" annotation with a supplied id through rtm for element "SCREEN_ONE" of type "INTERACTIVE" and rootElement "UNIT_ONE" with
      | target | {"json":"target"} |
      | body   | {"json":"body"}   |
    Then the courseware annotation "one" is created successfully with the supplied id
    When "Bob" creates a courseware "commenting" annotation with the same supplied id as before through rtm for element "SCREEN_ONE" of type "INTERACTIVE" and rootElement "UNIT_ONE" with
      | target | {"json":"target"} |
      | body   | {"json":"body"}   |
    Then the courseware annotation is not created due to conflict

  Scenario: A user with no permission should not be able to create a courseware annotation
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    When "Charlie" creates a courseware "commenting" annotation through rtm for element "SCREEN_ONE" of type "INTERACTIVE" and rootElement "UNIT_ONE" with
      | target | {"json":"target"} |
      | body   | {"json":"body"}   |
    Then the courseware annotation create fails due to missing permission level

  Scenario: A user with Contributor permission and identifying motivation should be able to create and update a courseware annotation
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    When "Bob" creates a courseware "identifying" annotation through rtm for element "SCREEN_ONE" of type "INTERACTIVE" and rootElement "UNIT_ONE" with
      | target | {"json":"target"} |
      | body   | {"json":"body"}   |
    Then the courseware annotation "one" is created successfully
    Then "Alice" can list following fields from courseware annotation "one" with motivation "identifying" for element "SCREEN_ONE" of type "INTERACTIVE" and rootElement "UNIT_ONE"
      | motivation | identifying |
      | target     | target      |
      | body       | body        |
    When "Bob" updates a courseware annotation "one" with motivation "identifying" for element "SCREEN_ONE" of type "INTERACTIVE" and rootElement "UNIT_ONE" with
      | target | {"json":"target1"} |
      | body   | {"json":"body1"}   |
    Then the courseware annotation "one" is updated successfully
    Then "Alice" can list following fields from courseware annotation "one" with motivation "identifying" and rootElement "UNIT_ONE"
      | motivation | identifying |
      | target     | target1     |
      | body       | body1       |

  Scenario: A user with reviewer permission and identifying motivation should not be able to create a courseware annotation
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And a workspace account "Charlie" is created
    And "Alice" has granted "REVIEWER" permission level to "account" "Charlie" over project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    When "Charlie" creates a courseware "identifying" annotation through rtm for element "SCREEN_ONE" of type "INTERACTIVE" and rootElement "UNIT_ONE" with
      | target | {"json":"target"} |
      | body   | {"json":"body"}   |
    Then the courseware annotation create fails due to missing permission level

  Scenario: A user with no permission should not be able to update a courseware annotation
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    When "Bob" creates a courseware "commenting" annotation through rtm for element "SCREEN_ONE" of type "INTERACTIVE" and rootElement "UNIT_ONE" with
      | target | {"json":"target"} |
      | body   | {"json":"body"}   |
    Then the courseware annotation "one" is created successfully
    When "Charlie" updates a courseware annotation "one" with motivation "commenting" for element "SCREEN_ONE" of type "INTERACTIVE" and rootElement "UNIT_ONE" with
      | target | {"json":"target1"} |
      | body   | {"json":"body1"}   |
    Then the courseware annotation update fails due to missing permission level

  Scenario: A user with reviewer permission and identifying motivation should not be able to update a courseware annotation
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    When "Bob" creates a courseware "identifying" annotation through rtm for element "SCREEN_ONE" of type "INTERACTIVE" and rootElement "UNIT_ONE" with
      | target | {"json":"target"} |
      | body   | {"json":"body"}   |
    Then the courseware annotation "one" is created successfully
    And "Alice" has granted "REVIEWER" permission level to "account" "Charlie" over project "TRO"
    When "Charlie" updates a courseware annotation "one" with motivation "identifying" for element "SCREEN_ONE" of type "INTERACTIVE" and rootElement "UNIT_ONE" with
      | target | {"json":"target1"} |
      | body   | {"json":"body1"}   |
    Then the courseware annotation update fails due to missing permission level

  Scenario: A user with no permission should not be able to fetch a courseware annotation
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    When "Bob" creates a courseware "commenting" annotation through rtm for element "SCREEN_ONE" of type "INTERACTIVE" and rootElement "UNIT_ONE" with
      | target | {"json":"target"} |
      | body   | {"json":"body"}   |
    Then the courseware annotation "one" is created successfully
    When "Charlie" tries to fetch fields from courseware annotation "one" with motivation "commenting" for element "SCREEN_ONE" of type "INTERACTIVE" and rootElement "UNIT_ONE"
    Then the courseware annotation fetch fails due to missing permission level

  Scenario: A user with Contributor permission and identifying motivation should be able to delete a courseware annotation
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    When "Bob" creates a courseware "identifying" annotation through rtm for element "SCREEN_ONE" of type "INTERACTIVE" and rootElement "UNIT_ONE" with
      | target | {"json":"target"} |
      | body   | {"json":"body"}   |
    Then the courseware annotation "one" is created successfully
    When "Bob" deletes annotation "one" of courseware element "SCREEN_ONE" of type "INTERACTIVE"
    Then the courseware annotation "one" is deleted successfully

  Scenario: A user with Reviewer permission and identifying motivation should not be able to delete a courseware annotation
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    When "Bob" creates a courseware "identifying" annotation through rtm for element "SCREEN_ONE" of type "INTERACTIVE" and rootElement "UNIT_ONE" with
      | target | {"json":"target"} |
      | body   | {"json":"body"}   |
    Then the courseware annotation "one" is created successfully
    And a workspace account "Charlie" is created
    When "Charlie" deletes annotation "one" of courseware element "SCREEN_ONE" of type "INTERACTIVE"
    Then the courseware annotation delete fails for "one" due to missing permission level

  Scenario: A user with Contributor permission should be able get a courseware annotation
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    When "Bob" creates a courseware "identifying" annotation through rtm for element "SCREEN_ONE" of type "INTERACTIVE" and rootElement "UNIT_ONE" with
      | target | {"json":"target"} |
      | body   | {"json":"body"}   |
    Then the courseware annotation "one" is created successfully
    Then "Bob" can get following fields from courseware annotation "one" for element "SCREEN_ONE" and type "INTERACTIVE"
      | motivation | identifying|
      | target     | target     |
      | body       | body       |

  Scenario: A user with no permission should not be able to get a courseware annotation
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    When "Bob" creates a courseware "commenting" annotation through rtm for element "SCREEN_ONE" of type "INTERACTIVE" and rootElement "UNIT_ONE" with
      | target | {"json":"target"} |
      | body   | {"json":"body"}   |
    Then the courseware annotation "one" is created successfully
    When "Charlie" tries to get fields from courseware annotation "one" for element "SCREEN_ONE" of type "INTERACTIVE"
    Then the courseware annotation get fails for annotaion "one" due to missing permission level

  Scenario: A user with REVIEWER permission should be able to resolve a courseware annotation
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created a courseware "commenting" annotation "ONE" for element "SCREEN_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a courseware "commenting" annotation "TWO" for element "LINEAR_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    When "Bob" resolve courseware annotations for rootElement "UNIT_ONE" with value "true"
      | ONE |
      | TWO |
    Then the courseware annotations are resolved successfully

  Scenario: A user with REVIEWER permission should be able to read a courseware annotation
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created a courseware "commenting" annotation "ONE" for element "SCREEN_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a courseware "commenting" annotation "TWO" for element "SCREEN_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    When "Bob" reads courseware annotations for rootElement "UNIT_ONE" and element "SCREEN_ONE" of type "INTERACTIVE" with value "true"
      | ONE |
      | TWO |
    Then the courseware annotations are read successfully
    When "Bob" tries to get fields from courseware annotation "ONE" for element "SCREEN_ONE" of type "INTERACTIVE"
    Then the courseware annotation "ONE" is fetched successfully with read value "true"

  Scenario: A user with REVIEWER permission should be able to unread a courseware annotation
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created a courseware "commenting" annotation "ONE" for element "SCREEN_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a courseware "commenting" annotation "TWO" for element "SCREEN_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    When "Bob" reads courseware annotations for rootElement "UNIT_ONE" and element "SCREEN_ONE" of type "INTERACTIVE" with value "true"
      | ONE |
      | TWO |
    Then the courseware annotations are read successfully
    When "Bob" tries to get fields from courseware annotation "ONE" for element "SCREEN_ONE" of type "INTERACTIVE"
    Then the courseware annotation "ONE" is fetched successfully with read value "true"
    When "Bob" reads courseware annotations for rootElement "UNIT_ONE" and element "SCREEN_ONE" of type "INTERACTIVE" with value "false"
      | ONE |
      | TWO |
    Then the courseware annotations are read successfully
    When "Bob" tries to get fields from courseware annotation "ONE" for element "SCREEN_ONE" of type "INTERACTIVE"
    Then the courseware annotation "ONE" is fetched successfully with read value "false"

  Scenario: A user with REVIEWER permission should be able to aggregate a courseware annotation
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has created a courseware "commenting" annotation "ONE" for element "SCREEN_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a courseware "commenting" annotation "TWO" for element "LINEAR_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    When "Bob" resolve courseware annotations for rootElement "UNIT_ONE" with value "true"
      | ONE |
      | TWO |
    Then the courseware annotations are resolved successfully
    When "Bob" aggregate courseware annotations for rootElement "UNIT_ONE"
    Then the courseware annotations are aggregated successfully with following values
      | total      | 2 |
      | read       | 0 |
      | unRead     | 2 |
      | resolved   | 2 |
      | unResolved | 0 |
