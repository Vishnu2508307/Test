package com.smartsparrow.courseware.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.iam.data.permission.workspace.ThemePermissionByAccount;
import com.smartsparrow.iam.data.permission.workspace.ThemePermissionGateway;
import com.smartsparrow.iam.data.team.TeamAccount;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ThemePermissionServiceTest {

    @InjectMocks
    ThemePermissionService themePermissionService;
    @Mock
    TeamService teamService;
    @Mock
    ThemePermissionGateway themePermissionGateway;
    
    private static final UUID themeId = UUID.randomUUID();
    private static final UUID teamId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void findHighestPermissionLevel_success() {
        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.just(new TeamAccount()
                                                                                      .setAccountId(accountId)
                                                                                      .setTeamId(teamId)));
        when(themePermissionGateway.fetchThemePermissionByAccount(accountId, themeId)).
                thenReturn(Mono.just(new ThemePermissionByAccount()
                                             .setThemeId(themeId)
                                             .setAccountId(accountId)
                                             .setPermissionLevel(PermissionLevel.CONTRIBUTOR)));

        when(themePermissionGateway.fetchThemePermissionByTeam(teamId, themeId)).
                thenReturn(Mono.just(PermissionLevel.REVIEWER));

        PermissionLevel highestPermissionLevel = themePermissionService.findHighestPermissionLevel(accountId,
                                                                                                   themeId).block();
        assertNotNull(highestPermissionLevel);
        verify(teamService, atMost(1)).findTeamsForAccount(accountId);
        verify(themePermissionGateway, atMost(1)).fetchThemePermissionByAccount(accountId, themeId);
        verify(themePermissionGateway, atMost(1)).fetchThemePermissionByTeam(teamId, themeId);

    }
}
