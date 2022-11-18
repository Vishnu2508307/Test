package com.smartsparrow.courseware.wiring;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.smartsparrow.asset.service.AssetService;
import com.smartsparrow.cache.config.CacheConfig;
import com.smartsparrow.cache.config.RouteConsumersConfig;
import com.smartsparrow.cohort.wiring.PassportConfig;
import com.smartsparrow.config.service.ConfigurationService;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.courseware.pathway.Pathway;
import com.smartsparrow.courseware.pathway.PathwayType;
import com.smartsparrow.data.AbstractModuleDecorator;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.ActionContext;
import com.smartsparrow.eval.action.ActionResult;
import com.smartsparrow.eval.data.ActionConsumer;
import com.smartsparrow.eval.data.EvaluationRequest;
import com.smartsparrow.eval.data.EvaluationResponse;
import com.smartsparrow.eval.mutation.MutationOperation;
import com.smartsparrow.eval.mutation.MutationOperator;
import com.smartsparrow.eval.mutation.operations.DifferenceMutationOperation;
import com.smartsparrow.eval.mutation.operations.ListMutationOperation;
import com.smartsparrow.eval.mutation.operations.ListMutationOperationAdd;
import com.smartsparrow.eval.mutation.operations.ListMutationOperationRemove;
import com.smartsparrow.eval.mutation.operations.ListMutationOperationSet;
import com.smartsparrow.eval.mutation.operations.MutationOperationSet;
import com.smartsparrow.eval.mutation.operations.SumMutationOperation;
import com.smartsparrow.eval.mutation.operations.UnsupportedMutationOperation;
import com.smartsparrow.eval.service.EvaluationService;
import com.smartsparrow.eval.service.PathwayProgressUpdateService;
import com.smartsparrow.eval.service.ProgressUpdateService;
import com.smartsparrow.eval.wiring.EvaluationFeatureConfig;
import com.smartsparrow.eval.wiring.EvaluationFeatureConfigurationValues;
import com.smartsparrow.eval.wiring.EvaluationFeatureMode;
import com.smartsparrow.eval.wiring.EvaluationOperationsModule;
import com.smartsparrow.learner.data.DeploymentChangeIdGateway;
import com.smartsparrow.learner.service.LatestDeploymentChangeIdCache;
import com.smartsparrow.learner.service.LearnerAssetService;
import com.smartsparrow.learner.wiring.GradePassbackConfig;
import com.smartsparrow.util.DataType;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.workspace.wiring.PublishMetadataConfig;

import data.EntityType;
import data.SynchronizableService;
import reactor.util.annotation.NonNull;

