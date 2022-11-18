package com.smartsparrow.sso.data.oidc;

import javax.inject.Inject;

import com.datastax.driver.core.Session;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.sso.service.AccessToken;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AccessTokenByWebSessionTokenGateway {

    private final AccessTokenByWebSessionTokenMaterializer accessTokenByWebSessionTokenMaterializer;
    private final AccessTokenByWebSessionTokenMutator accessTokenByWebSessionTokenMutator;

    private final Session session;

    @Inject
    public AccessTokenByWebSessionTokenGateway(AccessTokenByWebSessionTokenMaterializer accessTokenByWebSessionTokenMaterializer,
            AccessTokenByWebSessionTokenMutator accessTokenByWebSessionTokenMutator,
            Session session) {
        this.accessTokenByWebSessionTokenMaterializer = accessTokenByWebSessionTokenMaterializer;
        this.accessTokenByWebSessionTokenMutator = accessTokenByWebSessionTokenMutator;
        this.session = session;
    }

    /**
     * Persist an Open ID Connect access_token received during a Token Request.
     *
     * @param accessToken the access_token values to persist.
     * @return nada.
     */
    public Flux<Void> persist(final AccessToken accessToken) {
        return Mutators.execute(session, Flux.just(accessTokenByWebSessionTokenMutator.upsert(accessToken)));
    }

    /**
     * Find the access_token received during the Token Request.
     *
     * @param webSessionToken the web session token value, or bearer token.
     * @return the access_token received during the Token Request
     */
    public Mono<AccessToken> find(final String webSessionToken) {
        return ResultSets.query(session, accessTokenByWebSessionTokenMaterializer.find(webSessionToken)) //
                .flatMapIterable(row -> row) //
                .map(accessTokenByWebSessionTokenMaterializer::fromRow) //
                .singleOrEmpty();
    }

}
