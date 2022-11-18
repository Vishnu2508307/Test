Feature: Process an LTI v1.1 Launch Request

  Background:
    Given an account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "LESSON_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway named "PATHWAY_ONE" for the "LESSON_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "PATHWAY_ONE" pathway
    And "Alice" has LTI consumer credentials "LTI_CREDS_ONE" with key "KEY_ONE" and secret "SECRET_ONE" available
    And "Alice" has created a cohort "CLASS_ONE" with values
      | name           | Alice's cohort          |
      | enrollmentType | LTI                     |
      | workspaceId    | ${one_workspace_id}     |
      | ltiKey         | ${KEY_ONE_ltiKey}       |
      | ltiSecret      | ${SECRET_ONE_ltiSecret} |
    And "Alice" has created a cohort "CLASS_TWO" with values
      | name           | Alice's cohort          |
      | enrollmentType | LTI                     |
      | workspaceId    | ${one_workspace_id}     |
      | ltiKey         | ${KEY_ONE_ltiKey}       |
      | ltiSecret      | ${SECRET_ONE_ltiSecret} |
    And "Alice" has published "LESSON_ONE" activity to cohort "CLASS_ONE" as "DEPLOYMENT_ONE"
    And "Alice" has published "LESSON_ONE" activity to cohort "CLASS_TWO" as "DEPLOYMENT_TWO"

