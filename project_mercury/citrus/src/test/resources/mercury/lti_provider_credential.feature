Feature: Create lti credentials and sign a lti params via graphql

  Background:
    Given a support role account "Alice" exists
    And an account "Bob" is created
    And "Bob" has created and published plugin "LTI_PLUGIN"

  Scenario: A support role user should be able to create lti credentials for a plugin
    When "Alice" creates lti provider credentials for plugin "LTI_PLUGIN" with
      | name            | LtiProviderCredentials                                                                                                          |
      | key             | jisc.ac.uk                                                                                                                      |
      | secret          | secret                                                                                                                          |
      | whitelistFields | "user_id,role,context_id,context_label,context_title,custom_CRN,custom_Module,custom_content_guid,lti_version,lti_message_type" |
    Then the lti provider credentials "LTI_CREDENTIALS" are created for "LTI_PLUGIN"

  Scenario: A non support role user should not be able to create lti credentials for a plugin
    When "Bob" creates lti provider credentials for plugin "LTI_PLUGIN" with
      | name            | LtiProviderCredentials                                                                                                          |
      | key             | jisc.ac.uk                                                                                                                      |
      | secret          | secret                                                                                                                          |
      | whitelistFields | "user_id,role,context_id,context_label,context_title,custom_CRN,custom_Module,custom_content_guid,lti_version,lti_message_type" |
    Then the lti provider credentials are not "create" due to missing permission level

  Scenario: A support role user should be able to delete lti credentials for a plugin
    Given "Alice" has created lti provider credentials "CREDS_ONE" for plugin "LTI_PLUGIN" with
      | name            | LtiProviderCredentials                                                                                                          |
      | key             | jisc.ac.uk                                                                                                                      |
      | secret          | secret                                                                                                                          |
      | whitelistFields | "user_id,role,context_id,context_label,context_title,custom_CRN,custom_Module,custom_content_guid,lti_version,lti_message_type" |
    When "Alice" deletes lti credentials "CREDS_ONE" for plugin "LTI_PLUGIN"
    Then the lti credentials are deleted successfully

  Scenario: A non support role user should not be able to delete lti credentials for a plugin
    Given "Alice" has created lti provider credentials "CREDS_ONE" for plugin "LTI_PLUGIN" with
      | name            | LtiProviderCredentials                                                                                                          |
      | key             | jisc.ac.uk                                                                                                                      |
      | secret          | secret                                                                                                                          |
      | whitelistFields | "user_id,role,context_id,context_label,context_title,custom_CRN,custom_Module,custom_content_guid,lti_version,lti_message_type" |
    When "Bob" deletes lti credentials "CREDS_ONE" for plugin "LTI_PLUGIN"
    Then the lti provider credentials are not "delete" due to missing permission level

  Scenario: A user should be able to sign an lti launch
    Given "Alice" has created lti provider credentials "CREDS_ONE" for plugin "LTI_PLUGIN" with
      | name            | LtiProviderCredentials                                                                                                          |
      | key             | jisc.ac.uk                                                                                                                      |
      | secret          | secret                                                                                                                          |
      | whitelistFields | "user_id,role,context_id,context_label,context_title,custom_CRN,custom_Module,custom_content_guid,lti_version,lti_message_type" |
    When "Bob" sends a GraphQL message
      """
      {
        pluginById(id: "${LTI_PLUGIN_id}") {
          lti {
            toolProvider {
              signLaunch(url: "http://lti.tools/saltire/tp",  key: "${CREDS_ONE_key}",
                params: [
                        {name: "user_id", value: "${Bob_id}"},
                        {name: "role", value: "LEARNER"},
                        {name: "context_id", value: "LtiProviderCredentials"},
                        {name: "context_label", value: "ltiProviderKey"},
                        {name: "context_title", value: "ltiProviderSecret"},
                        {name: "custom_CRN", value: "LtiProviderCredentials"},
                        {name: "custom_Module", value: "ltiProviderKey"},
                        {name: "custom_content_guid", value: "ltiProviderSecret"},
                        {name: "lti_version", value: "LTI-1p0"},
                        {name: "lti_message_type", value: "basic-lti-launch-request"}
                ]) {
                method
                url
                formParameters
              }
            }
          }
        }
      }
      """
  Then mercury should respond with a GraphQL response
   """
    {
      "pluginById": {
        "lti": {
          "toolProvider": {
            "signLaunch": {
              "method": "POST",
              "url": "http://lti.tools/saltire/tp",
              "formParameters": {
                "lti_version": "LTI-1p0",
                "role": "LEARNER",
                "oauth_signature": "@notEmpty()@",
                "context_title": "ltiProviderSecret",
                "custom_content_guid": "ltiProviderSecret",
                "oauth_consumer_key": "${CREDS_ONE_key}",
                "custom_CRN": "LtiProviderCredentials",
                "oauth_signature_method": "@notEmpty()@",
                "oauth_timestamp": "@notEmpty()@",
                "oauth_version": "1.0",
                "oauth_nonce": "@notEmpty()@",
                "custom_Module": "ltiProviderKey",
                "context_id": "LtiProviderCredentials",
                "context_label": "ltiProviderKey"
              }
            }
          }
        }
      }
    }
   """
