package com.smartsparrow.rtm.message.handler.courseware;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.service.AccountIdentityService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.ListAccountSummaryMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class ListAccountSummaryMessageHandler implements MessageHandler<ListAccountSummaryMessage> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ListAccountSummaryMessageHandler.class);

    public static final String AUTHOR_ACCOUNT_SUMMARY_LIST = "author.account.summary.list";
    public static final String AUTHOR_ACCOUNT_SUMMARY_LIST_OK = "author.account.summary.list.ok";
    public static final String AUTHOR_ACCOUNT_SUMMARY_LIST_ERROR = "author.account.summary.list.error";

    private final AccountIdentityService accountInformationService;

    @Inject
    public ListAccountSummaryMessageHandler(final AccountIdentityService accountInformationService) {
        this.accountInformationService = accountInformationService;
    }

    @Override
    public void validate(ListAccountSummaryMessage message) throws RTMValidationException {
        affirmArgument(message.getAccountIds() != null, "missing accountIds");
        affirmArgument(!message.getAccountIds().isEmpty(), "at least 1 element in accountIds is required");
    }

    @Trace(dispatcher = true)
    @Override
    public void handle(Session session, ListAccountSummaryMessage message) throws WriteResponseException {

        accountInformationService.fetchAccountSummaryPayload(message.getAccountIds())
                .doOnEach(log.reactiveErrorThrowable("error fetching account summary information ",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("accountIds", message.getAccountIds());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .collectList()
                .subscribe(accountSummaryPayloads -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                       AUTHOR_ACCOUNT_SUMMARY_LIST_OK,
                                       message.getId());
                               basicResponseMessage.addField("accountSummaryPayloads", accountSummaryPayloads);
                               Responses.writeReactive(session, basicResponseMessage);
                           },
                           ex -> {
                               log.jsonDebug("Unable to fetch account summary information",
                                             new HashMap<String, Object>() {
                                                 {
                                                     put("message", message.toString());
                                                     put("error", ex.getStackTrace());
                                                 }
                                             });
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       AUTHOR_ACCOUNT_SUMMARY_LIST_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Unable to fetch account summary information");
                           }
                );
    }
}
