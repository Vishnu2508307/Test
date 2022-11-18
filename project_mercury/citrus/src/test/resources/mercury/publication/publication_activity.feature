Feature: Fetching a list of activities

  Scenario: Got an error on fetching activities if not authenticated
    Given a user is not logged in
    When user requests a list of activity
    Then mercury should respond with: "publication.activity.fetch.error" and code "401"

  Scenario: Fetch a list of published activity
    Given a workspace account "Alice" is created
    And "Alice" has created and published course plugin
    And "Alice" has created workspace "One"
    And "Alice" has created project "Two" in workspace "One"
    And "Alice" has created activity "Three" inside project "Two" with id
    Given "Alice" has created publication "publication_test" for the activity
    Then "Alice" should fetch a list of activity in workspace "One" including
      | Three |