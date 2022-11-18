Feature: Fetch the publication status from oculus

  Scenario: Got an error on fetching publication status if not authenticated
    Given a user is not logged in
    When "Bob" requests to fetch the publication status for "BRNT-D8UZ24XCZIW-REV"
    Then mercury should respond with: "publication.oculus.status.error" and code "401"

  Scenario: The workspace OWNER should be able to fetch the publication status from oculus
    Given a workspace account "Alice" is created
    When "Alice" requests to fetch the publication status for "BRNT-D8UZ24XCZIW-REV"
    Then the publication status has been fetched successfully
