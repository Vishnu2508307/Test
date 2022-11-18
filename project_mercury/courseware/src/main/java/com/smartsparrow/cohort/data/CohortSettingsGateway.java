package com.smartsparrow.cohort.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.Enums;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class CohortSettingsGateway {

    private final static Logger log = LoggerFactory.getLogger(CohortSettingsGateway.class);

    private final Session session;

    private final CohortSettingsMutator cohortSettingsMutator;
    private final CohortSettingsMaterializer cohortSettingsMaterializer;
    private final CohortBannerImageMutator cohortBannerImageMutator;
    private final CohortBannerImageMaterializer cohortBannerImageMaterializer;

    @Inject
    public CohortSettingsGateway(Session session,
                                 CohortSettingsMutator cohortSettingsMutator,
                                 CohortSettingsMaterializer cohortSettingsMaterializer,
                                 CohortBannerImageMutator cohortBannerImageMutator,
                                 CohortBannerImageMaterializer cohortBannerImageMaterializer) {
        this.session = session;
        this.cohortSettingsMutator = cohortSettingsMutator;
        this.cohortSettingsMaterializer = cohortSettingsMaterializer;
        this.cohortBannerImageMutator = cohortBannerImageMutator;
        this.cohortBannerImageMaterializer = cohortBannerImageMaterializer;
    }

    /**
     * Save cohort settings
     *
     * @param cohortSettings the {@link CohortSettings} to save
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> persist(CohortSettings cohortSettings) {
        return Mutators.execute(session, Mutators.upsert(cohortSettingsMutator, cohortSettings))
                .doOnError(throwable -> {
                    log.error(String.format("error while saving cohort settings for cohort %s",
                            cohortSettings.getCohortId()), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Set the learner redirect id for a cohort settings entry
     *
     * @param cohortId the cohort id to set the learner redirect for
     * @param learnerRedirectId an id referencing {@link com.smartsparrow.learner.redirect.LearnerRedirect} object
     * @return a flux of void
     */
    public Flux<Void> updateLearnerRedirectId(final UUID cohortId, final UUID learnerRedirectId) {
        return Mutators.execute(session, Flux.just(cohortSettingsMutator
                .updateLearnerRedirectId(cohortId, learnerRedirectId)))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Update cohort settings
     * @param cohortSettings settings value to save
     */
    public Flux<Void> update(CohortSettings cohortSettings) {
        return Mutators.execute(session, Flux.just(cohortSettingsMutator.update(cohortSettings)))
                .doOnError(throwable -> {
                    log.error(String.format("error while updating cohort settings %s",
                            cohortSettings), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Delete cohort settings
     * @param cohortSettings settings to delete
     */
    public Flux<Void> delete(CohortSettings cohortSettings) {
        return Mutators.execute(session, Mutators.delete(cohortSettingsMutator, cohortSettings))
                .doOnError(throwable -> {
                    log.error(String.format("error while deleting cohort settings for cohort %s",
                            cohortSettings.getCohortId()), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Save a cohort banner image
     *
     * @param cohortBannerImage the {@link CohortBannerImage} to save
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> persist(CohortBannerImage cohortBannerImage) {
        return Mutators.execute(session, Mutators.upsert(cohortBannerImageMutator, cohortBannerImage))
                .doOnError(throwable -> {
                    log.error(String.format("error while saving cohort settings for cohort %s",
                            cohortBannerImage.getCohortId()), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find cohort settings for a cohort
     * @param cohortId the cohort {@link UUID}
     * @return a {@link Mono} of {@link CohortSettings}
     */
    @Trace(async = true)
    public Mono<CohortSettings> findCohortSettings(UUID cohortId) {
        return ResultSets.query(session, cohortSettingsMaterializer.fetchByCohort(cohortId))
                .flatMapIterable(row->row)
                .map(this::settingsFromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching cohort settings for cohort %s", cohortId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find a cohort banner image by cohort and size
     *
     * @param cohortId the cohort {@link UUID}
     * @param size the image size
     * @return a {@link Mono} of {@link CohortBannerImage}
     */
    public Mono<CohortBannerImage> findCohortBannerImage(UUID cohortId, CohortBannerImage.Size size) {
        return ResultSets.query(session, cohortBannerImageMaterializer.findCohortBannerImage(cohortId, size))
                .flatMapIterable(row->row)
                .map(this::bannerImageFromRow)
                .singleOrEmpty()
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching cohort banner image for cohort %s with size %s",
                            cohortId, size), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find cohort banner image by cohort
     *
     * @param cohortId the cohort {@link UUID}
     * @return a {@link Flux} of {@link CohortBannerImage}
     */
    public Flux<CohortBannerImage> findCohortBannerImage(UUID cohortId) {
        return ResultSets.query(session, cohortBannerImageMaterializer.findCohortBannerImage(cohortId))
                .flatMapIterable(row->row)
                .map(this::bannerImageFromRow)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching cohort banner images for cohort %s",
                            cohortId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Convert a row of data to a cohort settings.
     *
     * @param row the {@link Row} to convert
     * @return a {@link CohortSettings}
     */
    private CohortSettings settingsFromRow(Row row) {
        return new CohortSettings()
                .setCohortId(row.getUUID("cohort_id"))
                .setBannerPattern(row.getString("banner_pattern"))
                .setColor(row.getString("color"))
                .setBannerImage(row.getString("banner_image"))
                .setProductId(row.getString("product_id"))
                .setLearnerRedirectId(row.getUUID("learner_redirect_id"));
    }

    /**
     * Convert a row of data to a cohort banner image.
     *
     *
     * @param row the {@link Row} to convert
     * @return a {@link CohortBannerImage}
     */
    private CohortBannerImage bannerImageFromRow(Row row) {
        return new CohortBannerImage()
                .setCohortId(row.getUUID("cohort_id"))
                .setSize(Enums.of(CohortBannerImage.Size.class, row.getString("size")))
                .setBannerImage(row.getString("banner_image"));
    }
}
