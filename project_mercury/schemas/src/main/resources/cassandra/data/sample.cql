-- setup a "sample" subscription.
INSERT INTO iam_global.subscription ( id, iam_region, name ) VALUES ( 721d5e6e-504f-11e9-8647-d663bd873d93, 'GLOBAL', 'Sample Subscription');

-- sample SPR Google OIDC
INSERT INTO iam_global.fidm_oidc_rp_credential ( relying_party_id , subscription_id , issuer_url , client_id , client_secret , authentication_request_scope, log_debug, enforce_verified_email)
    VALUES ( 6796c890-504f-11e9-8647-d663bd873d93, 721d5e6e-504f-11e9-8647-d663bd873d93, 'https://accounts.google.com', '599996276248-bctjf9e8la9ngt5jd6ste7lphf2e1oph.apps.googleusercontent.com', 'dYlNUl-U-zy8gvkpGjnnTDS3', 'openid email profile', true, true );

