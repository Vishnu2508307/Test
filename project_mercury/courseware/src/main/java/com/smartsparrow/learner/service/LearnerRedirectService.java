package com.smartsparrow.learner.service;

import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.exception.ConflictFault;
import com.smartsparrow.learner.data.LearnerRedirectGateway;
import com.smartsparrow.learner.redirect.LearnerRedirect;
import com.smartsparrow.learner.redirect.LearnerRedirectType;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Mono;

/**
 * Service to enable/map redirects
 * <p>
 * This maps:
 * - [URI]/type/key to Destination Path
 */
@Singleton
public class LearnerRedirectService {

    private final LearnerRedirectGateway learnerRedirectGateway;

    @Inject
    public LearnerRedirectService(final LearnerRedirectGateway learnerRedirectGateway) {
        this.learnerRedirectGateway = learnerRedirectGateway;
    }

    /**
     * Create a redirect
     *
     * @param type the type
     * @param key the part used by the end-user
     * @param destinationPath the path of the resultant redirect, including leading slash
     * @return the created redirect
     */
    public Mono<LearnerRedirect> create(final LearnerRedirectType type,
                                        final String key,
                                        final String destinationPath) {
        //
        affirmNotNull(type, "type is required");
        affirmArgumentNotNullOrEmpty(key, "key is required");
        affirmArgumentNotNullOrEmpty(destinationPath, "destination path is required");

        // check if the entry already exists.
        return fetch(type, key)
                .hasElement()
                .flatMap(exists -> {
                    // return an error if the entry already exists
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new ConflictFault("entry already exists"));
                    }

                    // proceed creating the entry otherwise
                    LearnerRedirect r = new LearnerRedirect();
                    r.setId(UUIDs.timeBased());
                    r.setVersion(UUIDs.timeBased());
                    r.setType(type);
                    r.setKey(key);
                    r.setDestinationPath(destinationPath);

                    // upsert and return.
                    return learnerRedirectGateway.persist(r) //
                            .then(Mono.just(r));
                });

    }

    /**
     * Update a redirect
     *
     * @param redirectId the id of the original redirect
     * @param type the redirect type
     * @param key the part used by the end-user
     * @param destinationPath the path of the resultant redirect, including leading slash
     * @return the updated redirect
     */
    public Mono<LearnerRedirect> update(final UUID redirectId,
                                        final LearnerRedirectType type,
                                        final String key,
                                        final String destinationPath) {
        //
        affirmNotNull(redirectId, "id is required");
        affirmNotNull(type, "type is required");
        affirmArgumentNotNullOrEmpty(key, "key is required");
        affirmArgumentNotNullOrEmpty(destinationPath, "destinationPath is required");

        //
        LearnerRedirect r = new LearnerRedirect();
        r.setId(redirectId);
        r.setVersion(UUIDs.timeBased());
        r.setType(type);
        r.setKey(key);
        r.setDestinationPath(destinationPath);

        // upsert and return.
        return learnerRedirectGateway.persist(r) //
                .then(Mono.just(r));
    }

    /**
     * Find a redirect by using the type and key
     *
     * @param type the type
     * @param key the part used by the end-user
     * @return the matching redirect
     */
    public Mono<LearnerRedirect> fetch(final LearnerRedirectType type, final String key) {
        affirmNotNull(type, "type is required");
        affirmArgumentNotNullOrEmpty(key, "key is required");
        //
        return learnerRedirectGateway.fetch(type, key);
    }

    /**
     * Delete a redirect by id. This method will only delete the redirect from the by_key table keeping the history and
     * by_id tables untouched
     *
     * @param redirectId the redirect id to delete the by_key entry for
     * @return a mono with the deleted redirect
     */
    public Mono<LearnerRedirect> delete(final UUID redirectId) {
        affirmArgument(redirectId != null, "redirectId is required");

        // find the redirect
        // FIXME: this is an anti-pattern in cassandra. Once we have proper RTM messages to set/update/delete redirects
        // then this logic can be deprecated
        return learnerRedirectGateway.fetchById(redirectId)
                // delete the redirect
                .flatMap(learnerRedirect -> learnerRedirectGateway.delete(learnerRedirect)
                        .singleOrEmpty()
                        .thenReturn(learnerRedirect));
    }

}
