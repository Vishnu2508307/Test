Feature: User avatar uploading

  Scenario: User uploads avatar successfully
    Given a new account "User1" is created
    When "User1" uploads avatar
    Then me.get message returns smallAvatar

  Scenario: User gets error message if avatar image has incorrect format
    Given a new account "User1" is created
    When "User1" uploads broken avatar image
    Then mercury should respond with: "me.avatar.set.error" and code "400" and reason "Invalid format for avatar field"

  Scenario: User gets error message if avatar field is empty
    Given a new account "User1" is created
    When "User1" uploads empty avatar
    Then mercury should respond with: "me.avatar.set.error" and code "400" and reason "Avatar is missing"


