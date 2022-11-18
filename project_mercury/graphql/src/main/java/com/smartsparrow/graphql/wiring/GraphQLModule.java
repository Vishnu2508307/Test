package com.smartsparrow.graphql.wiring;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.smartsparrow.graphql.schema.AccountIdentitySchema;
import com.smartsparrow.graphql.schema.AccountSchema;
import com.smartsparrow.graphql.schema.ActivitySchema;
import com.smartsparrow.graphql.schema.AnnotationSchema;
import com.smartsparrow.graphql.schema.AssetSchema;
import com.smartsparrow.graphql.schema.AttemptSchema;
import com.smartsparrow.graphql.schema.AvatarSchema;
import com.smartsparrow.graphql.schema.ClockSchema;
import com.smartsparrow.graphql.schema.CohortSchema;
import com.smartsparrow.graphql.schema.ComponentSchema;
import com.smartsparrow.graphql.schema.CoursewareElementSchema;
import com.smartsparrow.graphql.schema.CoursewareElementStructureSchema;
import com.smartsparrow.graphql.schema.DeploymentSchema;
import com.smartsparrow.graphql.schema.DocumentAssociationSchema;
import com.smartsparrow.graphql.schema.DocumentItemMutationSchema;
import com.smartsparrow.graphql.schema.DocumentItemSchema;
import com.smartsparrow.graphql.schema.DocumentItemTagMutationSchema;
import com.smartsparrow.graphql.schema.DocumentMutationSchema;
import com.smartsparrow.graphql.schema.DocumentSchema;
import com.smartsparrow.graphql.schema.ElementSchema;
import com.smartsparrow.graphql.schema.EnrollmentSchema;
import com.smartsparrow.graphql.schema.GeneratorSchema;
import com.smartsparrow.graphql.schema.IAMClaimSchema;
import com.smartsparrow.graphql.schema.InteractiveSchema;
import com.smartsparrow.graphql.schema.ItemAssociationMutationSchema;
import com.smartsparrow.graphql.schema.ItemAssociationSchema;
import com.smartsparrow.graphql.schema.LTISchema;
import com.smartsparrow.graphql.schema.LTIToolProviderSchema;
import com.smartsparrow.graphql.schema.LearnSchema;
import com.smartsparrow.graphql.schema.LearnerCompetencyMetSchema;
import com.smartsparrow.graphql.schema.LearnerElementSchema;
import com.smartsparrow.graphql.schema.MathAssetSchema;
import com.smartsparrow.graphql.schema.PathwaySchema;
import com.smartsparrow.graphql.schema.PingSchema;
import com.smartsparrow.graphql.schema.PluginSchema;
import com.smartsparrow.graphql.schema.ProductSchema;
import com.smartsparrow.graphql.schema.ProgressSchema;
import com.smartsparrow.graphql.schema.ScopeRegistrySchema;
import com.smartsparrow.graphql.schema.ScopeSchema;
import com.smartsparrow.graphql.schema.SubscriptionSchema;
import com.smartsparrow.graphql.schema.WalkableSchema;
import com.smartsparrow.graphql.schema.WorkspaceSchema;

import graphql.schema.GraphQLSchema;
import io.leangen.graphql.GraphQLSchemaGenerator;
import io.leangen.graphql.generator.mapping.strategy.DefaultImplementationDiscoveryStrategy;
import io.leangen.graphql.metadata.strategy.value.jackson.JacksonValueMapperFactory;

public class GraphQLModule extends AbstractModule {

    private final static Logger log = LoggerFactory.getLogger(GraphQLModule.class);

