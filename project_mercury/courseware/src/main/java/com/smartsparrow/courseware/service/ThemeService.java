package com.smartsparrow.courseware.service;

import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.CoursewareThemeGateway;
import com.smartsparrow.courseware.data.ThemeCoursewareElement;
import com.smartsparrow.iam.data.permission.workspace.ThemePermissionByAccount;
import com.smartsparrow.iam.data.permission.workspace.ThemePermissionByTeam;
import com.smartsparrow.iam.data.permission.workspace.ThemePermissionGateway;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import com.smartsparrow.workspace.data.AccountByTheme;
import com.smartsparrow.workspace.data.IconLibrary;
import com.smartsparrow.workspace.data.ActivityThemeIconLibrary;
import com.smartsparrow.workspace.data.IconLibraryByTheme;
import com.smartsparrow.workspace.data.IconLibraryState;
import com.smartsparrow.workspace.data.TeamByTheme;
import com.smartsparrow.workspace.data.Theme;
import com.smartsparrow.workspace.data.ThemeAccessGateway;
import com.smartsparrow.workspace.data.ThemeByAccount;
import com.smartsparrow.workspace.data.ThemeByTeam;
import com.smartsparrow.workspace.data.ThemeGateway;
import com.smartsparrow.workspace.data.ThemePayload;
import com.smartsparrow.workspace.data.ThemeVariant;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class ThemeService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ThemeService.class);

    private final ThemeGateway themeGateway;
    private final ThemeAccessGateway themeAccessGateway;
    private final ThemePermissionGateway themePermissionGateway;
    private final TeamService teamService;
    private final CoursewareThemeGateway coursewareThemeGateway;
    private final ThemePermissionService themePermissionService;

    @Inject
    public ThemeService(final ThemeGateway themeGateway,
                        final ThemeAccessGateway themeAccessGateway,
                        final ThemePermissionGateway themePermissionGateway,
                        final TeamService teamService,
                        final CoursewareThemeGateway coursewareThemeGateway,
                        final ThemePermissionService themePermissionService) {
        this.themeGateway = themeGateway;
        this.themeAccessGateway = themeAccessGateway;
        this.themePermissionGateway = themePermissionGateway;
        this.teamService = teamService;
        this.coursewareThemeGateway = coursewareThemeGateway;
        this.themePermissionService = themePermissionService;
    }

    /**
     * Creates a theme
     * @param accountId the account id
     * @param name the theme name
     * @return mono of theme
     */
    @Trace(async = true)
    public Mono<Theme> create(final UUID accountId,
                              final String name) {
        affirmArgument(accountId != null, "missing accountId");
        affirmArgument(name != null, "missing theme name");

        UUID themeId = UUIDs.timeBased();
        Theme theme = new Theme()
                .setId(themeId)
                .setName(name);

        return themeGateway.persistTheme(theme)
                .thenEmpty(saveAccountPermissions(accountId, themeId, PermissionLevel.OWNER))
                .then(Mono.just(theme))
                .doOnEach(ReactiveTransaction.linkOnNext());

    }

    /**
     * Save account permissions for a theme
     *
     * @param accountId account id to be granted with permission
     * @param themeId theme id
     * @param permissionLevel permission level
     */
    @Trace(async = true)
    public Flux<Void> saveAccountPermissions(final UUID accountId, final UUID themeId, final PermissionLevel permissionLevel) {
        affirmArgument(accountId != null, "accountId is required");
        affirmArgument(themeId != null, "themeId is required");
        affirmArgument(permissionLevel != null, "permissionLevel is required");

        return Flux.merge(
                themePermissionGateway.persist(new ThemePermissionByAccount()
                                                       .setAccountId(accountId)
                                                       .setThemeId(themeId)
                                                       .setPermissionLevel(permissionLevel))
                        .doOnEach(ReactiveTransaction.linkOnNext()),
                themeAccessGateway.persist(new ThemeByAccount()
                                                   .setAccountId(accountId)
                                                   .setThemeId(themeId))
                        .doOnEach(ReactiveTransaction.linkOnNext()),
                themeAccessGateway.persist(new AccountByTheme()
                                                   .setAccountId(accountId)
                                                   .setThemeId(themeId)
                                                   .setPermissionLevel(permissionLevel))
                        .doOnEach(ReactiveTransaction.linkOnNext()));
    }

    /**
     * Update a theme
     * @param themeId the theme id
     * @param name the theme name
     * @return mono of updated theme object
     */
    @Trace(async = true)
    public Mono<Theme> update(final UUID themeId,
                              final String name) {
        affirmArgument(themeId != null, "missing themeId");
        affirmArgument(name != null, "missing theme name");

        return themeGateway
                .fetchThemeById(themeId)
                .flatMap(theme -> {
                    Theme newTheme = new Theme()
                            .setId(theme.getId())
                            .setName(name);
                    return themeGateway.persistTheme(newTheme)
                            .then(Mono.just(newTheme));
                })
                .doOnEach(ReactiveTransaction.linkOnNext());

    }

    /**
     * Fetch theme by id
     * @param themeId the theme id
     * @return mono of theme object
     */
    @Trace(async = true)
    public Mono<Theme> fetchThemeById(final UUID themeId) {
        affirmArgument(themeId != null, "missing themeId");
        return themeGateway
                .fetchThemeById(themeId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Save team permissions for a theme
     *
     * @param teamId the team id to save the permission for
     * @param themeId the themeId id the team will have permission over
     * @param permissionLevel the permission level
     */
    @Trace(async = true)
    public Flux<Void> saveTeamPermission(final UUID teamId, final UUID themeId, final PermissionLevel permissionLevel) {
        affirmArgument(teamId != null, "teamId is required");
        affirmArgument(themeId != null, "themeId is required");
        affirmArgument(permissionLevel != null, "permissionLevel is required");

        return Flux.merge(
                themePermissionGateway.persist(new ThemePermissionByTeam()
                                                       .setTeamId(teamId)
                                                       .setThemeId(themeId)
                                                       .setPermissionLevel(permissionLevel))
                        .doOnEach(ReactiveTransaction.linkOnNext()),
                themeAccessGateway.persist(new ThemeByTeam()
                                                   .setTeamId(teamId)
                                                   .setThemeId(themeId))
                        .doOnEach(ReactiveTransaction.linkOnNext()),
                themeAccessGateway.persist(new TeamByTheme()
                                                   .setTeamId(teamId)
                                                   .setThemeId(themeId)
                                                   .setPermissionLevel(permissionLevel))
                        .doOnEach(ReactiveTransaction.linkOnNext())
        );
    }

    /**
     * Delete team permission over a theme
     *
     * @param teamId the team id to delete the permission for
     * @param themeId the theme id the permission relates to
     * @return a flux of void
     */
    @Trace(async = true)
    public Flux<Void> deleteTeamPermission(final UUID teamId, final UUID themeId) {
        affirmArgument(teamId != null, "teamId is required");
        affirmArgument(themeId != null, "themeId is required");

        return Flux.merge(
                themePermissionGateway.delete(new ThemePermissionByTeam()
                                                      .setTeamId(teamId)
                                                      .setThemeId(themeId))
                        .doOnEach(ReactiveTransaction.linkOnNext()),
                themeAccessGateway.delete(new ThemeByTeam()
                                                  .setTeamId(teamId)
                                                  .setThemeId(themeId))
                        .doOnEach(ReactiveTransaction.linkOnNext()),
                themeAccessGateway.delete(new TeamByTheme()
                                                  .setTeamId(teamId)
                                                  .setThemeId(themeId))
                        .doOnEach(ReactiveTransaction.linkOnNext())
        );
    }

    /**
     * Delete the account permissions over a theme.
     *
     * @param accountId the account to delete the permission for
     * @param themeId the theme id the permission relates to
     */
    @Trace(async = true)
    public Flux<Void> deleteAccountPermissions(final UUID accountId, final UUID themeId) {
        affirmArgument(accountId != null, "accountId is required");
        affirmArgument(themeId != null, "themeId is required");

        return Flux.merge(
                themePermissionGateway.delete(new ThemePermissionByAccount()
                                                      .setAccountId(accountId)
                                                      .setThemeId(themeId))
                        .doOnEach(ReactiveTransaction.linkOnNext()),
                themeAccessGateway.delete(new ThemeByAccount()
                                                  .setAccountId(accountId)
                                                  .setThemeId(themeId))
                        .doOnEach(ReactiveTransaction.linkOnNext()),
                themeAccessGateway.delete(new AccountByTheme()
                                                  .setAccountId(accountId)
                                                  .setThemeId(themeId))
                        .doOnEach(ReactiveTransaction.linkOnNext())
        );
    }

    /**
     * Fetches a list of theme payload an account has access to
     *
     * @param accountId the account id
     * @return flux of themes
     */
    @Trace(async = true)
    public Flux<ThemePayload> fetchThemes(final UUID accountId) {
        affirmArgument(accountId != null, "accountId is required");

        return themeAccessGateway.fetchThemeForAccount(accountId)
                .mergeWith(teamService.findTeamsForAccount(accountId)
                                   .flatMap(teamAccount -> themeAccessGateway.fetchThemeByTeam(teamAccount.getTeamId())))
                .distinct()
                .flatMap(themeId-> getThemePayload(themeId, accountId))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Get theme payload,
     * And list of variant without config for each theme in the payload, except default variant with config
     * @param themeId the theme id
     * @param accountId the account id
     * @return mono of theme payload
     */
    @Trace(async = true)
    private Mono<ThemePayload> getThemePayload(final UUID themeId, final UUID accountId) {
        Mono<Theme> themeMono = themeGateway.fetchThemeById(themeId)
                .defaultIfEmpty(new Theme())
                .doOnEach(ReactiveTransaction.linkOnNext());

        Mono<ThemeVariant> defaultVariantMono = themeGateway.findThemeVariantByState(themeId,
                                                                                     ThemeState.DEFAULT)
                .defaultIfEmpty(new ThemeVariant())
                .doOnEach(ReactiveTransaction.linkOnNext());

        Mono<List<ThemeVariant>> variantListMono = themeGateway.fetchVariantsByThemeId(themeId)
                .collectList()
                .defaultIfEmpty(Collections.emptyList())
                .doOnEach(ReactiveTransaction.linkOnNext());

        Mono<PermissionLevel> permissionLevelMono = themePermissionService.findHighestPermissionLevel(accountId,
                                                                                                      themeId);

        // list of variant, having config info for default variant only
        Mono<List<ThemeVariant>> variantNamesMono = filterConfigAndAddDefaultVariant(variantListMono,
                                                                                     defaultVariantMono)
                .defaultIfEmpty(Collections.emptyList());

        // list of icon library names with status as SELECTED or NOT_SELECTED
        Mono<List<IconLibrary>> iconLibrariesMono = themeGateway.fetchIconLibrariesByThemeId(themeId).collectList()
                .defaultIfEmpty(Collections.emptyList());

        return Mono.zip(themeMono, variantNamesMono, permissionLevelMono, iconLibrariesMono)
                .map(tuple4 -> {
                    Theme theme = tuple4.getT1();
                    List<ThemeVariant> themeVariants = tuple4.getT2();
                    PermissionLevel permissionLevel = tuple4.getT3();
                    List<IconLibrary> iconLibraries = tuple4.getT4();

                    return new ThemePayload()
                            .setId(theme.getId())
                            .setName(theme.getName())
                            .setPermissionLevel(permissionLevel)
                            .setThemeVariants(themeVariants)
                            .setIconLibraries(iconLibraries);
                });

    }

    /**
     * Remove config from list of variant list, and add default variant with config
     * @param variantListMono the theme variant list
     * @param defaultVariantMono the default theme variant
     * @return mono of list of variant name
     */
    public Mono<List<ThemeVariant>> filterConfigAndAddDefaultVariant(final Mono<List<ThemeVariant>> variantListMono,
                                                                     final Mono<ThemeVariant> defaultVariantMono) {
        return Mono.zip(variantListMono, defaultVariantMono)
                .map(tuple2 -> {
                    List<ThemeVariant> variantList = tuple2.getT1();
                    ThemeVariant defaultVariant = tuple2.getT2();
                    //filter default variant from the list and return info without config
                    List<ThemeVariant> updatedVariants = variantList.stream().filter(variant -> !(variant.getVariantId()).equals(
                            defaultVariant.getVariantId())).map(variant -> new ThemeVariant()
                            .setThemeId(variant.getThemeId())
                            .setVariantId(variant.getVariantId())
                            .setVariantName(variant.getVariantName())
                    ).collect(Collectors.toList());
                    // add default variant with config and state info
                    if (defaultVariant != null && defaultVariant.getThemeId()!= null) {
                        updatedVariants.add(defaultVariant);
                    }
                    return updatedVariants;
                });
    }

    /**
     * Fetch theme payload with theme and variant list info
     *
     * @param elementId the courseware element id
     * @return mono of theme payload object
     */
    @Trace(async = true)
    public Mono<ThemePayload> fetchThemeByElementId(final UUID elementId) {
        affirmArgument(elementId != null, "elementId is required");

        return coursewareThemeGateway.fetchThemeByElementId(elementId)
                .flatMap(this::getThemePayload)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Return theme payload with list of all variants for an element
     * @param themeCoursewareElement the theme by element
     * @return return mono of theme payload
     */
    private Mono<ThemePayload> getThemePayload(final ThemeCoursewareElement themeCoursewareElement) {

        return themeGateway.fetchThemeById(themeCoursewareElement.getThemeId())
                .flatMap(themeObj -> {

                    Mono<List<ThemeVariant>> variantListMono = themeGateway.fetchVariantsByThemeId(
                            themeCoursewareElement.getThemeId()).collectList()
                            .defaultIfEmpty(new ArrayList<>());
                    Mono<ThemeVariant> themeVariantByStateMono = themeGateway.findThemeVariantByState(
                            themeCoursewareElement.getThemeId(),
                            ThemeState.DEFAULT)
                            .defaultIfEmpty(new ThemeVariant());

                    Mono<List<IconLibrary>> iconLibrariesMono = themeGateway.fetchIconLibrariesByThemeId(
                            themeCoursewareElement.getThemeId()).collectList()
                            .defaultIfEmpty(new ArrayList<>());

                    return Mono.zip(Mono.just(themeObj), variantListMono, themeVariantByStateMono, iconLibrariesMono)
                            .map(tuple4 -> {
                                Theme theme = tuple4.getT1();
                                List<ThemeVariant> variants = tuple4.getT2();
                                ThemeVariant defaultVariant = tuple4.getT3();
                                List<IconLibrary> iconLibraries = tuple4.getT4();
                                //filter only selected icon libraries
                                List<IconLibrary> selectedIconLibraries = iconLibraries.stream().filter(iconLibrary -> !(iconLibrary.getStatus())
                                        .equals(IconLibraryState.NOT_SELECTED)).collect(Collectors.toList());
                                //filter default variant
                                List<ThemeVariant> updatedVariants = variants.stream().filter(variant -> !(variant.getVariantId()).equals(
                                        defaultVariant.getVariantId())).collect(Collectors.toList());
                                // add default variant info with state and config
                                if (defaultVariant != null && defaultVariant.getThemeId() != null) {
                                    updatedVariants.add(defaultVariant);
                                }

                                return new ThemePayload()
                                        .setId(theme.getId())
                                        .setName(theme.getName())
                                        .setThemeVariants(updatedVariants)
                                        .setIconLibraries(selectedIconLibraries);
                            });
                });

    }

    /**
     * Saves theme and element association
     *
     * @param elementId the courseware element id
     * @param themeId the theme id associated to courseware element
     * @param elementType the element type
     * @return mono of theme by courseware element object
     */
    @Trace(async = true)
    public Mono<ThemeCoursewareElement> saveThemeByElement(final UUID themeId,
                                                           final UUID elementId,
                                                           final CoursewareElementType elementType) {
        affirmArgument(themeId != null, "themeId is required");
        affirmArgument(elementId != null, "elementId is required");
        affirmArgument(elementType != null, "elementType is required");

        ThemeCoursewareElement themeCoursewareElement = new ThemeCoursewareElement()
                .setThemeId(themeId)
                .setElementId(elementId)
                .setElementType(elementType);

        return coursewareThemeGateway.fetchThemeByElementId(elementId)
                .flatMap(themeByElement -> coursewareThemeGateway.deleteElementByTheme(themeByElement))
                .thenMany(coursewareThemeGateway.persist(themeCoursewareElement))
                .then(Mono.just(themeCoursewareElement))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Deletes theme and element association
     *
     * @param elementId the courseware element id
     * @param elementType the element type
     * @return mono of void
     */
    @Trace(async = true)
    public Mono<Void> deleteThemeByElement(final UUID elementId, final CoursewareElementType elementType) {
        affirmArgument(elementId != null, "elementId is required");
        affirmArgument(elementType != null, "elementType is required");

        ThemeCoursewareElement themeCoursewareElement = new ThemeCoursewareElement()
                .setElementId(elementId)
                .setElementType(elementType);

        return coursewareThemeGateway.fetchThemeByElementId(elementId)
                .flatMap(themeByElement -> coursewareThemeGateway.deleteElementByTheme(themeByElement))
                .thenMany(coursewareThemeGateway.delete(themeCoursewareElement))
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete a theme
     * Also, deletes any accounts and teams association with the theme
     * @param themeId the theme id
     * @return flux of void
     */
    @Trace(async = true)
    public Flux<Void> deleteTheme(final UUID themeId) {
        affirmArgument(themeId != null, "themeId is required");

        return Flux.merge(
                themeGateway.delete(new Theme()
                                            .setId(themeId))
                        .doOnEach(ReactiveTransaction.linkOnNext()),

                themeAccessGateway.fetchAccountsForTheme(themeId)
                        .flatMap(accountByTheme -> deleteAccountPermissions(accountByTheme.getAccountId(), themeId)
                                .thenMany(teamService.findTeamsForAccount(accountByTheme.getAccountId())
                                                  .flatMap(teamAccount -> deleteTeamPermission(teamAccount.getTeamId(),
                                                                                               themeId)))),
                themeGateway.deleteThemeVariantAndByState(new ThemeVariant()
                                                                  .setThemeId(themeId))
                        .doOnEach(ReactiveTransaction.linkOnNext()),
               // delete theme association from elements
                coursewareThemeGateway.fetchElementByThemeId(themeId)
                        .flatMap(themeCoursewareElement -> coursewareThemeGateway.deleteElementThemeAssociation(
                                themeCoursewareElement))
                        .doOnEach(ReactiveTransaction.linkOnNext()));
    }

    /**
     * Creates a theme variant
     *
     * @param themeId the theme id
     * @param variantName the theme variant name
     * @param config the theme config
     * @param state the theme state
     * @return mono of theme variant
     */
    @Trace(async = true)
    public Mono<ThemeVariant> createThemeVariant(final UUID themeId,
                                                 final String variantName,
                                                 final String config,
                                                 final ThemeState state) {
        affirmArgument(themeId != null, "missing themeId");
        affirmArgument(variantName != null, "variant name");
        affirmArgument(config != null, "missing config");

        UUID variantId = UUIDs.timeBased();
        ThemeVariant themeVariant = new ThemeVariant()
                .setThemeId(themeId)
                .setVariantId(variantId)
                .setVariantName(variantName)
                .setConfig(config);

        if (state != null && state.equals(ThemeState.DEFAULT)) {
            themeVariant.setState(ThemeState.DEFAULT);
            return themeGateway.persistThemeVariant(themeVariant)
                            .then(themeGateway.persistThemeVariantByState(themeVariant))
                            .then(Mono.just(themeVariant))
                            .doOnEach(ReactiveTransaction.linkOnNext());
        }
        return themeGateway.persistThemeVariant(themeVariant)
                .then(Mono.just(themeVariant))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete a theme variant
     * @param themeId the theme id
     * @param variantId the variant id
     * @return flux of void
     */
    @Trace(async = true)
    public Flux<Void> deleteThemeVariant(final UUID themeId,
                                         final UUID variantId) {
        affirmArgument(themeId != null, "missing themeId");
        affirmArgument(variantId != null, "missing variantId");

        ThemeVariant themeVariant = new ThemeVariant()
                .setThemeId(themeId)
                .setVariantId(variantId);

        return themeGateway.deleteThemeVariant(themeVariant)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch theme variant info by theme id and state
     * @param themeId the theme id
     * @param state the theme state
     * @param variantId the theme variant id
     * @return mono of theme variant
     */
    @Trace(async = true)
    public Mono<ThemeVariant> fetchThemeVariantByState(final UUID themeId,
                                                              final ThemeState state,
                                                              final UUID variantId) {
        affirmArgument(themeId != null, "missing themeId");
        affirmArgument(state != null, "missing theme state");

        return themeGateway.fetchVariantByStateAndVariantId(themeId, state, variantId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Update theme variant
     * @param themeId the theme id
     * @param variantId the variant id
     * @param variantName the variant name
     * @param config the config
     * @return mono of theme variant
     */
    @Trace(async = true)
    public Mono<ThemeVariant> updateThemeVariant(final UUID themeId,
                                                 final UUID variantId,
                                                 final String variantName,
                                                 final String config) {

        affirmArgument(themeId != null, "missing themeId");
        affirmArgument(variantId != null, "missing variantId");
        affirmArgument(variantName != null, "variant name");
        affirmArgument(config != null, "missing config");

        return themeGateway.fetchVariantByThemeIdAndVariantId(themeId, variantId)
                .flatMap(themeVariant -> {
                    ThemeVariant updatedVariant = new ThemeVariant()
                            .setThemeId(themeId)
                            .setVariantId(variantId)
                            .setVariantName(variantName)
                            .setConfig(config);
                    return themeGateway.persistThemeVariant(updatedVariant)
                            .thenReturn(updatedVariant)
                            .doOnEach(ReactiveTransaction.linkOnNext());
                })

                //check in default table
                .flatMap(updatedVariant -> themeGateway.fetchVariantByStateAndVariantId(themeId,
                                                                                        ThemeState.DEFAULT,
                                                                                        variantId)
                        .flatMap(defaultVariant ->
                                         themeGateway.persistThemeVariantByState(updatedVariant.setState(
                                                 ThemeState.DEFAULT))
                                                 .thenReturn(updatedVariant)
                                                 .doOnEach(ReactiveTransaction.linkOnNext()))
                        .thenReturn(updatedVariant));

    }

    /**
     * Get theme variant info by theme id and variant id
     * @param themeId the theme id
     * @param variantId the variant id
     * @return mono of theme variant object
     */
    @Trace(async = true)
    public Mono<ThemeVariant> getThemeVariant(final UUID themeId,
                                              final UUID variantId) {

        affirmArgument(themeId != null, "missing themeId");
        affirmArgument(variantId != null, "missing variantId");

        return themeGateway.fetchVariantByStateAndVariantId(themeId, ThemeState.DEFAULT, variantId)
                //check in variantTheme table , if above returns empty
                .switchIfEmpty(themeGateway.fetchVariantByThemeIdAndVariantId(themeId, variantId))
                .defaultIfEmpty(new ThemeVariant())
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * fetch default theme variant by theme id
     * @param themeId the theme id
     * @return mono of theme variant
     */
    @Trace(async = true)
    public Mono<ThemeVariant> findThemeVariantByState(final UUID themeId, final ThemeState state) {
        return themeGateway.findThemeVariantByState(themeId, state)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Associates a theme and iconLibrary info
     * @param themeId the theme id
     * @param iconLibraries the list of iconLibrary with name and status
     * @return flux of IconLibraryByTheme object
     */
    @Trace(async = true)
    public Flux<IconLibraryByTheme> saveThemeIconLibraries(final UUID themeId,
                                                           final List<IconLibrary> iconLibraries) {
        affirmArgument(themeId != null, "missing themeId");
        affirmArgumentNotNullOrEmpty(iconLibraries, "missing icon library info");
        return iconLibraries.stream()
                .map(iconLibrary -> {
                    if (iconLibrary.getStatus() == null || !iconLibrary.getStatus().equals(IconLibraryState.SELECTED)) {
                        iconLibrary.setStatus(IconLibraryState.NOT_SELECTED);
                    }
                    IconLibraryByTheme iconLibraryByTheme = new IconLibraryByTheme()
                            .setThemeId(themeId)
                            .setIconLibrary(iconLibrary.getName())
                            .setStatus(iconLibrary.getStatus());
                    return themeGateway.persistIconLibraryByTheme(iconLibraryByTheme)
                            .doOnEach(ReactiveTransaction.linkOnNext())
                            .then(Mono.just(iconLibraryByTheme)).flux();
                })
                .reduce(Flux::concatWith)
                .orElse(Flux.empty());
    }

    /**
     * Fetches a list of account collaborators for a theme
     *
     * @param themeId the theme id
     * @return a flux of AccountByTheme {@link AccountByTheme}
     */
    @Trace(async = true)
    public Flux<AccountByTheme> fetchAccountCollaborators(final UUID themeId) {
        affirmArgument(themeId != null, "themeId is required");
        return themeAccessGateway.fetchAccountsForTheme(themeId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetches a list of team collaborators for a theme
     *
     * @param themeId the theme id
     * @return a flux of TeamByTheme {@link TeamByTheme}
     */
    @Trace(async = true)
    public Flux<TeamByTheme> fetchTeamCollaborators(final UUID themeId) {
        affirmArgument(themeId != null, "themeId is required");
        return themeAccessGateway.findTeamsByTheme(themeId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Associates an activityId and iconLibrary for activity theme
     * @param activityId the activity id
     * @param iconLibraries the list of iconLibrary with name and status
     * @return flux of ActivityThemeIconLibrary object
     */
    @Trace(async = true)
    public Flux<ActivityThemeIconLibrary> saveActivityThemeIconLibraries(final UUID activityId,
                                                                         final List<IconLibrary> iconLibraries) {
        affirmArgument(activityId != null, "missing activityId");
        affirmArgumentNotNullOrEmpty(iconLibraries, "missing icon library info");
        return iconLibraries.stream()
                .map(iconLibrary -> {
                    if (iconLibrary.getStatus() == null || !iconLibrary.getStatus().equals(IconLibraryState.SELECTED)) {
                        iconLibrary.setStatus(IconLibraryState.NOT_SELECTED);
                    }
                    ActivityThemeIconLibrary activityThemeIconLibrary = new ActivityThemeIconLibrary()
                            .setActivityId(activityId)
                            .setIconLibrary(iconLibrary.getName())
                            .setStatus(iconLibrary.getStatus());
                    return themeGateway.persistActivityThemeIconLibrary(activityThemeIconLibrary)
                            .doOnEach(ReactiveTransaction.linkOnNext())
                            .then(Mono.just(activityThemeIconLibrary)).flux();
                })
                .reduce(Flux::concatWith)
                .orElse(Flux.empty());
    }

    /**
     * Fetches activity theme icon libraries
     *
     * @param activityId the activity id
     * @return a flux of ActivityThemeIconLibrary
     */
    @Trace(async = true)
    public Flux<IconLibrary> fetchActivityThemeIconLibraries(UUID activityId) {
        affirmArgument(activityId != null, "missing activityId");
        return themeGateway.fetchActivityThemeIconLibraries(activityId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