#  Scenario: POST a LTI Launch Request missing lti_message_type
#    Given an LTI Launch Request to "http://some-tld.pearson.com" for cohort "CLASS_ONE" and activity "LESSON_ONE" with "KEY_ONE" and "SECRET_ONE" with parameters
#      | lti_version                      | LTI-1p0                 |
#      | resource_link_id                 | ${__generate}           |
#      | user_id                          | ${__generate}           |
#      | lis_person_contact_email_primary | homer@${__generate}.tld |
#      | lis_person_name_given            | Homer Jay               |
#      | lis_person_name_family           | Simpson                 |
#      | lis_person_name_full             | Homer Jay Simpson       |
#    When the LTI Launch Request is submitted with a "valid" pi session
#    Then the response status code is "BAD_REQUEST"
#    And the response body contains "missing required field lti_message_type"
#
#  Scenario: POST an LTI Launch Request should enroll the user and redirect
#    Given an LTI Launch Request to "http://some-tld.pearson.com" for cohort "CLASS_ONE" and activity "LESSON_ONE" with "KEY_ONE" and "SECRET_ONE" with parameters
#      | lti_version                      | LTI-1p0                  |
#      | lti_message_type                 | basic-lti-launch-request |
#      | resource_link_id                 | ${__generate}            |
#      | user_id                          | ${__generate}            |
#      | lis_person_contact_email_primary | homer@${__generate}.tld  |
#      | lis_person_name_given            | Homer Jay                |
#      | lis_person_name_family           | Simpson                  |
#      | lis_person_name_full             | Homer Jay Simpson        |
#    When the LTI Launch Request is submitted with a "valid" pi session
#    Then the response status code is "SEE_OTHER"
#    And the response has a header "location" starting with "http://some-tld.pearson.com/"
#    And the response redirects to url "http://some-tld.pearson.com" with
#      | CLASS_ONE  |
#      | LESSON_ONE |
#    And the response has a header "set-cookie" starting with "bearerToken="
#
#  Scenario: POST an LTI launch request with invalid signature
#    Given "Alice" has LTI consumer credentials "INVALID_LTI_CREDS_ONE" with key "INVALID_KEY_ONE" and secret "INVALID_SECRET_ONE" available
#    Given an LTI Launch Request to "http://some-tld.pearson.com" for cohort "CLASS_ONE" and activity "LESSON_ONE" with "INVALID_KEY_ONE" and "INVALID_SECRET_ONE" with parameters
#      | lti_version                      | LTI-1p0                  |
#      | lti_message_type                 | basic-lti-launch-request |
#      | resource_link_id                 | ${__generate}            |
#      | user_id                          | ${__generate}            |
#      | lis_person_contact_email_primary | homer@${__generate}.tld  |
#      | lis_person_name_given            | Homer Jay                |
#      | lis_person_name_family           | Simpson                  |
#      | lis_person_name_full             | Homer Jay Simpson        |
#    When the LTI Launch Request is submitted with a "valid" pi session
#    Then the response status code is "BAD_REQUEST"
#    And the response body contains "signature_invalid"
#
#  Scenario: POST an LTI Launch Request, as a federated login
#    Given an LTI Launch Request to "http://some-tld.pearson.com" for cohort "CLASS_ONE" and activity "LESSON_ONE" with "KEY_ONE" and "SECRET_ONE" with parameters
#      | lti_version                      | LTI-1p0                  |
#      | lti_message_type                 | basic-lti-launch-request |
#      | resource_link_id                 | ${__generate}            |
#      | user_id                          | SP-937-215               |
#      | lis_person_contact_email_primary | picard@starfleet.tld     |
#      | lis_person_name_given            | Jean-Luc                 |
#      | lis_person_name_family           | Picard                   |
#    When the LTI Launch Request is submitted with a "valid" pi session
#    Then the response status code is "SEE_OTHER"
#    And the response has a header "location" starting with "http://some-tld.pearson.com"
#    And the response redirects to url "http://some-tld.pearson.com" with
#      | CLASS_ONE  |
#      | LESSON_ONE |
#    And the response has a header "set-cookie" starting with "bearerToken="
#    # continue, this will test the federation
#    Given an LTI Launch Request to "http://some-tld.pearson.com" for cohort "CLASS_ONE" and activity "LESSON_ONE" with "KEY_ONE" and "SECRET_ONE" with parameters
#      | lti_version                      | LTI-1p0                  |
#      | lti_message_type                 | basic-lti-launch-request |
#      | resource_link_id                 | ${__generate}            |
#      | user_id                          | SP-937-215               |
#      | lis_person_contact_email_primary | picard@starfleet.tld     |
#      | lis_person_name_given            | Jean-Luc                 |
#      | lis_person_name_family           | Picard                   |
#    When the LTI Launch Request is submitted with a "valid" pi session
#    Then the response status code is "SEE_OTHER"
#    And the response has a header "location" starting with "http://some-tld.pearson.com"
#    And the response redirects to url "http://some-tld.pearson.com" with
#      | CLASS_ONE  |
#      | LESSON_ONE |
#    And the response has a header "set-cookie" starting with "bearerToken="
#
#  #
#  # The following tests assert that the correct user info is supplied in the launch request.
#  #
#
#  Scenario: POST an LTI Launch Request missing required user_id
#    Given an LTI Launch Request to "http://some-tld.pearson.com" for cohort "CLASS_ONE" and activity "LESSON_ONE" with "KEY_ONE" and "SECRET_ONE" with parameters
#      | lti_version                      | LTI-1p0                  |
#      | lti_message_type                 | basic-lti-launch-request |
#      | resource_link_id                 | ${__generate}            |
#      | lis_person_contact_email_primary | homer@${__generate}.tld  |
#      | lis_person_name_given            | Homer Jay                |
#      | lis_person_name_family           | Simpson                  |
#      | lis_person_name_full             | Homer Jay Simpson        |
#    When the LTI Launch Request is submitted with a "valid" pi session
#    Then the response status code is "BAD_REQUEST"
#    And the response body contains "missing required field user_id"
#
#  Scenario: POST an LTI Launch Request, should fail when the domain is not a .pearson.com
#    Given an LTI Launch Request to "http://some-tld.foo.com" for cohort "CLASS_ONE" and activity "LESSON_ONE" with "KEY_ONE" and "SECRET_ONE" with parameters
#      | lti_version                      | LTI-1p0                  |
#      | lti_message_type                 | basic-lti-launch-request |
#      | resource_link_id                 | ${__generate}            |
#      | user_id                          | ${__generate}            |
#      | lis_person_contact_email_primary | homer@${__generate}.tld  |
#      | lis_person_name_given            | Homer Jay                |
#      | lis_person_name_family           | Simpson                  |
#      | lis_person_name_full             | Homer Jay Simpson        |
#    When the LTI Launch Request is submitted with a "valid" pi session
#    Then the response status code is "BAD_REQUEST"
#    And the response body contains "Invalid continue_to url parameter"

  Scenario: POST an LTI Launch Request without a valid session should initialise the session and proceed
    Given an LTI Launch Request to "http://some-tld.pearson.com" for cohort "CLASS_ONE" and activity "LESSON_ONE" with "KEY_ONE" and "SECRET_ONE" with parameters
      | lti_version                      | LTI-1p0                  |
      | lti_message_type                 | basic-lti-launch-request |
      | resource_link_id                 | ${__generate}            |
      | user_id                          | ${__generate}            |
      | lis_person_contact_email_primary | homer@${__generate}.tld  |
      | lis_person_name_given            | Homer Jay                |
      | lis_person_name_family           | Simpson                  |
      | lis_person_name_full             | Homer Jay Simpson        |
      | custom_lab_id                    | ${__generate}            |
    When the LTI Launch Request is submitted with a "missing" pi session
    Then the response status code is "OK"
    And the response has a header "Content-type" starting with "text/html"
    And the response body contains
      | launchRequestId | LAUNCH_REQUEST_ID_ONE |
      | hash            | HASH_ONE              |
    When a pi session is initialised for LTI hash "HASH_ONE" and launch "LAUNCH_REQUEST_ID_ONE"
    And the response redirects to url "http://some-tld.pearson.com" with
      | CLASS_ONE  |
      | LESSON_ONE |

  Scenario: POST an LTI Launch Request should enroll the user if not exists and redirect
    Given an LTI Launch Request to "http://some-tld.pearson.com" for cohort "CLASS_ONE" and activity "LESSON_ONE" with "KEY_ONE" and "SECRET_ONE" with parameters
      | lti_version                      | LTI-1p0                  |
      | lti_message_type                 | basic-lti-launch-request |
      | resource_link_id                 | ${__generate}            |
      | user_id                          | ${__generate}            |
      | lis_person_contact_email_primary | homer@${__generate}.tld  |
      | lis_person_name_given            | Homer Jay                |
      | lis_person_name_family           | Simpson                  |
      | lis_person_name_full             | Homer Jay Simpson        |
      | custom_lab_id                    | ${__generate}            |
    When the LTI Launch Request is submitted with a "valid" pi session
    Then the response status code is "SEE_OTHER"
    And the response has a header "location" starting with "http://some-tld.pearson.com/"
    And the response redirects to url "http://some-tld.pearson.com" with
      | CLASS_ONE  |
      | LESSON_ONE |
    When "Alice" lists the enrollments for the cohort "CLASS_ONE"
    Then "Alice" shows up in the list of enrolled users for the cohort "CLASS_ONE"
    When the LTI Launch Request is submitted with a "valid" pi session
    Then the response status code is "SEE_OTHER"
    And the response has a header "location" starting with "http://some-tld.pearson.com/"
    And the response redirects to url "http://some-tld.pearson.com" with
      | CLASS_ONE  |
      | LESSON_ONE |
    When "Alice" lists the enrollments for the cohort "CLASS_ONE"
    Then "Alice" shows up in the list of enrolled users for the cohort "CLASS_ONE"

  Scenario: POST an LTI Launch Request should succeed for returning user with invalid piSession
    Given an LTI Launch Request to "http://some-tld.pearson.com" for cohort "CLASS_ONE" and activity "LESSON_ONE" with "KEY_ONE" and "SECRET_ONE" with parameters
      | lti_version                      | LTI-1p0                  |
      | lti_message_type                 | basic-lti-launch-request |
      | resource_link_id                 | ${__generate}            |
      | user_id                          | ${__generate}            |
      | lis_person_contact_email_primary | homer@${__generate}.tld  |
      | lis_person_name_given            | Homer Jay                |
      | lis_person_name_family           | Simpson                  |
      | lis_person_name_full             | Homer Jay Simpson        |
      | custom_lab_id                    | ${__generate}            |
    When the LTI Launch Request is submitted with a "valid" pi session
    Then the response status code is "SEE_OTHER"
    And the response has a header "location" starting with "http://some-tld.pearson.com/"
    And the response redirects to url "http://some-tld.pearson.com" with
      | CLASS_ONE  |
      | LESSON_ONE |
    When "Alice" lists the enrollments for the cohort "CLASS_ONE"
    Then "Alice" shows up in the list of enrolled users for the cohort "CLASS_ONE"
    Given an LTI Launch Request to "http://some-tld.pearson.com" for cohort "CLASS_TWO" and activity "LESSON_ONE" with "KEY_ONE" and "SECRET_ONE" with parameters
      | lti_version                      | LTI-1p0                  |
      | lti_message_type                 | basic-lti-launch-request |
      | resource_link_id                 | ${__generate}            |
      | user_id                          | previous_user            |
      | lis_person_contact_email_primary | homer@${__generate}.tld  |
      | lis_person_name_given            | Homer Jay                |
      | lis_person_name_family           | Simpson                  |
      | lis_person_name_full             | Homer Jay Simpson        |
      | custom_lab_id                    | ${__generate}            |
    When the LTI Launch Request is submitted with a "missing" pi session
    Then the response status code is "SEE_OTHER"
    And the response has a header "location" starting with "http://some-tld.pearson.com/"
    And the response redirects to url "http://some-tld.pearson.com" with
      | CLASS_TWO  |
      | LESSON_ONE |
    When "Alice" lists the enrollments for the cohort "CLASS_TWO"
    Then "Alice" shows up in the list of enrolled users for the cohort "CLASS_TWO"