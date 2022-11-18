Feature: Creating a publication

  Scenario: Got an error on fetching publications if not authenticated
    Given a user is not logged in
    When "Bob" requests to create a publication "Test publication"
    Then mercury should respond with: "publication.create.request.error" and code "401"

  Scenario: The workspace OWNER should be able to create a publication
    Given a workspace account "Alice" is created
    When "Alice" requests to create a publication "Test publication"
    Then the publication is successfully created

  Scenario: The workspace OWNER should be able to create a publication with Output Type "BRONTE_PEARSON_PLUS"
    Given a workspace account "Alice" is created
    When "Alice" requests to create a publication "Test publication" with Output Type "BRONTE_PEARSON_PLUS"
    Then the publication is successfully created

  Scenario: The workspace OWNER should be able to create a publication with Output Type "BRONTE_CLASSES_ON_DEMAND"
    Given a workspace account "Alice" is created
    When "Alice" requests to create a publication "Test publication" with Output Type "BRONTE_CLASSES_ON_DEMAND"
    Then the publication is successfully created

  Scenario: The workspace OWNER should be able to update a published course title
    Given a workspace account "Alice" is created
    When "Alice" requests to create a publication "Test publication" with Output Type "BRONTE_CLASSES_ON_DEMAND"
    Then the publication is successfully created
    When "Alice" can update published course title "Test publication" with "Test publication update" with activityId "96c81c2c-4e9f-4f8c-b095-9dd1e17dcad2"
    Then the publication title is successfully updated
