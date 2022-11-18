Feature: Testing GraphQL servlet

  Scenario: Request a GraphQL ping not authorized
    When "Anon" requests via http "/graphql" with query parameter named "query" with value "{ ping { result } }"
    Then the server should respond with http status "401" "NOT_AUTHORIZED" and error message "Bearer token not supplied"

  Scenario: Request a GraphQL ping
    Given "Alice" is logged in
    When "Alice" requests via http "/graphql" with query parameter named "query" with value "{ ping { result } }"
    Then the server should respond with a response body of '{"data":{"ping":{"result":"pong"}}}'
