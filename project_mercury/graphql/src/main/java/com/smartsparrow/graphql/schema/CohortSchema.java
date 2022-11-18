package com.smartsparrow.graphql.schema;

import static com.smartsparrow.iam.util.Permissions.affirmPermission;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.cohort.data.CohortEnrollment;
import com.smartsparrow.cohort.data.CohortSettings;
import com.smartsparrow.cohort.data.CohortSummary;
import com.smartsparrow.cohort.data.EnrollmentType;
import com.smartsparrow.cohort.service.CohortEnrollmentService;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.cohort.service.PassportService;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowCohortInstructor;
import com.smartsparrow.graphql.auth.AllowEnrolledStudent;
import com.smartsparrow.graphql.type.Learn;
import com.smartsparrow.iam.exception.PermissionFault;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

@Singleton
public class CohortSchema {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CohortSchema.class);

    private final CohortService cohortService;
    private final AllowCohortInstructor allowCohortInstructor;
    private final AllowEnrolledStudent allowEnrolledStudent;
    private final CohortEnrollmentService cohortEnrollmentService;
    private final PassportService passportService;

    private static final String ERROR_MESSAGE = "User does not have permissions to view cohort";

    @Inject
    public CohortSchema(final CohortService cohortService,
                        final AllowEnrolledStudent allowEnrolledStudent,
                        final AllowCohortInstructor allowCohortInstructor,
                        final CohortEnrollmentService cohortEnrollmentService,
                        final PassportService passportService) {
        this.cohortService = cohortService;
        this.allowCohortInstructor = allowCohortInstructor;
        this.allowEnrolledStudent = allowEnrolledStudent;
        this.cohortEnrollmentService = cohortEnrollmentService;
        this.passportService = passportService;
    }

    @GraphQLQuery(name = "cohortById", description = "Cohort summary data")
    public CompletableFuture<CohortSummary> getCohortById(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                          @GraphQLArgument(name = "cohortId", description = "Fetch a cohort with specific id") @GraphQLNonNull UUID cohortId) {
        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        affirmPermission(allowCohortInstructor.test(context.getAuthenticationContext(), cohortId), ERROR_MESSAGE);
        return cohortService.fetchCohortSummary(cohortId).toFuture();
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = "Cohort.cohort")
    @GraphQLQuery(name = "cohort", description = "Cohort summary data")
    public CompletableFuture<CohortSummary> getCohort(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                   @GraphQLContext Learn learn,
                                   @GraphQLArgument(name = "cohortId", description = "Fetch a cohort with specific id") UUID cohortId) {
        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        AuthenticationContext authenticationContext = context.getAuthenticationContext();

        affirmArgument(cohortId != null, "missing cohortId");

        Mono<CohortSummary> cohortSummaryMono = Mono.just(cohortId)
                .map(uuid -> {
                    if (allowEnrolledStudent.test(context.getAuthenticationContext(), cohortId) ||
                            allowCohortInstructor.test(context.getAuthenticationContext(), cohortId)) {
                        return uuid;
                    } else {
                        throw new PermissionFault(ERROR_MESSAGE);
                    }
                })
                .flatMap(cohortService::fetchCohortSummary);

        return cohortSummaryMono.doOnError(throwable -> {
                    log.error("Permission error", throwable);
                    throw Exceptions.propagate(throwable);
                })
                .onErrorResume(throwable -> handlePermissionFaults(cohortId, authenticationContext))
                .toFuture();
    }

    @GraphQLQuery(name = "productId", description = "the product id associated to the cohort")
    public CompletableFuture<String> getProductId(@GraphQLContext CohortSummary cohortSummary) {
        return cohortService.fetchCohortProductId(cohortSummary.getId()).toFuture();
    }

    private Mono<CohortSummary> handlePermissionFaults(UUID cohortId, AuthenticationContext authenticationContext) {

        Mono<CohortSettings> cohortSettings = cohortService.fetchCohortSettings(cohortId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .single()
                .onErrorResume(throwable -> {
                    log.error("cohort settings not found");
                    return Mono.error(new PermissionFault(ERROR_MESSAGE));
                });

        final Mono<CohortSummary> cohortSummary = cohortService.fetchCohortSummary(cohortId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .onErrorResume(throwable -> Mono.error(new PermissionFault(ERROR_MESSAGE)));

        return cohortSettings
                .zipWith(cohortSummary)
                .flatMap(tuple2 -> {
                    CohortSettings settings = tuple2.getT1();
                    CohortSummary summary = tuple2.getT2();
                    switch (summary.getType()) {

                        case OPEN:
                            // allow anyone in
                            return cohortEnrollmentService.enrollAccount(authenticationContext.getAccount().getId(), //
                                                                         summary.getId(), //
                                                                         EnrollmentType.OPEN,
                                                                         authenticationContext.getPearsonUid())
                                    .doOnEach(ReactiveTransaction.linkOnNext())
                                    .doOnEach(ReactiveTransaction.expireOnComplete())
                                    .subscriberContext(ReactiveMonitoring.createContext())
                                    .thenReturn(summary);

                        case PASSPORT:
                            // perform passport entitlement check
                            Mono<Boolean> isPassportEntitled = passportService.checkEntitlement(authenticationContext.getPearsonUid(),
                                                                                                settings.getProductId())
                                    .onErrorResume(throwable -> Mono.just(false))
                                    .doOnEach(ReactiveTransaction.linkOnNext())
                                    .doOnEach(ReactiveTransaction.expireOnComplete())
                                    .subscriberContext(ReactiveMonitoring.createContext())
                                    .single();

                            return isPassportEntitled
                                    .flatMap(isEntitled -> {
                                        if (!isEntitled) {
                                            return Mono.error(new PermissionFault(ERROR_MESSAGE));
                                        }
                                        return cohortEnrollmentService.enrollAccount(
                                                        authenticationContext.getAccount().getId(),
                                                        summary.getId(),
                                                        EnrollmentType.PASSPORT,
                                                        authenticationContext.getPearsonUid())
                                                .doOnEach(ReactiveTransaction.linkOnNext())
                                                .doOnEach(ReactiveTransaction.expireOnComplete())
                                                .subscriberContext(ReactiveMonitoring.createContext())
                                                .thenReturn(summary);
                                    })
                                    .doOnError(throwable -> { throw new PermissionFault(ERROR_MESSAGE); });

                        case LTI:
                            // try finding the launch record
                            log.jsonWarn(
                                    "account tried to access a configured LTI cohort without performing an LTI launch",
                                    new HashMap<String, Object>() {
                                        {put("cohortId", cohortId);}
                                        {put("accountId", authenticationContext.getAccount().getId());}
                                    });
                            return Mono.error(new PermissionFault(ERROR_MESSAGE));

                        default:
                            // any other case should not be authorized
                            return Mono.error(new PermissionFault(ERROR_MESSAGE));
                    }
                });
    }
}
