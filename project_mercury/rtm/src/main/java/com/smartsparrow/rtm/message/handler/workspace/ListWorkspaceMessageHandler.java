package com.smartsparrow.rtm.message.handler.workspace;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.util.NewRelic;
import com.smartsparrow.rtm.util.NewRelicTransactionAttributes;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.EmptyReceivedMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import com.smartsparrow.workspace.service.WorkspaceService;

import java.util.HashMap;

public class ListWorkspaceMessageHandler implements MessageHandler<EmptyReceivedMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ListWorkspaceMessageHandler.class);

    public static final String WORKSPACE_LIST = "workspace.list";
    private static final String WORKSPACE_LIST_OK = "workspace.list.ok";
    private static final String WORKSPACE_LIST_ERROR = "workspace.list.error";

    private final WorkspaceService workspaceService;
    private final Provider<AuthenticationContext> authenticationContextProvider;

    @Inject
    public ListWorkspaceMessageHandler(WorkspaceService workspaceService,
                                       Provider<AuthenticationContext> authenticationContextProvider) {
        this.workspaceService = workspaceService;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = WORKSPACE_LIST)
    @Override
    public void handle(Session session, EmptyReceivedMessage message) throws WriteResponseException {
        final Account account = authenticationContextProvider.get().getAccount();
        NewRelic.addCustomAttribute(NewRelicTransactionAttributes.ACCOUNT_ID.getValue(), account.getId().toString(), log);

        workspaceService.fetchWorkspaces(account.getId())
                .doOnEach(log.reactiveErrorThrowable("error while listing workspaces", throwable -> new HashMap<String, Object>() {
                    {
                        put("accountId", account.getId());
                    }
                }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .collectList()
                .subscribe(workspaces -> {
                    Responses.writeReactive(session, new BasicResponseMessage(WORKSPACE_LIST_OK, message.getId())
                    .addField("workspaces", workspaces));
                }, ex->{
                    log.debug("error while listing collaborators for plugin ", new HashMap<String, Object>() {
                        {
                            put("id", message.getId());
                            put("error", ex.getStackTrace());
                        }
                    });
                    Responses.errorReactive(session, message.getId(), WORKSPACE_LIST_ERROR,
                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "error while listing workspaces");
                });
    }
}
