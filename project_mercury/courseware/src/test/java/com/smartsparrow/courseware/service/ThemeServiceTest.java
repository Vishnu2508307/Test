package com.smartsparrow.courseware.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.CoursewareThemeGateway;
import com.smartsparrow.courseware.data.ThemeCoursewareElement;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.data.permission.workspace.ThemePermissionByAccount;
import com.smartsparrow.iam.data.permission.workspace.ThemePermissionByTeam;
import com.smartsparrow.iam.data.permission.workspace.ThemePermissionGateway;
import com.smartsparrow.iam.data.team.TeamAccount;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
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

public class ThemeServiceTest {

    @InjectMocks
    ThemeService themeService;
    @Mock
    ThemeGateway themeGateway;
    @Mock
    ThemeAccessGateway themeAccessGateway;
    @Mock
    ThemePermissionGateway themePermissionGateway;
    @Mock
    TeamService teamService;
    @Mock
    CoursewareThemeGateway coursewareThemeGateway;
    @Mock
    ThemePermissionService themePermissionService;

    private static final UUID teamId = UUID.randomUUID();
    private static final UUID variantId = UUID.randomUUID();
    private static final UUID themeId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final String name = "Theme_one";
    private static final String variantName = "Day";
    private static final String config = " {\"colo\":\"orange\", \"margin\":\"20\"}";
    private static final UUID activityId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(coursewareThemeGateway.deleteElementByTheme(any(ThemeCoursewareElement.class))).thenReturn(Mono.empty());
        when(coursewareThemeGateway.fetchThemeByElementId(any())).thenReturn(Mono.just(new ThemeCoursewareElement()
                                                                                               .setThemeId(themeId)
                                                                                               .setElementId(elementId)));
    }

    @Test
    void create_NullName() {
        Throwable e = assertThrows(IllegalArgumentFault.class,
                                   () -> themeService.create(accountId, null));
        assertEquals(e.getMessage(), "missing theme name");
    }

    @Test
    void create_NullAccountId() {
        Throwable e = assertThrows(IllegalArgumentFault.class,
                                   () -> themeService.create(null, name));
        assertEquals(e.getMessage(), "missing accountId");
    }


    @Test
    void create_success() {
        when(themeGateway.persistTheme(any())).thenReturn(Mono.empty());
        when(themePermissionGateway.persist(any(ThemePermissionByAccount.class))).thenReturn(Flux.empty());
        when(themeAccessGateway.persist(any(ThemeByAccount.class))).thenReturn(Flux.empty());
        when(themeAccessGateway.persist(any(AccountByTheme.class))).thenReturn(Flux.empty());
        Theme theme = themeService.create(accountId, name).block();
        assertNotNull(theme);
        verify(themeGateway, atMost(1)).persistTheme(any());
        verify(themePermissionGateway, atMost(1)).persist(any(ThemePermissionByAccount.class));
        verify(themeAccessGateway, atMost(1)).persist(any(ThemeByAccount.class));
        verify(themeAccessGateway, atMost(1)).persist(any(AccountByTheme.class));
    }

    @Test
    void update_success() {
        when(themeGateway.fetchThemeById(any())).thenReturn(Mono.just(new Theme()
        .setId(themeId)
        .setName(name)));
        when(themeGateway.persistTheme(any())).thenReturn(Mono.empty());

        Theme theme = themeService.update(themeId, name).block();
        assertNotNull(theme);

        verify(themeGateway, atMost(1)).persistTheme(any());
        verify(themeGateway, atMost(1)).fetchThemeById(any());
    }

    @Test
    void saveTeamPermission_success() {
        UUID teamId = UUID.randomUUID();
        when(themePermissionGateway.persist(any(ThemePermissionByTeam.class))).thenReturn(Flux.empty());
        when(themeAccessGateway.persist(any(ThemeByTeam.class))).thenReturn(Flux.empty());
        when(themeAccessGateway.persist(any(TeamByTheme.class))).thenReturn(Flux.empty());

        themeService.saveTeamPermission(teamId, themeId, PermissionLevel.CONTRIBUTOR);

        verify(themePermissionGateway, atMost(1)).persist(any(ThemePermissionByTeam.class));
        verify(themeAccessGateway, atMost(1)).persist(any(ThemeByTeam.class));
        verify(themeAccessGateway, atMost(1)).persist(any(TeamByTheme.class));
    }

    @Test
    void deleteTeamPermission_success() {
        UUID teamId = UUID.randomUUID();
        when(themePermissionGateway.delete(any(ThemePermissionByTeam.class))).thenReturn(Flux.empty());
        when(themeAccessGateway.delete(any(ThemeByTeam.class))).thenReturn(Flux.empty());
        when(themeAccessGateway.delete(any(TeamByTheme.class))).thenReturn(Flux.empty());

        themeService.deleteTeamPermission(teamId, themeId);

        verify(themePermissionGateway, atMost(1)).delete(any(ThemePermissionByTeam.class));
        verify(themeAccessGateway, atMost(1)).delete(any(ThemeByTeam.class));
        verify(themeAccessGateway, atMost(1)).delete(any(TeamByTheme.class));
    }

    @Test
    void deleteAccountPermissions_success() {
        when(themePermissionGateway.delete(any(ThemePermissionByAccount.class))).thenReturn(Flux.empty());
        when(themeAccessGateway.delete(any(ThemeByAccount.class))).thenReturn(Flux.empty());
        when(themeAccessGateway.delete(any(AccountByTheme.class))).thenReturn(Flux.empty());

        themeService.deleteAccountPermissions(accountId, themeId);

        verify(themePermissionGateway, atMost(1)).delete(any(ThemePermissionByAccount.class));
        verify(themeAccessGateway, atMost(1)).delete(any(ThemeByAccount.class));
        verify(themeAccessGateway, atMost(1)).delete(any(AccountByTheme.class));
    }
    @Test
    void fetchThemes_success() {
        UUID themeId = UUID.randomUUID();
        when(themeAccessGateway.fetchThemeForAccount(any())).thenReturn(Flux.just(themeId));
        when(teamService.findTeamsForAccount(any())).thenReturn(Flux.just(new TeamAccount()
                                                                                  .setTeamId(teamId)
                                                                                  .setAccountId(accountId)));
        when(themeAccessGateway.fetchThemeByTeam(any())).thenReturn(Flux.just(themeId));
        when(themePermissionService.findHighestPermissionLevel(any(), any())).thenReturn(Mono.just(PermissionLevel.OWNER));
        when(themeGateway.fetchVariantsByThemeId(any())).thenReturn(Flux.just(new ThemeVariant()
        .setThemeId(themeId)
        .setVariantId(variantId)));
        when( themeGateway.findThemeVariantByState(any(), any())).thenReturn(Mono.just(new ThemeVariant()
        .setVariantId(variantId)
        .setThemeId(themeId)));
        when(themeGateway.fetchThemeById(any())).thenReturn(Mono.just(new Theme()
        .setName("Theme_name")
        .setId(themeId)));
        when(themeGateway.fetchIconLibrariesByThemeId(themeId)).thenReturn(Flux.just(new IconLibrary().setName("Icon_library")
                                                                                             .setStatus(IconLibraryState.SELECTED)));

        List<ThemePayload> theme = themeService.fetchThemes(accountId).collectList().block();
        assertNotNull(theme);
        assertNotNull(theme.get(0).getId());

        verify(themeAccessGateway, atMost(1)).fetchThemeForAccount(any());
        verify(teamService, atMost(1)).findTeamsForAccount(any());
        verify(themeAccessGateway, atMost(1)).fetchThemeByTeam(any());
        verify(themeGateway, atMost(1)).findThemeVariantByState(any(), any());
        verify(themePermissionService, atMost(1)).findHighestPermissionLevel(any(), any());
        verify(themeGateway, atMost(1)).fetchIconLibrariesByThemeId(any());
    }

    @Test
    void fetchThemesByElementId_error() {
        Throwable e = assertThrows(IllegalArgumentFault.class,
                                   () -> themeService.fetchThemeByElementId(null));
        assertEquals(e.getMessage(), "elementId is required");
    }

    @Test
    void fetchThemesByElementId_success() {
        UUID elementId = UUID.randomUUID();

        when(themeGateway.fetchThemeById(any())).thenReturn(Mono.just(new Theme().setName("Theme_name").setId(themeId)));
        when(themeGateway.fetchVariantsByThemeId(any())).thenReturn(Flux.just(new ThemeVariant().setThemeId(themeId)
                                                                                               .setVariantId(variantId)
                                                                                               .setVariantName(variantName)));
        when(themeGateway.findThemeVariantByState(themeId,
                                                  ThemeState.DEFAULT)).thenReturn(Mono.just(new ThemeVariant()));
        when(themeGateway.fetchIconLibrariesByThemeId(themeId)).thenReturn(Flux.just(new IconLibrary().setName("Icon_library")
                                                                                             .setStatus(IconLibraryState.SELECTED)));
        ThemePayload themePayload = themeService.fetchThemeByElementId(elementId).block();
        assertNotNull(themePayload);
        assertNotNull(themePayload.getId());

        verify(coursewareThemeGateway, atMost(1)).fetchThemeByElementId(any());
        verify(themeGateway, atMost(1)).fetchThemeById(any());
        verify(themeGateway, atMost(1)).fetchVariantByThemeIdAndVariantId(any(), any());
        verify(themeGateway, atMost(1)).fetchIconLibrariesByThemeId(any());
    }

    @Test
    void saveThemeByElement_success() {
        UUID elementId = UUID.randomUUID();
        when(coursewareThemeGateway.persist(any(ThemeCoursewareElement.class))).thenReturn(Flux.empty());

        themeService.saveThemeByElement(themeId, elementId, CoursewareElementType.ACTIVITY);

        verify(coursewareThemeGateway, atMost(1)).persist(any(ThemeCoursewareElement.class));
    }

    @Test
    void deleteThemeByElement_success() {
        UUID elementId = UUID.randomUUID();
        when(coursewareThemeGateway.delete(any(ThemeCoursewareElement.class))).thenReturn(Flux.empty());

        themeService.deleteThemeByElement(elementId, CoursewareElementType.ACTIVITY);

        verify(coursewareThemeGateway, atMost(1)).delete(any(ThemeCoursewareElement.class));
    }

    @Test
    void deleteTheme_success(){
        when(themeGateway.delete(any(Theme.class))).thenReturn(Flux.empty());
        when(themeGateway.deleteThemeVariantAndByState(any(ThemeVariant.class))).thenReturn(Flux.empty());

        when(themePermissionGateway.delete(any(ThemePermissionByAccount.class))).thenReturn(Flux.empty());
        when(themeAccessGateway.delete(any(ThemeByAccount.class))).thenReturn(Flux.empty());
        when(themeAccessGateway.delete(any(AccountByTheme.class))).thenReturn(Flux.empty());

        when(themePermissionGateway.delete(any(ThemePermissionByTeam.class))).thenReturn(Flux.empty());
        when(themeAccessGateway.delete(any(ThemeByTeam.class))).thenReturn(Flux.empty());
        when(themeAccessGateway.delete(any(TeamByTheme.class))).thenReturn(Flux.empty());

        when(themeAccessGateway.fetchAccountsForTheme(any())).thenReturn(Flux.just(new AccountByTheme()
        .setAccountId(accountId)
        .setThemeId(themeId)));
        when(teamService.findTeamsForAccount(any())).thenReturn(Flux.just(new TeamAccount()
                                                                                  .setTeamId(teamId)
                                                                                  .setAccountId(accountId)));
        when(coursewareThemeGateway.fetchElementByThemeId(themeId)).thenReturn(Flux.just(new ThemeCoursewareElement()
                                                                                                 .setThemeId(themeId)
                                                                                                 .setElementId(elementId)));

        themeService.deleteTheme(themeId);

        verify(themeGateway, atMost(1)).delete(any(Theme.class));

        verify(themeAccessGateway, atMost(1)).fetchThemeForAccount(any());
        verify(teamService, atMost(1)).findTeamsForAccount(any());

        verify(themePermissionGateway, atMost(1)).delete(any(ThemePermissionByTeam.class));
        verify(themeAccessGateway, atMost(1)).delete(any(ThemeByTeam.class));
        verify(themeAccessGateway, atMost(1)).delete(any(TeamByTheme.class));

        verify(themePermissionGateway, atMost(1)).delete(any(ThemePermissionByAccount.class));
        verify(themeAccessGateway, atMost(1)).delete(any(ThemeByAccount.class));
        verify(themeAccessGateway, atMost(1)).delete(any(AccountByTheme.class));
        verify(themeGateway, atMost(1)).deleteThemeVariantAndByState(any(ThemeVariant.class));
    }

    @Test
    void createThemeVariant_success(){
        when(themeGateway.persistThemeVariant(any(ThemeVariant.class))).thenReturn(Mono.empty());
        when(themeGateway.persistThemeVariantByState(any(ThemeVariant.class))).thenReturn(Mono.empty());
        ThemeVariant themeVariant = themeService.createThemeVariant(themeId,
                                        variantName,
                                        config,
                                        null).block();
        assertNotNull(themeVariant);
        verify(themeGateway, atMost(1)).persistThemeVariant(any());
        verify(themeGateway, atMost(0)).persistThemeVariantByState(any());
    }

    @Test
    void createThemeVariant_success_Default(){
        when(themeGateway.persistThemeVariantByState(any(ThemeVariant.class))).thenReturn(Mono.empty());
        when(themeGateway.persistThemeVariant(any(ThemeVariant.class))).thenReturn(Mono.empty());
        ThemeVariant themeVariant = themeService.createThemeVariant(themeId,
                                                                    variantName,
                                                                    config,
                                                                    ThemeState.DEFAULT).block();
        assertNotNull(themeVariant);
        verify(themeGateway, atMost(1)).persistThemeVariantByState(any());
        verify(themeGateway, atMost(1)).persistThemeVariant(any());
    }

    @Test
    void deleteThemeVariant_success_Default() {
        when(themeGateway.deleteThemeVariant(any(ThemeVariant.class))).thenReturn(Flux.empty());

        themeService.deleteThemeVariant(themeId,
                                        variantId);
        verify(themeGateway, atMost(1)).deleteThemeVariant(any());
    }

    @Test
    void filterVariantNames_withDefault(){
        UUID varinatId_one = UUID.randomUUID();
        UUID varinatId_two = UUID.randomUUID();
        UUID varinatId_three = UUID.randomUUID();
        ThemeVariant themeVariantByState = new ThemeVariant()
                .setThemeId(themeId)
                .setVariantId(varinatId_one)
                .setVariantName("Day")
                .setConfig("config1")
                .setState(ThemeState.DEFAULT);
        ThemeVariant themeVariant_one = new ThemeVariant()
                .setThemeId(themeId)
                .setVariantId(varinatId_three)
                .setVariantName("Sepia")
                .setConfig("config2");
        ThemeVariant themeVariant_two = new ThemeVariant()
                .setThemeId(themeId)
                .setVariantId(varinatId_two)
                .setVariantName("Dark")
                .setConfig("config3");

        List<ThemeVariant> variantList = new LinkedList<>();
        variantList.add(themeVariant_one);
        variantList.add(themeVariant_two);

        List<ThemeVariant> themeVariantList = themeService.filterConfigAndAddDefaultVariant(Mono.just(variantList), Mono.just(themeVariantByState)).block();
        assertNotNull(themeVariantList);
        assertEquals(3, themeVariantList.size());
    }

    @Test
    void filterVariantNames_missingDefault(){
        UUID varinatId_one = UUID.randomUUID();
        UUID varinatId_two = UUID.randomUUID();

        ThemeVariant themeVariant_one = new ThemeVariant()
                .setThemeId(themeId)
                .setVariantId(varinatId_one)
                .setVariantName("Day")
                .setConfig("config1");
        ThemeVariant themeVariant_two = new ThemeVariant()
                .setThemeId(themeId)
                .setVariantId(varinatId_two)
                .setVariantName("Sepia")
                .setConfig("config2");

        List<ThemeVariant> variantList = new LinkedList<>();
        variantList.add(themeVariant_one);
        variantList.add(themeVariant_two);

        List<ThemeVariant> themeVariantList = themeService.filterConfigAndAddDefaultVariant(Mono.just(variantList), Mono.just(new ThemeVariant())).block();
        assertNotNull(themeVariantList);
        assertEquals(2, themeVariantList.size());
        assertTrue(themeVariantList.get(0).getVariantName().contains("Day"));
        assertTrue(themeVariantList.get(1).getVariantName().contains("Sepia"));
    }

    @Test
    void filterVariantNames_OnlyDefault(){
        UUID varinatId_one = UUID.randomUUID();
        UUID varinatId_two = UUID.randomUUID();

        ThemeVariant themeVariantByState = new ThemeVariant()
                .setThemeId(themeId)
                .setVariantId(varinatId_one)
                .setVariantName("Day")
                .setConfig("config1")
                .setState(ThemeState.DEFAULT);
        ThemeVariant themeVariant_one = new ThemeVariant()
                .setThemeId(themeId)
                .setVariantId(varinatId_two)
                .setVariantName("Sepia")
                .setConfig("config2");

        List<ThemeVariant> variantList = new ArrayList<>();
        variantList.add(themeVariant_one);

        List<ThemeVariant> themeVariantList = themeService.filterConfigAndAddDefaultVariant(Mono.just(variantList), Mono.just(themeVariantByState)).block();
        assertNotNull(themeVariantList);
        assertEquals(2, themeVariantList.size());
    }

    @Test
    void updateThemeVariant_success() {
        when(themeGateway.fetchVariantByThemeIdAndVariantId(themeId, variantId))
                .thenReturn(Mono.just(new ThemeVariant()
                                              .setThemeId(themeId)
                                              .setVariantId(variantId)
                                              .setVariantName("Day")
                                              .setConfig("config1")));
        when(themeGateway.persistThemeVariant(any(ThemeVariant.class)))
                .thenReturn(Mono.empty());
        when(themeGateway.fetchVariantByStateAndVariantId(themeId, ThemeState.DEFAULT, variantId))
                .thenReturn(Mono.just(new ThemeVariant()
                                              .setThemeId(themeId)
                                              .setVariantId(variantId)
                                              .setVariantName("Day")
                                              .setConfig("config1")
                                              .setState(ThemeState.DEFAULT)));
        when(themeGateway.persistThemeVariantByState(any(ThemeVariant.class)))
                .thenReturn(Mono.empty());
        ThemeVariant themeVariant = themeService.updateThemeVariant(themeId,
                                                                              variantId,
                                                                              variantName,
                                                                              config).block();
        assertNotNull(themeVariant);
        verify(themeGateway, atMost(1)).fetchVariantByThemeIdAndVariantId(any(), any());
        verify(themeGateway, atMost(1)).persistThemeVariant(any());
        verify(themeGateway, atMost(1)).fetchVariantByStateAndVariantId(any(), any(), any());
        verify(themeGateway, atMost(1)).persistThemeVariantByState(any());

    }

    @Test
    void saveThemeIconLibraries_success(){
        when(themeGateway.persistIconLibraryByTheme(any(IconLibraryByTheme.class))).thenReturn(Mono.empty());

        List<IconLibrary> iconLibraries = new ArrayList<>();
        iconLibraries.add(new IconLibrary().setName("FONTAWESOME").setStatus(IconLibraryState.SELECTED));
        iconLibraries.add(new IconLibrary().setName("MICROSOFT"));

        themeService.saveThemeIconLibraries(themeId, iconLibraries);

        verify(themeGateway, atMost(2)).persistIconLibraryByTheme(any());
        assertEquals(IconLibraryState.NOT_SELECTED,iconLibraries.get(1).getStatus());
    }

    @Test
    void fetchAccountCollaborators_success(){
        when(themeAccessGateway.fetchAccountsForTheme(themeId)).thenReturn(Flux.just(new AccountByTheme()));


        AccountByTheme accountByTheme = themeService.fetchAccountCollaborators(themeId).blockFirst();
        assertNotNull(accountByTheme);

        verify(themeAccessGateway, atMost(1)).fetchAccountsForTheme(themeId);
    }

    @Test
    void fetchTeamCollaborators_success() {
        when(themeAccessGateway.findTeamsByTheme(themeId)).thenReturn(Flux.just(new TeamByTheme()));

        TeamByTheme teamByTheme = themeService.fetchTeamCollaborators(themeId).blockFirst();
        assertNotNull(teamByTheme);

        verify(themeAccessGateway, atMost(1)).findTeamsByTheme(themeId);
    }

    @Test
    void saveActivityThemeIconLibraries_success() {
        when(themeGateway.persistActivityThemeIconLibrary(any(ActivityThemeIconLibrary.class))).thenReturn(Mono.empty());

        List<IconLibrary> iconLibraries = new ArrayList<>();
        iconLibraries.add(new IconLibrary().setName("FONTAWESOME").setStatus(IconLibraryState.SELECTED));
        iconLibraries.add(new IconLibrary().setName("MICROSOFT"));

        themeService.saveActivityThemeIconLibraries(themeId, iconLibraries);

        verify(themeGateway, atMost(2)).persistActivityThemeIconLibrary(any());
        assertEquals(IconLibraryState.NOT_SELECTED, iconLibraries.get(1).getStatus());
    }

    @Test
    void fetchIconLibrariesByActivityTheme_success() {
        when(themeGateway.fetchActivityThemeIconLibraries(activityId)).thenReturn(Flux.just(new IconLibrary()));

        List<IconLibrary> iconLibraries = new ArrayList<>();
        iconLibraries.add(new IconLibrary().setName("FONTAWESOME").setStatus(IconLibraryState.SELECTED));
        iconLibraries.add(new IconLibrary().setName("MICROSOFT"));

        IconLibrary iconLibraryByActivity = themeService.fetchActivityThemeIconLibraries(activityId).blockFirst();

        assertNotNull(iconLibraryByActivity);
        verify(themeGateway, atMost(1)).fetchActivityThemeIconLibraries(any());
    }

}