public abstract class AbstractCoursewareModule extends AbstractModuleDecorator {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AbstractCoursewareModule.class);

    protected MapBinder<PathwayType, Pathway> pathwayTypes;
    protected MapBinder<PathwayType, LearnerPathway> learnerPathwayTypes;
    protected MapBinder<EvaluationRequest.Type, EvaluationService<? extends EvaluationRequest, ? extends EvaluationResponse<?>>> evaluationTypes;
    protected MapBinder<Action.Type, ActionConsumer<? extends Action<? extends ActionContext<?>>, ? extends ActionResult<?>>> actionConsumers;
    protected MapBinder<CoursewareElementType, ProgressUpdateService> progressUpdateImplementations;
    protected MapBinder<PathwayType, PathwayProgressUpdateService<? extends LearnerPathway>> pathwayProgressUpdateImplementations;
    private MapBinder<DataType, Map<MutationOperator, MutationOperation>> mutationOperations;
    private MapBinder<MutationOperator, ListMutationOperation> listMutationOperations;
    protected MapBinder<EntityType, SynchronizableService> synchronizableServiceImplementations;

    @Override
    protected void configure() {
        // setup the binders for the pathway types
        pathwayTypes = MapBinder.newMapBinder(binder(),  //
                new TypeLiteral<PathwayType>() {
                }, //
                new TypeLiteral<Pathway>() {
                });
        learnerPathwayTypes = MapBinder.newMapBinder(binder(),  //
                new TypeLiteral<PathwayType>() {
                }, //
                new TypeLiteral<LearnerPathway>() {
                });

        // setup binding of evaluation implementations
        evaluationTypes = MapBinder.newMapBinder(binder(), new TypeLiteral<EvaluationRequest.Type>() {
                }, //
                new TypeLiteral<EvaluationService<? extends EvaluationRequest, ? extends EvaluationResponse<?>>>() { //
                }); //

        // setup binding for action consumers
        actionConsumers = MapBinder.newMapBinder(binder(), new TypeLiteral<Action.Type>(){
                }, //
                new TypeLiteral<ActionConsumer<? extends Action<? extends ActionContext<?>>, ? extends ActionResult<?>>>() {
                }); //

        // setup binding for progress update service implementations
        progressUpdateImplementations = MapBinder.newMapBinder(binder(), new TypeLiteral<CoursewareElementType>(){
                }, //
                new TypeLiteral<ProgressUpdateService>() {
                }); //

        synchronizableServiceImplementations = MapBinder.newMapBinder(binder(), new TypeLiteral<EntityType>(){
                }, //
                new TypeLiteral<SynchronizableService>() {
                }); //

        pathwayProgressUpdateImplementations = MapBinder.newMapBinder(binder(), new TypeLiteral<PathwayType>(){
                }, //
                new TypeLiteral<PathwayProgressUpdateService<? extends LearnerPathway>>() {
                }); //
        // setup binding of listMutationOperations implementations
        listMutationOperations = MapBinder.newMapBinder(binder(),  //
                                                        new TypeLiteral<MutationOperator>() {
                                                        }, //
                                                        new TypeLiteral<ListMutationOperation>() {
                                                        });
        mutationOperations = MapBinder.newMapBinder(binder(),  //
                                                    new TypeLiteral<DataType>() {
                                                    }, //
                                                    new TypeLiteral<Map<MutationOperator, MutationOperation>>() {
                                                    });

        listMutationOperations.addBinding(MutationOperator.ADD).to(ListMutationOperationAdd.class);
        listMutationOperations.addBinding(MutationOperator.REMOVE).to(ListMutationOperationRemove.class);
        listMutationOperations.addBinding(MutationOperator.SET).to(ListMutationOperationSet.class);

        mutationOperations.addBinding(DataType.STRING).toInstance(new HashMap<MutationOperator, MutationOperation>() {
            {
                put(MutationOperator.ADD, new UnsupportedMutationOperation());
                put(MutationOperator.REMOVE, new UnsupportedMutationOperation());
                put(MutationOperator.SET, new MutationOperationSet());
            }
        });
        mutationOperations.addBinding(DataType.NUMBER).toInstance(new HashMap<MutationOperator, MutationOperation>() {
            {
                put(MutationOperator.ADD, new SumMutationOperation());
                put(MutationOperator.REMOVE, new DifferenceMutationOperation());
                put(MutationOperator.SET, new MutationOperationSet());
            }
        });
        mutationOperations.addBinding(DataType.BOOLEAN).toInstance(new HashMap<MutationOperator, MutationOperation>() {
            {
                put(MutationOperator.ADD, new UnsupportedMutationOperation());
                put(MutationOperator.REMOVE, new UnsupportedMutationOperation());
                put(MutationOperator.SET, new MutationOperationSet());
            }
        });


        install(new EvaluationOperationsModule());

        this.decorate();
    }

    /**
     * Assisted injection for LearnerAssetService
     *
     * @param learnerAssetService the service to provide with the defined name
     * @return the learner implementation of the AssetService
     */
    @Provides
    @Named("LearnerAssetService")
    AssetService provideAssetService(LearnerAssetService learnerAssetService) {
        return learnerAssetService;
    }

    @Provides
    public CacheConfig provideCacheConfig(ConfigurationService configurationService) {
        return configurationService.get(CacheConfig.class, "cache");
    }

    @Provides
    public RouteConsumersConfig provideRouteConsumerConfig(ConfigurationService configurationService) {
        return configurationService.get(RouteConsumersConfig.class, "routeConsumers");
    }

    @Provides
    public PassportConfig providePassportConfig(ConfigurationService configurationService) {
        return configurationService.get(PassportConfig.class, "passport");
    }

    @Provides
    public GradePassbackConfig provideGradePassbackConfigConfig(ConfigurationService configurationService) {
        return configurationService.get(GradePassbackConfig.class, "grade_passback");
    }

    @Provides
    public PublishMetadataConfig providePublishMetadataConfig(ConfigurationService configurationService) {
        return configurationService.get(PublishMetadataConfig.class, "publish_metadata");
    }

    @Provides
    public EvaluationFeatureConfig provideEvaluationFeatureConfig(ConfigurationService configurationService) {
        EvaluationFeatureConfig config = configurationService.get(EvaluationFeatureConfig.class, "evaluation.feature");
        // make this safe, if no configs are found then default to the camel evaluation
        if (config == null) {
            if (log.isDebugEnabled()) {
                log.jsonDebug("missing evaluation.feature configurations. Initializing default value", new HashMap<>());
            }
            return new EvaluationFeatureConfig()
                    .setConfiguredFeature(EvaluationFeatureConfigurationValues.CAMEL_EVALUATION);
        }

        return config;
    }

    @Provides
    public EvaluationFeatureMode provideEvaluationFeatureMode(ConfigurationService configurationService) {
        EvaluationFeatureMode mode = configurationService.get(EvaluationFeatureMode.class, "evaluation.mode");
        // make this safe, if no mode is found then use DEFAULT
        if (mode == null) {
            if (log.isDebugEnabled()) {
                log.jsonDebug("missing evaluation.mode. Initializing default value", new HashMap<>());
            }
            return new EvaluationFeatureMode().setProcessingMode("DEFAULT");
        }

        return mode;
    }

    @Provides
    @Singleton
    LatestDeploymentChangeIdCache provideLatestDeploymentChangeIdCache(DeploymentChangeIdGateway gateway) {

        // This provider injects its own private gateway to avoid circular dependencies from injecting the whole
        // DeploymentService

        LoadingCache<UUID, UUID> cache = CacheBuilder
                .newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build(new CacheLoader<UUID, UUID>() {
                    @Override
                    public UUID load(@NonNull UUID deploymentId) throws Exception {
                        return gateway.findLatestChangeId(deploymentId)
                                .block();
                    }
                });

        return new LatestDeploymentChangeIdCache(cache);
    }

    @Provides
    CsgConfig provideCsgConfig(ConfigurationService configurationService) {
        return configurationService.get(CsgConfig.class, "csg");
    }
}
