package com.smartsparrow.rtm.message.handler.courseware.theme;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.service.ThemeService;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.theme.GrantThemePermissionMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;

public class GrantThemePermissionMessageHandler implements MessageHandler<GrantThemePermissionMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GrantThemePermissionMessageHandler.class);

    public static final String THEME_PERMISSION_GRANT = "theme.permission.grant";
    public static final String THEME_PERMISSION_GRANT_OK = "theme.permission.grant.ok";
    public static final String THEME_PERMISSION_GRANT_ERROR = "theme.permission.grant.error";

    private final ThemeService themeService;
    private final AccountService accountService;
    private final TeamService teamService;

    @Inject
    public GrantThemePermissionMessageHandler(ThemeService themeService,
                                              AccountService accountService,
                                              TeamService teamService) {
        this.themeService = themeService;
        this.accountService = accountService;
        this.teamService = teamService;
    }

    @Override
    public void validate(GrantThemePermissionMessage message) {

        affirmArgument(message.getThemeId() != null, "themeId is required");
        affirmArgument(message.getPermissionLevel() != null, "permissionLevel is required");

        affirmArgument(themeService.fetchThemeById(message.getThemeId()).block() != null,
                       String.format("theme %s not found", message.getThemeId()));

        affirmArgument(!(message.getTeamIds() != null && message.getAccountIds() != null),
                       "too many arguments supplied. Either accountIds or teamIds is required");

        affirmArgument(!(message.getTeamIds() == null && message.getAccountIds() == null),
                       "either accountIds or teamIds is required");

        if (message.getTeamIds() != null) {
            List<UUID> teamIds = message.getTeamIds();
            affirmArgument(!teamIds.isEmpty(), "at least 1 element in teamIds is required");

            teamIds.forEach(teamId -> {
                affirmArgument(teamService.findTeam(teamId).block() != null,
                               String.format("team %s not found", teamId));
            });
        }

        if (message.getAccountIds() != null) {
            List<UUID> accountIds = message.getAccountIds();
            affirmArgument(!accountIds.isEmpty(), "at least 1 element in accountIds is required");

            accountIds.forEach(accountId -> {
                affirmArgument(accountService.findById(accountId)
                                       .blockLast() != null, String.format("account %s not found", accountId));
            });
        }
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = THEME_PERMISSION_GRANT)
    @Override
    public void handle(Session session, GrantThemePermissionMessage message) throws WriteResponseException {

        Flux<Void> savePermission;
        String field;

        final PermissionLevel permissionLevel = message.getPermissionLevel();
        final UUID themeId = message.getThemeId();

        Map<String, List<UUID>> fields = new HashMap<String, List<UUID>>() {
            {
                put("accountIds", message.getAccountIds());
                put("teamIds", message.getTeamIds());
            }
        };

        if (message.getTeamIds() != null) {
            field = "teamIds";
            savePermission = message.getTeamIds().stream()
                    .map(teamId -> themeService.saveTeamPermission(teamId, themeId, permissionLevel)
                            .doOnEach(log.reactiveErrorThrowable("Error while saving team permission for a theme"))
                            .doOnEach(ReactiveTransaction.linkOnNext())
                            .doOnEach(ReactiveTransaction.expireOnComplete())
                            .subscriberContext(ReactiveMonitoring.createContext()))
                    .reduce((prev, next) -> Flux.merge(prev, next))
                    .orElse(Flux.empty());
        } else {
            field = "accountIds";
            savePermission = message.getAccountIds().stream()
                    .map(accountId -> themeService.saveAccountPermissions(accountId, themeId, permissionLevel)
                            .doOnEach(log.reactiveErrorThrowable("Error while saving account permission for a theme"))
                            .doOnEach(ReactiveTransaction.linkOnNext())
                            .doOnEach(ReactiveTransaction.expireOnComplete())
                            .subscriberContext(ReactiveMonitoring.createContext()))
                    .reduce((prev, next) -> Flux.merge(prev, next))
                    .orElse(Flux.empty());
        }

        savePermission
                .subscribe(success -> {
                               // do nothing here, never executed
                           },
                           ex -> {

                               log.jsonError("error granting permission over a theme {}",
                                             new HashMap<String, Object>() {
                                                 {
                                                     put("themeId", message.getThemeId());
                                                     put("accountIds", message.getAccountIds());
                                                     put("teamIds", message.getTeamIds());
                                                     put("error", ex.getStackTrace());
                                                 }
                                             },
                                             ex);
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       THEME_PERMISSION_GRANT_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "error granting permission over a theme");
                           },
                           () -> Responses.writeReactive(session,
                                                         new BasicResponseMessage(THEME_PERMISSION_GRANT_OK,
                                                                                  message.getId())
                                                                 .addField(field, fields.get(field))
                                                                 .addField("themeId", message.getThemeId())
                                                                 .addField("permissionLevel",
                                                                           message.getPermissionLevel())));

    }
}
