package com.smartsparrow.learner.service;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.service.ThemeService;
import com.smartsparrow.courseware.service.ThemeState;
import com.smartsparrow.learner.data.LearnerSelectedThemePayload;
import com.smartsparrow.learner.data.LearnerThemeByElement;
import com.smartsparrow.learner.data.LearnerThemeGateway;
import com.smartsparrow.learner.data.LearnerThemeVariant;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import com.smartsparrow.workspace.data.ThemePayload;
import com.smartsparrow.workspace.data.ThemeVariant;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class LearnerThemeService {

    private final LearnerThemeGateway learnerThemeGateway;
    private final ThemeService themeService;


    @Inject
    public LearnerThemeService(final LearnerThemeGateway learnerThemeGateway,
                               final ThemeService themeService) {
        this.learnerThemeGateway = learnerThemeGateway;
        this.themeService = themeService;
    }

    /**
     * Persist selected theme for an element
     *
     * @param elementId the element id
     * @return mono of void
     */
    @Trace(async = true)
    public Mono<Void> saveSelectedThemeByElement(final UUID elementId) {
        affirmArgument(elementId != null, "missing elementId");

        return themeService.fetchThemeByElementId(elementId)
                .flatMap(themePayload -> saveThemeByElement(themePayload, elementId)
                        .then(saveThemeVariant(themePayload.getThemeVariants())))
                .doOnEach(ReactiveTransaction.linkOnNext());

    }

    /**
     * Persist theme element info
     *
     * @param themePayload the theme payload
     * @param elementId the element id
     * @return mono of void
     */
    @Trace(async = true)
    private Mono<Void> saveThemeByElement(final ThemePayload themePayload,
                                          final UUID elementId) {
        LearnerThemeByElement learnerThemeByElement = new LearnerThemeByElement()
                .setElementId(elementId)
                .setThemeId(themePayload.getId())
                .setThemeName(themePayload.getName());
        return learnerThemeGateway.persistThemeElement(learnerThemeByElement)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist selected theme variants list of a theme
     *
     * @param themeVariantList variant list
     * @return mono of void
     */
    @Trace(async = true)
    private Mono<Void> saveThemeVariant(final List<ThemeVariant> themeVariantList) {

        return themeVariantList.stream()
                .map(themeVariant -> {
                    LearnerThemeVariant learnerThemeVariant = new LearnerThemeVariant()
                            .setThemeId(themeVariant.getThemeId())
                            .setVariantId(themeVariant.getVariantId())
                            .setVariantName(themeVariant.getVariantName())
                            .setConfig(themeVariant.getConfig());

                    if (themeVariant.getState() != null && themeVariant.getState().equals(ThemeState.DEFAULT)) {
                        learnerThemeVariant.setState(themeVariant.getState());
                        return learnerThemeGateway.persistDefaultThemeVariant(learnerThemeVariant)
                                .then(learnerThemeGateway.persistThemeVariant(learnerThemeVariant)).flux();
                    }

                    learnerThemeVariant.setState(ThemeState.NOT_DEFAULT);
                    return learnerThemeGateway.persistThemeVariant(learnerThemeVariant).flux();

                }).reduce(Flux::concatWith)
                .orElse(Flux.empty())
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch selected theme by an element
     *
     * @param elementId the element id
     * @return mono of learner selected theme payload
     */
    @Trace(async = true)
    public Mono<LearnerSelectedThemePayload> fetchSelectedTheme(final UUID elementId) {
        return learnerThemeGateway.fetchThemeByElement(elementId)
                .flatMap(learnerThemeByElement -> fetchThemeVariant(learnerThemeByElement))
                .defaultIfEmpty(new LearnerSelectedThemePayload())
                .doOnEach(ReactiveTransaction.linkOnNext());

    }

    /**
     * Fetch theme variant list
     *
     * @param learnerThemeByElement the learner theme by element
     * @return mono of learner selected theme payload
     */
    @Trace(async = true)
    private Mono<LearnerSelectedThemePayload> fetchThemeVariant(final LearnerThemeByElement learnerThemeByElement) {
        return learnerThemeGateway.fetchVariantsByThemeId(learnerThemeByElement.getThemeId())
                .collectList()
                .flatMap(learnerThemeVariants -> {
                    LearnerSelectedThemePayload learnerSelectedTheme = new LearnerSelectedThemePayload()
                            .setElementId(learnerThemeByElement.getElementId())
                            .setThemeId(learnerThemeByElement.getThemeId())
                            .setThemeName(learnerThemeByElement.getThemeName())
                            .setThemeVariants(learnerThemeVariants);
                    return Mono.just(learnerSelectedTheme);
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch selected theme variant info by theme id and variant id
     * @param themeId the theme id
     * @param variantId the variant id
     * @return mono of learner theme variant
     */
    @Trace(async = true)
    public Mono<LearnerThemeVariant> fetchThemeVariant(final UUID themeId, final UUID variantId) {
        return learnerThemeGateway.fetchVariantsByThemeIdAndVariantId(themeId, variantId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
