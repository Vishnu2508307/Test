Feature: Fetching a list of publications for the activity

  Scenario: Got an error on fetching publications if not authenticated
    Given a user is not logged in
    When user requests a list of publications for a activity
    Then mercury should respond with: "publication.history.fetch.error" and code "401"

  Scenario: Fetch a list of publications of the activity
    Given a workspace account "Alice" is created
    Given "Alice" has created publication "publication001" for activity "b2e905d0-91eb-11eb-9a79-2d567e2fc555" with version "1.0"
    Given "Alice" has created publication "publication002" for activity "b2e905d0-91eb-11eb-9a79-2d567e2fc555" with version "1.0"
    Given "Alice" has created publication "publication003" for activity "b2e905d0-91eb-11eb-9a79-2d567e2fc555" with version "1.0"
    Then "Alice" should fetch a list of publications of the activity "b2e905d0-91eb-11eb-9a79-2d567e2fc555" including
      | publication001 |
      | publication002 |
      | publication003 |

  Scenario: Fetch a list of publications of the activity after deleting
    Given a workspace account "Alice" is created
    Given "Alice" has created publication "publication001" for activity "b2e905d0-91eb-11eb-9a79-2d567e2fc555" with version "1.0"
    Given "Alice" has created publication "publication002" for activity "b2e905d0-91eb-11eb-9a79-2d567e2fc555" with version "1.0"
    Given "Alice" has created publication "publication003" for activity "b2e905d0-91eb-11eb-9a79-2d567e2fc555" with version "1.0"
    When "Alice" has deleted publication "publication003" for activity "b2e905d0-91eb-11eb-9a79-2d567e2fc555" with version "1.0"
    Then "Alice" should fetch a list of publications of the activity "b2e905d0-91eb-11eb-9a79-2d567e2fc555" including
      | publication001 |
      | publication002 |