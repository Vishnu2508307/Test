Feature: Permission checks for courseware messages

  Background:
    Given workspace accounts are created
      | Alice |
      | Bob   |
    And "Alice" has created workspace "workspace"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "workspace"
    And "Alice" has created activity "UNIT" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway for the "UNIT" activity
    And "Alice" has created a "LESSON" activity for the "LINEAR" pathway
    And "Alice" has created a "SCREEN" interactive for the "LINEAR" pathway
    And "Alice" has created a "SUCCESS" feedback for the "SCREEN" interactive
    And "Alice" has created a "TEXT" component for the "SCREEN" interactive
    And "Alice" has created a "VIDEO" component for the "LESSON" activity

  Scenario: The user without permissions on workspace should not be able to create/delete/update/fetch courseware elements
    Then "Bob" can not create an activity for "LINEAR" pathway due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can not save config for "UNIT" activity due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can not save theme for "UNIT" activity due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can not delete "LESSON" activity for "LINEAR" pathway due to error: code 401 message "Unauthorized: Unauthorized permission level"

    Then "Bob" can not create a pathway for "UNIT" activity due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can not delete "LINEAR" pathway for "UNIT" activity due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can not fetch "LINEAR" pathway due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can not reorder "LESSON" walkable inside "LINEAR" pathway due to error: code 401 message "Unauthorized: Unauthorized permission level"

    Then "Bob" can not create an interactive for "LINEAR" pathway due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can not delete "SCREEN" interactive for "LINEAR" pathway due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can not fetch "SCREEN" interactive due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can not save config for "SCREEN" interactive due to error: code 401 message "Unauthorized: Unauthorized permission level"

    Then "Bob" can not create a feedback for "SCREEN" interactive due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can not delete "SUCCESS" feedback for "SCREEN" interactive due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can not fetch "SUCCESS" feedback due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can not save config for "SUCCESS" feedback due to error: code 401 message "Unauthorized: Unauthorized permission level"

    Then "Bob" can not create a component for "SCREEN" interactive due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can not delete "TEXT" component from "SCREEN" interactive due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can not fetch "TEXT" component due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can not save config for "TEXT" component due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can not create a component for "LESSON" activity due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can not delete "VIDEO" component from "LESSON" activity due to error: code 401 message "Unauthorized: Unauthorized permission level"

    Then "Bob" can not fetch a breadcrumb for LINEAR pathway due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can not subscribe to "UNIT" activity events due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can not unsubscribe from "UNIT" activity events due to error: code 401 message "Unauthorized: Unauthorized permission level"

  Scenario: The user with REVIEWER permissions on workspace should be able only to fetch courseware elements
    When "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "workspace"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    Then "Bob" can not create an activity for "LINEAR" pathway due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can not save config for "UNIT" activity due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can not save theme for "UNIT" activity due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can not delete "LESSON" activity for "LINEAR" pathway due to error: code 401 message "Unauthorized: Unauthorized permission level"

    Then "Bob" can not create a pathway for "UNIT" activity due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can not delete "LINEAR" pathway for "UNIT" activity due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can fetch "LINEAR" pathway successfully
    Then "Bob" can not reorder "LESSON" walkable inside "LINEAR" pathway due to error: code 401 message "Unauthorized: Unauthorized permission level"

    Then "Bob" can not create an interactive for "LINEAR" pathway due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can not delete "SCREEN" interactive for "LINEAR" pathway due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can fetch "SCREEN" interactive successfully
    Then "Bob" can not save config for "SCREEN" interactive due to error: code 401 message "Unauthorized: Unauthorized permission level"

    Then "Bob" can not create a feedback for "SCREEN" interactive due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can not delete "SUCCESS" feedback for "SCREEN" interactive due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can fetch "SUCCESS" feedback successfully
    Then "Bob" can not save config for "SUCCESS" feedback due to error: code 401 message "Unauthorized: Unauthorized permission level"

    Then "Bob" can not create a component for "SCREEN" interactive due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can not delete "TEXT" component from "SCREEN" interactive due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can fetch "TEXT" component successfully
    Then "Bob" can not save config for "TEXT" component due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can not create a component for "LESSON" activity due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Then "Bob" can not delete "VIDEO" component from "LESSON" activity due to error: code 401 message "Unauthorized: Unauthorized permission level"

    Then "Bob" can subscribe to "UNIT" activity events successfully
    Then "Bob" can unsubscribe from "UNIT" activity events successfully

  #note: In the following scenario delete steps should be the last steps otherwise they will delete elements and it will not possible to perform checks
  Scenario: The user with CONTRIBUTOR permissions on workspace should be able to create/delete/update/fetch activities
    When "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "workspace"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    Then "Bob" can create an activity for "LINEAR" pathway successfully
    Then "Bob" can save config for "UNIT" activity successfully
    Then "Bob" can save theme for "UNIT" activity successfully
    Then "Bob" can delete "LESSON" activity for "LINEAR" pathway successfully

  #Note: the order of steps are important here. Can be improved later
  Scenario: The user with CONTRIBUTOR permissions on workspace should be able to create/delete/update/fetch pathways
    When "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "workspace"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    Then "Bob" can reorder "LESSON,SCREEN" walkables inside "LINEAR" pathway successfully
    Then "Bob" can create a pathway for "UNIT" activity successfully
    Then "Bob" can fetch "LINEAR" pathway successfully
    Then "Bob" can delete "LINEAR" pathway for "UNIT" activity successfully

  Scenario: The user with CONTRIBUTOR permissions on workspace should be able to create/delete/update/fetch interactive elements
    When "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "workspace"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    Then "Bob" can create an interactive for "LINEAR" pathway successfully
    Then "Bob" can fetch "SCREEN" interactive successfully
    Then "Bob" can save config for "SCREEN" interactive successfully
    Then "Bob" can delete "SCREEN" interactive for "LINEAR" pathway successfully

  Scenario: The user with CONTRIBUTOR permissions on workspace should be able to create/delete/update/fetch feedback elements
    When "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "workspace"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    Then "Bob" can create a feedback for "SCREEN" interactive successfully
    Then "Bob" can fetch "SUCCESS" feedback successfully
    Then "Bob" can save config for "SUCCESS" feedback successfully
    Then "Bob" can delete "SUCCESS" feedback for "SCREEN" interactive successfully

  Scenario: The user with CONTRIBUTOR permissions on workspace should be able to create/delete/update/fetch components
    When "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "workspace"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    Then "Bob" can create a component for "SCREEN" interactive successfully
    Then "Bob" can fetch "TEXT" component successfully
    Then "Bob" can save config for "TEXT" component successfully
    Then "Bob" can create a component for "LESSON" activity successfully
    Then "Bob" can delete "TEXT" component from "SCREEN" interactive successfully
    Then "Bob" can delete "VIDEO" component from "LESSON" activity successfully




