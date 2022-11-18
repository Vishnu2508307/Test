package com.smartsparrow.rtm.message.handler.team;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.team.UpdateTeamMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import java.util.HashMap;

public class UpdateTeamMessageHandler implements MessageHandler<UpdateTeamMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(UpdateTeamMessageHandler.class);

    public static final String IAM_TEAM_UPDATE = "iam.team.update";
    public static final String IAM_TEAM_UPDATE_OK = "iam.team.update.ok";
    public static final String IAM_TEAM_UPDATE_ERROR = "iam.team.update.error";

    private final TeamService teamService;

    @Inject
    public UpdateTeamMessageHandler(TeamService teamService) {
        this.teamService = teamService;
    }

    @Override
    public void validate(UpdateTeamMessage message) throws RTMValidationException {
        if (message.getTeamId() == null) {
            throw new RTMValidationException("missing teamId", message.getId(), IAM_TEAM_UPDATE_ERROR);
        }
        if (message.getName() != null && message.getName().isEmpty()) {
            throw new RTMValidationException("Team name can not be empty", message.getId(), IAM_TEAM_UPDATE_ERROR);
        }
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = IAM_TEAM_UPDATE)
    @Override
    public void handle(Session session, UpdateTeamMessage message) throws WriteResponseException {

        teamService.updateTeam(message.getTeamId(), message.getName(), message.getDescription(), message.getThumbnail())
                .doOnEach(log.reactiveErrorThrowable("Error occurred while updating team"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .then(teamService.findTeam(message.getTeamId()))
                .subscribe(team -> {
                    BasicResponseMessage basicResponseMessage = new BasicResponseMessage(IAM_TEAM_UPDATE_OK, message.getId());
                    basicResponseMessage.addField("team", team);
                    Responses.writeReactive(session, basicResponseMessage);
                }, ex -> {
                    log.jsonDebug("Unable to update team", new HashMap<String, Object>(){
                        {
                            put("message", message.toString());
                            put("error", ex.getStackTrace());
                        }
                    });
                    Responses.errorReactive(session, message.getId(), IAM_TEAM_UPDATE_ERROR, HttpStatus.SC_UNPROCESSABLE_ENTITY,
                            "Unable to update team");
                });
    }
}
