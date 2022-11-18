package com.smartsparrow.rtm.message.handler.courseware.theme;

import com.smartsparrow.courseware.service.ThemeService;
import com.smartsparrow.iam.payload.CollaboratorPayload;
import com.smartsparrow.iam.payload.TeamCollaboratorPayload;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.theme.ListThemeCollaboratorMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;

import com.smartsparrow.workspace.data.AccountByTheme;
import com.smartsparrow.workspace.data.TeamByTheme;
import com.smartsparrow.workspace.data.ThemePermission;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.smartsparrow.util.Warrants.affirmArgument;

public class ListThemeCollaboratorMessageHandler implements MessageHandler<ListThemeCollaboratorMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ListThemeCollaboratorMessageHandler.class);

    public  static final String AUTHOR_THEME_COLLABORATOR_SUMMARY = "author.theme.collaborator.summary";
    public static final String AUTHOR_THEME_COLLABORATOR_SUMMARY_OK = "author.theme.collaborator.summary.ok";
    public static final String AUTHOR_THEME_COLLABORATOR_SUMMARY_ERROR = "author.theme.collaborator.summary.error";

    private final ThemeService themeService;
    private final AccountService accountService;
    private final TeamService teamService;

    @Inject
    public ListThemeCollaboratorMessageHandler(final ThemeService themeService,
                                               final AccountService accountService,
                                               final TeamService teamService) {
        this.themeService = themeService;
        this.accountService = accountService;
        this.teamService = teamService;
    }

    @Override
    public void validate(ListThemeCollaboratorMessage message) throws RTMValidationException {
        affirmArgument(message.getThemeId() != null, "missing themeId");
    }

    @Override
    public void handle(Session session, ListThemeCollaboratorMessage message) throws WriteResponseException {
        Flux<AccountByTheme> accounts = themeService.fetchAccountCollaborators(message.getThemeId());
        Flux<TeamByTheme> teams = themeService.fetchTeamCollaborators(message.getThemeId());

        Flux<? extends ThemePermission> collaboratorsFlux = Flux.concat(teams, accounts);

        Mono<Long> total = collaboratorsFlux.count();

        if (message.getLimit() != null) {
            collaboratorsFlux = collaboratorsFlux.take(message.getLimit());
        }

        Mono<Map<String, List<CollaboratorPayload>>> collaborators = collaboratorsFlux
                .flatMap(collaborator -> {
                    if (collaborator instanceof TeamByTheme) {
                        return teamService.getTeamCollaboratorPayload(((TeamByTheme) collaborator).getTeamId(),
                                                                      collaborator.getPermissionLevel())
                                .doOnEach(log.reactiveErrorThrowable("error while listing team collaborators by theme"))
                                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT));
                    } else {
                        return accountService.getCollaboratorPayload(((AccountByTheme) collaborator).getAccountId(),
                                                                     collaborator.getPermissionLevel())
                                .doOnEach(log.reactiveErrorThrowable("error while listing collaborators by theme"))
                                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT));
                    }
                })
                .collect(() -> new HashMap<>(2), (map, payload) -> {
                    if (payload instanceof TeamCollaboratorPayload) {
                        map.computeIfAbsent("teams", x -> new ArrayList<>()).add(payload);
                    } else {
                        map.computeIfAbsent("accounts", x -> new ArrayList<>()).add(payload);
                    }
                });

        Mono.zip(collaborators, total).subscribe(tuple2 -> Responses.writeReactive(session,
                                                                                   new BasicResponseMessage(
                                                                                           AUTHOR_THEME_COLLABORATOR_SUMMARY_OK,
                                                                                           message.getId())
                                                                                           .addField("collaborators",
                                                                                                     tuple2.getT1())
                                                                                           .addField("total",
                                                                                                     tuple2.getT2())),
                                                 ex -> {

                                                     log.jsonDebug("error while listing collaborators for theme",
                                                                   new HashMap<String, Object>() {
                                                                       {
                                                                           put("themeId", message.getThemeId());
                                                                           put("error", ex.getStackTrace());
                                                                       }
                                                                   });
                                                     Responses.errorReactive(session,
                                                                             message.getId(),
                                                                             AUTHOR_THEME_COLLABORATOR_SUMMARY_ERROR,
                                                                             HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                                             "error while listing collaborators for theme");
                                                 });

    }
}
