package com.smartsparrow.graphql.schema;

import static com.smartsparrow.iam.util.Permissions.affirmPermission;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.cohort.data.CohortEnrollment;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowCohortInstructor;
import com.smartsparrow.graphql.service.GraphQLPageFactory;
import com.smartsparrow.graphql.type.Learn;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.learner.data.CompetencyMet;
import com.smartsparrow.learner.data.CompetencyMetByStudent;
import com.smartsparrow.learner.payload.LearnerDocumentItemPayload;
import com.smartsparrow.learner.service.CompetencyMetService;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.execution.relay.Page;
import reactor.core.publisher.Mono;

@Singleton
public class LearnerCompetencyMetSchema {

    private final AllowCohortInstructor allowCohortInstructor;
    //
    private final CompetencyMetService competencyMetService;

    @Inject
    public LearnerCompetencyMetSchema(AllowCohortInstructor allowCohortInstructor,
            CompetencyMetService competencyMetService) {
        this.allowCohortInstructor = allowCohortInstructor;
        this.competencyMetService = competencyMetService;
    }

    /**
     * Find the latest competency met entry over a specific learner document item
     *
     * @param payload the learner document item payload context
     * @return a competency met by student
     */
    @GraphQLQuery(name = "competencyDocumentItemMet", description = "fetch the competency met for a student over a learner document item")
    public CompletableFuture<CompetencyMetByStudent> getCompetencyMetByStudent(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                               @GraphQLContext LearnerDocumentItemPayload payload) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        Account account = context.getAuthenticationContext().getAccount();
        return competencyMetService.findLatest(account.getId(), payload.getDocumentId(), payload.getId())
                .toFuture();
    }

    /**
     * Find all the competency met entries for a student over a learner document
     *
     * @param learn the context from which this graphql query is available from
     * @param documentId the document to find the student competency met entries over
     * @param before fetch the nodes before this node
     * @param last represents then number of the last nodes to fetch
     * @return a page of competency met by student
     * @throws IllegalArgumentFault when documentId is <code>null</code>
     */
    @GraphQLQuery(name = "competencyDocumentMet", description = "fetch the competency met for a student over a learner document item")
    public CompletableFuture<Page<CompetencyMetByStudent>> getAllCompetencyMetByDocument(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                                         @GraphQLContext Learn learn,
                                                                                         @GraphQLArgument(name = "documentId",
                                                                              description = "the document id to find the student competency met over")
                                                                              UUID documentId,
                                                                                         @GraphQLArgument(name = "before",
                                                                              description = "fetching only nodes before this node(exclusive)")
                                                                              String before,
                                                                                         @GraphQLArgument(name = "last",
                                                                              description = "fetching only the last certain number of nodes")
                                                                              Integer last) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        affirmArgument(documentId != null, "documentId is required");

        Account account = context.getAuthenticationContext().getAccount();
        Mono<List<CompetencyMetByStudent>> all = competencyMetService.findLatest(account.getId(), documentId)
                .collectList();

        return GraphQLPageFactory.createPage(all, before, last).toFuture();
    }

    /**
     * Fetch the competency met object for a competency met by student
     *
     * @param competencyMetByStudent the student competency met entry to find the competency met full object for
     * @return a competency met object
     */
    @GraphQLQuery(name = "competencyMet", description = "fetch the competency met full object")
    public CompletableFuture<CompetencyMet> getCompetencyMet(@GraphQLContext CompetencyMetByStudent competencyMetByStudent) {
        return competencyMetService.findCompetencyMet(competencyMetByStudent.getMetId())
                .toFuture();
    }

    /**
     * Fetch all the competency met entries for a student
     *
     * @param learn the context from which the graphql query is available
     * @param before fetch the nodes before this node
     * @param last represents then number of the last nodes to fetch
     * @return a page of competency met by student
     */
    @GraphQLQuery(name = "competencyMetAll", description = "fetch all the competency met entries for a student")
    public CompletableFuture<Page<CompetencyMetByStudent>> getAllCompetencyMet(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                               @GraphQLContext Learn learn,
                                                                               @GraphQLArgument(name = "before",
                                                                    description = "fetching only nodes before this node(exclusive)")
                                                                    String before,
                                                                               @GraphQLArgument(name = "last",
                                                                    description = "fetching only the last certain number of nodes")
                                                                    Integer last) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        Account account = context.getAuthenticationContext().getAccount();
        Mono<List<CompetencyMetByStudent>> all = competencyMetService.findLatest(account.getId())
                .collectList();

        return GraphQLPageFactory.createPage(all, before, last).toFuture();
    }

    @GraphQLQuery(name = "learnerPerformanceByCompetency", description = "fetch all the competency across the enrollments")
    public CompletableFuture<CompetencyMetByStudent> getCompetencyMetByEnrollment(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                                  @GraphQLContext final CohortEnrollment cohortEnrollment,
                                                                                  @GraphQLArgument(name = "documentId", description = "the document id") @GraphQLNonNull UUID documentId,
                                                                                  @GraphQLArgument(name = "documentItemId", description = "the document item id") @GraphQLNonNull UUID documentItemId) {
        // only allow instructors to request this data as it is part of the Cohort Enrollment context.
        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        affirmPermission(allowCohortInstructor.test(context.getAuthenticationContext(), cohortEnrollment.getCohortId()), "Unauthorized");
        //
        return competencyMetService.findLatest(cohortEnrollment.getAccountId(), documentId, documentItemId).toFuture();
    }
}