    /**
     * Provide a GraphQL Schema. This is the expensive operation and should only be done once.
     *
     * @return the GraphQL Schema
     */
    @Singleton
    @Provides
    public GraphQLSchema provideSchema(final AccountIdentitySchema accountIdentitySchema,
                                       final AccountSchema accountSchema,
                                       final ActivitySchema activitySchema,
                                       final AssetSchema assetSchema,
                                       final AttemptSchema attemptSchema,
                                       final AvatarSchema avatarSchema,
                                       final ClockSchema clockSchema,
                                       final CohortSchema cohortSchema,
                                       final ComponentSchema componentSchema,
                                       final DeploymentSchema deploymentSchema,
                                       final DocumentSchema documentSchema,
                                       final DocumentItemMutationSchema documentItemMutationSchema,
                                       final DocumentAssociationSchema documentAssociationSchema,
                                       final DocumentItemSchema documentItemSchema,
                                       final DocumentMutationSchema documentMutationSchema,
                                       final GeneratorSchema generatorSchema,
                                       final InteractiveSchema interactiveSchema,
                                       final ItemAssociationMutationSchema itemAssociationMutationSchema,
                                       final LearnSchema learnSchema,
                                       final PathwaySchema pathwaySchema,
                                       final PingSchema pingSchema,
                                       final PluginSchema pluginSchema,
                                       final ProductSchema productSchema,
                                       final ProgressSchema progressSchema,
                                       final ScopeSchema scopeSchema,
                                       final SubscriptionSchema subscriptionSchema,
                                       final WalkableSchema walkableSchema,
                                       final WorkspaceSchema workspaceSchema,
                                       final DocumentItemTagMutationSchema documentItemTagMutationSchema,
                                       final LearnerCompetencyMetSchema learnerCompetencyMetSchema,
                                       final ItemAssociationSchema itemAssociationSchema,
                                       final EnrollmentSchema enrollmentSchema,
                                       final IAMClaimSchema iamClaimSchema,
                                       final AnnotationSchema annotationSchema,
                                       final CoursewareElementSchema coursewareElementSchema,
                                       final LTISchema ltiSchema,
                                       final LTIToolProviderSchema ltiToolProviderSchema,
                                       final CoursewareElementStructureSchema coursewareElementStructureSchema,
                                       final ScopeRegistrySchema scopeRegistrySchema,
                                       final MathAssetSchema mathAssetSchema,
                                       final ElementSchema elementSchema,
                                       final LearnerElementSchema learnerElementSchema) {
        // the method arguments here are a way to provide injected dependencies to a module.
        //
        GraphQLSchema schema = new GraphQLSchemaGenerator() //
                // Mark fields as deprecated as same in java code.
                .withJavaDeprecationRespected(true)
                .withImplementationDiscoveryStrategy(new DefaultImplementationDiscoveryStrategy())
                .withValueMapperFactory(new JacksonValueMapperFactory())
                // Add types.
                .withOperationsFromSingleton(accountIdentitySchema) //
                .withOperationsFromSingleton(accountSchema) //
                .withOperationsFromSingleton(activitySchema) //
                .withOperationsFromSingleton(assetSchema) //
                .withOperationsFromSingleton(attemptSchema) //
                .withOperationsFromSingleton(avatarSchema) //
                .withOperationsFromSingleton(clockSchema) //
                .withOperationsFromSingleton(cohortSchema) //
                .withOperationsFromSingleton(componentSchema) //
                .withOperationsFromSingleton(deploymentSchema) //
                .withOperationsFromSingleton(documentSchema) //
                .withOperationsFromSingleton(documentItemMutationSchema) //
                .withOperationsFromSingleton(documentAssociationSchema) //
                .withOperationsFromSingleton(documentItemSchema) //
                .withOperationsFromSingleton(documentMutationSchema) //
                .withOperationsFromSingleton(generatorSchema) //
                .withOperationsFromSingleton(interactiveSchema) //
                .withOperationsFromSingleton(itemAssociationMutationSchema) //
                .withOperationsFromSingleton(learnSchema) //
                .withOperationsFromSingleton(pathwaySchema) //
                .withOperationsFromSingleton(pingSchema) //
                .withOperationsFromSingleton(pluginSchema) //
                .withOperationsFromSingleton(productSchema) //
                .withOperationsFromSingleton(progressSchema) //
                .withOperationsFromSingleton(scopeSchema) //
                .withOperationsFromSingleton(subscriptionSchema) //
                .withOperationsFromSingleton(walkableSchema) //
                .withOperationsFromSingleton(workspaceSchema) //
                .withOperationsFromSingleton(documentItemTagMutationSchema) //
                .withOperationsFromSingleton(learnerCompetencyMetSchema) //
                .withOperationsFromSingleton(itemAssociationSchema) //
                .withOperationsFromSingleton(enrollmentSchema) //
                .withOperationsFromSingleton(iamClaimSchema) //
                .withOperationsFromSingleton(annotationSchema) //
                .withOperationsFromSingleton(coursewareElementSchema) //
                .withOperationsFromSingleton(ltiSchema) //
                .withOperationsFromSingleton(ltiToolProviderSchema) //
                .withOperationsFromSingleton(coursewareElementStructureSchema) //
                .withOperationsFromSingleton(scopeRegistrySchema) //
                .withOperationsFromSingleton(mathAssetSchema) //
                .withOperationsFromSingleton(elementSchema) //
                .withOperationsFromSingleton(learnerElementSchema) //
                // Define base packages - otherwise fields from parents which are in different package is not visible
                // e.g. fields from com.smartsparrow.courseware.data.Activity are not visible for type com.smartsparrow.learner.data.LearnerActivity
                .withBasePackages("com.smartsparrow") //
                // Build it.
                .generate();

        return GraphQLSchema
                .newSchema(schema)
                .build();
    }

    // required method as this module is installed in multiple places; based on which functionality requires it.
    @Override
    public boolean equals(Object o) {
        //
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        // if we got this far, we can assume that we are comparing two "new GraphQLModule()" objects.
        // ideally, we would ensure that the provideSchema returns GraphQLSchemas that are the same, but
        // this is difficult because provideSchema has injected values.
        return true;
    }

    @Override
    public int hashCode() {
        // similar to the equals() method, ideally it would hash the GraphQLSchema.
        // return something that will be the same across new() operations.
        return getClass().hashCode();
    }
}
