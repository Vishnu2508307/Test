Feature: Create a student and instructor accounts

  Scenario: Create an account successfully
    Given A message type iam.student.provision
    And message contains "email" "mercury:randomEmail()"
    And message contains "password" "password"
    When I post the message to mercury
    Then a user should be created
    And have roles: "STUDENT"

  Scenario: Create an instructor successfully
    Given A message type iam.instructor.provision
    And message contains "email" "mercury:randomEmail()"
    And message contains "password" "password"
    When I post the message to mercury
    Then a user should be created
    And have roles: "ADMIN, INSTRUCTOR, DEVELOPER, STUDENT, AERO_INSTRUCTOR"

  Scenario: Get an account exists exception
    Given A message type iam.instructor.provision
    And a random user is already created
    When I try to create an account for this random user again
    Then mercury should respond with: "iam.instructor.provision.error"

  Scenario: Provision a user within the same subscription
    Given an ADMIN user with subscription OWNER permission is logged in
    When I provision a new user within the same subscription
    Then a user should be created

  Scenario: It should not allow to provision users with an invalid role
    Given an account "Alice" is created
    When "Alice" try to provision a new user "Chuck" within the same subscription with roles
      | SUPPORT |
    Then the account is not provisioned
    When "Alice" try to provision a new user "Chuck" within the same subscription with roles
      | INSTRUCTOR |
    Then the account is not provisioned

