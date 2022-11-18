package com.smartsparrow.iam.service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.iam.collaborator.AccountCollaborator;
import com.smartsparrow.iam.collaborator.Collaborator;
import com.smartsparrow.iam.collaborator.CollaboratorResult;
import com.smartsparrow.iam.collaborator.Collaborators;
import com.smartsparrow.iam.collaborator.TeamCollaborator;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Singleton
public class CollaboratorService {

    private final AccountService accountService;
    private final TeamService teamService;

    @Inject
    public CollaboratorService(AccountService accountService,
                               TeamService teamService) {
        this.accountService = accountService;
        this.teamService = teamService;
    }

    /**
     * Computes the collaborators from a flux of team and account collaborators
     *
     * @param teams    the team collaborators flux - must extend {@link TeamCollaborator}
     * @param accounts the account collaborators flux - must extend {@link AccountCollaborator}
     * @return a mono of collaborator resutlt
     */
    public Mono<CollaboratorResult> getCollaborators(@Nonnull final Flux<? extends TeamCollaborator> teams,
                                                     @Nonnull final Flux<? extends AccountCollaborator> accounts) {

        Flux<? extends Collaborator> collaboratorsFlux = Flux.concat(teams, accounts);

        final Mono<Long> total = collaboratorsFlux.count();

        return getCollaboratorResultMono(collaboratorsFlux, total);

    }
        /**
         * Computes the collaborators from a flux of team and account collaborators
         *
         * @param teams    the team collaborators flux - must extend {@link TeamCollaborator}
         * @param accounts the account collaborators flux - must extend {@link AccountCollaborator}
         * @param limit    the number of collaborators to take out of the total
         * @return a mono of collaborator resutlt
         */
    public Mono<CollaboratorResult> getCollaborators(@Nonnull final Flux<? extends TeamCollaborator> teams,
                                                     @Nonnull final Flux<? extends AccountCollaborator> accounts,
                                                     final Integer limit) {

        Flux<? extends Collaborator> collaboratorsFlux = Flux.concat(teams, accounts);

        final Mono<Long> total = collaboratorsFlux.count();

        collaboratorsFlux = collaboratorsFlux.take(limit);

        return getCollaboratorResultMono(collaboratorsFlux, total);
    }

    private Mono<CollaboratorResult> getCollaboratorResultMono(final Flux<? extends Collaborator> collaboratorsFlux,
                                                               final Mono<Long> total) {
        final Mono<Collaborators> collaborators = collaboratorsFlux
                .flatMap(collaborator -> {
                    if (collaborator instanceof TeamCollaborator) {
                        return teamService.getTeamCollaboratorPayload(((TeamCollaborator) collaborator).getTeamId(),
                                collaborator.getPermissionLevel());
                    } else {
                        return accountService.getCollaboratorPayload(((AccountCollaborator) collaborator).getAccountId(),
                                collaborator.getPermissionLevel());
                    }
                })
                .collect(Collaborators::new, Collaborators::add);

        return Mono.zip(collaborators, total)
                .map(this::buildCollaboratorResult);
    }

    /**
     * Build a collaborator result
     *
     * @param tuple the tuple from which to build the collaborator result
     * @return a collaborator result
     */
    private CollaboratorResult buildCollaboratorResult(Tuple2<Collaborators, Long> tuple) {
        return new CollaboratorResult()
                .setTotal(tuple.getT2())
                .setCollaborators(tuple.getT1());
    }

}
