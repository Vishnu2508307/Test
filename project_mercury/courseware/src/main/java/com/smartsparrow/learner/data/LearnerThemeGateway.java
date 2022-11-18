package com.smartsparrow.learner.data;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Singleton
public class LearnerThemeGateway {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(LearnerThemeGateway.class);

    private final Session session;
    private final LearnerThemeByElementMaterializer learnerThemeByElementMaterializer;
    private final LearnerThemeByElementMutator learnerThemeByElementMutator;
    private final LearnerThemeVariantMaterializer learnerThemeVariantMaterializer;
    private final LearnerThemeVariantMutator learnerThemeVariantMutator;
    private final LearnerThemeVariantdefaultMaterializer learnerThemeVariantdefaultMaterializer;
    private final LearnerThemeVariantDefaultMutator learnerThemeVariantDefaultMutator;

    @Inject
    public LearnerThemeGateway(Session session,
                               final LearnerThemeByElementMaterializer learnerThemeByElementMaterializer,
                               final LearnerThemeByElementMutator learnerThemeByElementMutator,
                               final LearnerThemeVariantMaterializer learnerThemeVariantMaterializer,
                               final LearnerThemeVariantMutator learnerThemeVariantMutator,
                               final LearnerThemeVariantdefaultMaterializer learnerThemeVariantdefaultMaterializer,
                               final LearnerThemeVariantDefaultMutator learnerThemeVariantDefaultMutator) {
        this.session = session;
        this.learnerThemeByElementMaterializer = learnerThemeByElementMaterializer;
        this.learnerThemeByElementMutator = learnerThemeByElementMutator;
        this.learnerThemeVariantMaterializer = learnerThemeVariantMaterializer;
        this.learnerThemeVariantMutator = learnerThemeVariantMutator;
        this.learnerThemeVariantdefaultMaterializer = learnerThemeVariantdefaultMaterializer;
        this.learnerThemeVariantDefaultMutator = learnerThemeVariantDefaultMutator;
    }

    /**
     * Persist learner theme variant information
     *
     * @param learnerThemeVariant, the theme variant object
     */
    @Trace(async = true)
    public Mono<Void> persistThemeVariant(final LearnerThemeVariant learnerThemeVariant) {
        Flux<? extends Statement> iter = Mutators.upsert(learnerThemeVariantMutator,
                                                         learnerThemeVariant);
        return Mutators.execute(session, iter)
                .doOnEach(log.reactiveErrorThrowable("error while saving learner theme variant",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("themeId", learnerThemeVariant.getThemeId());
                                                             put("variantId", learnerThemeVariant.getVariantId());
                                                         }
                                                     }))
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist learner default theme variant information
     *
     * @param learnerThemeVariant, the theme variant object
     */
    @Trace(async = true)
    public Mono<Void> persistDefaultThemeVariant(final LearnerThemeVariant learnerThemeVariant) {
        Flux<? extends Statement> iter = Mutators.upsert(learnerThemeVariantDefaultMutator,
                                                         learnerThemeVariant);
        return Mutators.execute(session, iter)
                .doOnEach(log.reactiveErrorThrowable("error while saving learner default theme variant",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("themeId", learnerThemeVariant.getThemeId());
                                                             put("variantId", learnerThemeVariant.getVariantId());
                                                         }
                                                     }))
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist learner theme variant information
     *
     * @param themeByElement, the theme by element
     */
    @Trace(async = true)
    public Mono<Void> persistThemeElement(final LearnerThemeByElement themeByElement) {
        Flux<? extends Statement> iter = Mutators.upsert(learnerThemeByElementMutator,
                                                         themeByElement);
        return Mutators.execute(session, iter)
                .doOnEach(log.reactiveErrorThrowable("error while saving learner default theme variant",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("themeId", themeByElement.getThemeId());
                                                             put("elementId", themeByElement.getElementId());
                                                         }
                                                     }))
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch learner theme variant information by theme id
     *
     * @param themeId, the theme id
     * @return flux of theme variant object
     */
    @Trace(async = true)
    public Flux<LearnerThemeVariant> fetchVariantsByThemeId(final UUID themeId) {
        return ResultSets.query(session, learnerThemeVariantMaterializer.findVariantByThemeId(themeId))
                .flatMapIterable(row -> row)
                .map(learnerThemeVariantMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.reactiveError(String.format("error while fetching learner theme variant %s", themeId));
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetch learner theme info by element id
     *
     * @param elementId the element id
     * @return mono of theme by element
     */
    @Trace(async = true)
    public Mono<LearnerThemeByElement> fetchThemeByElement(final UUID elementId) {
        return ResultSets.query(session, learnerThemeByElementMaterializer.findThemeByElementId(elementId))
                .flatMapIterable(row -> row)
                .map(learnerThemeByElementMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.reactiveError(String.format("error while fetching learner theme by element %s", elementId));
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetch learner theme variant information by theme id and variant id
     *
     * @param themeId, the theme id
     * @param variantId, the variant id
     * @return mono of learner theme variant
     */
    @Trace(async = true)
    public Mono<LearnerThemeVariant> fetchVariantsByThemeIdAndVariantId(final UUID themeId, final UUID variantId) {
        return ResultSets.query(session,
                                learnerThemeVariantMaterializer.findVariantByThemeIdAndVariantId(themeId, variantId))
                .flatMapIterable(row -> row)
                .map(learnerThemeVariantMaterializer::fromRowWithConfig)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.reactiveError(String.format("error while fetching learner theme variant %s", themeId));
                    throw Exceptions.propagate(throwable);
                });
    }

}
