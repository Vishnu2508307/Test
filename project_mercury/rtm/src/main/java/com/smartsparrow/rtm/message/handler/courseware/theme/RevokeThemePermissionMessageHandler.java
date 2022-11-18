package com.smartsparrow.rtm.message.handler.courseware.theme;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.service.ThemeService;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.theme.RevokeThemePermissionMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;

public class RevokeThemePermissionMessageHandler implements MessageHandler<RevokeThemePermissionMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(RevokeThemePermissionMessageHandler.class);

    public static final String THEME_PERMISSION_REVOKE = "theme.permission.revoke";
    public static final String THEME_PERMISSION_REVOKE_OK = "theme.permission.revoke.ok";
    public static final String THEME_PERMISSION_REVOKE_ERROR = "theme.permission.revoke.error";

    private final ThemeService themeService;

    @Inject
    public RevokeThemePermissionMessageHandler(ThemeService themeService) {
        this.themeService = themeService;
    }

    @Override
    public void validate(RevokeThemePermissionMessage message) {
        affirmArgument(message.getThemeId() != null, "themeId is required");

        affirmArgument(!(message.getTeamId() != null && message.getAccountId() != null),
                       "too many arguments supplied. Either accountIds or teamIds is required");

        affirmArgument(!(message.getTeamId() == null && message.getAccountId() == null),
                       "either accountIds or teamIds is required");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = THEME_PERMISSION_REVOKE)
    @Override
    public void handle(Session session, RevokeThemePermissionMessage message) throws WriteResponseException {

        Flux<Void> permissionsDeleted;

        if (message.getTeamId() != null) {
            permissionsDeleted = themeService.deleteTeamPermission(message.getTeamId(), message.getThemeId())
                    .doOnEach(log.reactiveErrorThrowable("error while deleting theme permission for a team",
                                                         throwable -> new HashMap<String, Object>() {
                                                             {
                                                                 put("themeId", message.getThemeId());
                                                                 put("teamId", message.getTeamId());
                                                             }
                                                         }))
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    .doOnEach(ReactiveTransaction.expireOnComplete())
                    .subscriberContext(ReactiveMonitoring.createContext());
        } else {
            permissionsDeleted = themeService.deleteAccountPermissions(message.getAccountId(), message.getThemeId())
                    .doOnEach(log.reactiveErrorThrowable("error while deleting theme permission for an account",
                                                         throwable -> new HashMap<String, Object>() {
                                                             {
                                                                 put("themeId", message.getThemeId());
                                                                 put("accountId", message.getAccountId());
                                                             }
                                                         }))
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    .doOnEach(ReactiveTransaction.expireOnComplete())
                    .subscriberContext(ReactiveMonitoring.createContext());
        }

        permissionsDeleted
                .subscribe(ignore -> {
                    // nothing here, never executed
                }, ex -> {
                    log.jsonDebug("error while revoking permission over a theme", new HashMap<String, Object>() {
                        {
                            put("themeId", message.getThemeId());
                            put("accountId", message.getAccountId());
                            put("teamId", message.getTeamId());
                            put("error", ex.getStackTrace());
                        }
                    });

                    Responses.errorReactive(session,
                                            message.getId(),
                                            THEME_PERMISSION_REVOKE_ERROR,
                                            HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                            "error while revoking permission over a theme");
                }, () -> Responses.writeReactive(session, new BasicResponseMessage(THEME_PERMISSION_REVOKE_OK,
                                                                                   message.getId())));
    }
}
