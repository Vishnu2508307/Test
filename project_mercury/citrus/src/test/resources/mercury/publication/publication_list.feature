Feature: Fetching a list of publication

  Scenario: Got an error on fetching publications if not authenticated
    Given a user is not logged in
    When user requests a list of publication
    Then mercury should respond with: "publication.list.request.error" and code "401"

  Scenario: Fetch a list of publications
    Given a workspace account "Alice" is created
    Given "Alice" has created publication "Test publication"
    Given "Alice" has created publication "Test publication 01"
    Given "Alice" has created publication "Test publication 02"
    Then "Alice" should fetch a list of publications including
      | Test publication |
      | Test publication 01 |
      | Test publication 02 |

