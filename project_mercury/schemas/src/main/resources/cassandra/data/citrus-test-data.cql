--
--     _____________________________________________________________________________________
--    /                                                                                     \
--    | The purpose of this file is to run queries prior to the execution of citrus tests.  |
--    |                                                                                     |
--    | It must only be used to perform operations which are not possible using the API!!   |
--    |                                                                                     |
--    \_______________________________________________________________________________  __'\
--                                                                                    |/   \\
--                                                                                     \    \\  .
--                                                                                          |\\/|
--                                                                                          / " '\
--                                                                                          . .   .
--                                                                                         /    ) |
--                                                                                        '  _.'  |
--                                                                                        '-'/    \
--

--
--  [TICKET-####] Justification
--  QUERY
--
--  [PLT-5290] Create a support role user for integration test purposes in the data.cql file
--  email: 'support@citrus.dev', password: 'password'
--  QUERIES
INSERT INTO iam_global.subscription (id, name, iam_region)
VALUES (ac0baf30-c477-11e9-8fae-43bc3cbb0b3f, 'Citrus Test Subscription', 'GLOBAL');

INSERT INTO iam_global.account (id, subscription_id, iam_region, status, roles, password_hash, password_expired)
VALUES (57dbfa40-c49b-11e9-8623-39aed6204ec3, ac0baf30-c477-11e9-8fae-43bc3cbb0b3f, 'GLOBAL', 'ENABLED', {'ADMIN','DEVELOPER','INSTRUCTOR','STUDENT', 'SUPPORT'}, 'PBKDF2:86ef78fc06d2a27a91e7da92660657052d1521d1:00fcdea890c1bb220cdd0a9f80cc0850:32768', false);

INSERT INTO iam_global.account_identity_attribute (account_id, iam_region, subscription_id, given_name, family_name, honorific_prefix, email, primary_email)
VALUES (57dbfa40-c49b-11e9-8623-39aed6204ec3, 'GLOBAL', ac0baf30-c477-11e9-8fae-43bc3cbb0b3f, 'Citrus', 'Client', 'Mr.', {'support@citrus.dev'}, 'support@citrus.dev');

INSERT INTO iam_global.account_by_hash (hash, account_id, iam_region)
VALUES ('78b20eb31ab0f10e5b0b635954bb56dab0ea98c3300a103cc899aeff26e49f30', 57dbfa40-c49b-11e9-8623-39aed6204ec3, 'GLOBAL');

INSERT INTO iam_global.account_shadow_attribute (account_id, name, value, iam_region)
VALUES (57dbfa40-c49b-11e9-8623-39aed6204ec3, 'AERO_ACCESS', '', 'GLOBAL');

INSERT INTO iam_global.account_shadow_attribute (account_id, name, value, iam_region)
VALUES (57dbfa40-c49b-11e9-8623-39aed6204ec3, 'PROVISION_SOURCE', 'LEGACY_UNKNOWN', 'GLOBAL');
