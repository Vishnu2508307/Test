package com.smartsparrow.graphql.schema;

import static com.smartsparrow.iam.util.Permissions.affirmPermission;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.competency.data.Document;
import com.smartsparrow.competency.service.DocumentService;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowDocumentReviewerOrHigher;
import com.smartsparrow.graphql.service.GraphQLPageFactory;
import com.smartsparrow.graphql.type.Learn;
import com.smartsparrow.learner.payload.LearnerDocumentPayload;
import com.smartsparrow.learner.service.LearnerCompetencyDocumentService;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import com.smartsparrow.workspace.data.Workspace;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.execution.relay.Page;
import reactor.core.publisher.Mono;

@Singleton
public class DocumentSchema {

    private final DocumentService documentService;
    private final AllowDocumentReviewerOrHigher allowDocumentReviewerOrHigher;
    private final LearnerCompetencyDocumentService learnerCompetencyDocumentService;

    @Inject
    public DocumentSchema(DocumentService documentService,
                          AllowDocumentReviewerOrHigher allowDocumentReviewerOrHigher,
                          LearnerCompetencyDocumentService learnerCompetencyDocumentService) {
        this.documentService = documentService;
        this.allowDocumentReviewerOrHigher = allowDocumentReviewerOrHigher;
        this.learnerCompetencyDocumentService = learnerCompetencyDocumentService;
    }

    /**
     * Fetch a list of documents in workspace or a particular document
     *
     * @param documentId - The exact document id to fetch
     * @return {@link Document} - The Document object
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Document.competencyDocuments")
    @GraphQLQuery(name = "competencyDocuments", description = "List of competencies given a documentId")
    public CompletableFuture<Page<Document>> getDocuments(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                          @GraphQLContext Workspace workspace,
                                                          @Nullable
                                       @GraphQLArgument(name = "documentId",
                                               description = "Fetch a single document within a workspace")
                                               UUID documentId,
                                                          @GraphQLArgument(name = "before",
                                               description = "fetching only nodes before this node (exclusive)")
                                               String before,
                                                          @GraphQLArgument(name = "last",
                                               description = "fetching only the last certain number of nodes")
                                               Integer last) {
        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        UUID workspaceId = workspace.getId();

        if (documentId != null) {
            affirmPermission(allowDocumentReviewerOrHigher.test(context.getAuthenticationContext(), documentId),
                    "User does not have permissions to view document");

            Mono<List<Document>> documents = documentService
                    .fetchDocument(documentId)
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    .doOnEach(ReactiveTransaction.expireOnComplete())
                    .subscriberContext(ReactiveMonitoring.createContext())
                    .flux()
                    .collectList();

            return GraphQLPageFactory
                    .createPage(documents, before, last)
                    .toFuture();
        } else {
            UUID accountId = context.getAuthenticationContext().getAccount().getId();

            Mono<List<Document>> documents = documentService.fetchDocuments(accountId)
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    .doOnEach(ReactiveTransaction.expireOnComplete())
                    .subscriberContext(ReactiveMonitoring.createContext())
                    .filter(document -> workspaceId.equals(document.getWorkspaceId()))
                    .collectList();

            return GraphQLPageFactory
                    .createPage(documents, before, last)
                    .toFuture();
        }
    }

    /**
     * Allows to fetch a published document by its id
     *
     * @param learn the context the graphql query should be executed from
     * @param documentId the id of the document to fetch
     * @return a learner document payload object
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Document.learnerDocument")
    @GraphQLQuery(name = "learnerDocument", description = "Fetch a published document by documentId")
    public CompletableFuture<LearnerDocumentPayload> getPublishedDocument(@GraphQLContext Learn learn,
                                                                          @Nonnull
                                                       @GraphQLArgument(name = "documentId",
                                                               description = "Fetch a single published document by id")
                                                               UUID documentId) {
        return learnerCompetencyDocumentService.findDocument(documentId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .map(LearnerDocumentPayload::from)
                .toFuture();
    }

    /**
     * Fetch a list of documents
     *
     * @return {@link Document} - The Document object
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Document.competencyDocuments")
    @GraphQLQuery(name = "documents", description = "List of competencies")
    public CompletableFuture<Page<Document>> getAllDocuments(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                             @GraphQLArgument(name = "before",
                                               description = "fetching only nodes before this node (exclusive)")
                                               String before,
                                                             @GraphQLArgument(name = "last",
                                               description = "fetching only the last certain number of nodes")
                                               Integer last) {
        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        UUID accountId = context.getAuthenticationContext().getAccount().getId();

        Mono<List<Document>> documents = documentService.fetchDocuments(accountId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .collectList();

        return GraphQLPageFactory.createPage(documents, before, last)
                .toFuture();
    }
}
