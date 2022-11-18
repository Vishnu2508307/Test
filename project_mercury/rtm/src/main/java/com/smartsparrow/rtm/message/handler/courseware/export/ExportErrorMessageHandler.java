package com.smartsparrow.rtm.message.handler.courseware.export;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.export.data.AmbrosiaReducerErrorLog;
import com.smartsparrow.export.data.ExportErrorPayload;
import com.smartsparrow.export.service.ExportService;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.export.ExportGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ExportErrorMessageHandler implements MessageHandler<ExportGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ExportErrorMessageHandler.class);

    public static final String AUTHOR_ELEMENT_EXPORT_ERROR_RESULT = "author.export.error.result";
    public static final String AUTHOR_ELEMENT_EXPORT_ERROR_RESULT_OK = "author.export.error.result.ok";
    public static final String AUTHOR_ELEMENT_EXPORT_ERROR_RESULT_ERROR = "author.export.error.result.error";

    private final ExportService exportService;
    private final Provider<AuthenticationContext> authenticationContextProvider;

    @Inject
    public ExportErrorMessageHandler(final ExportService exportService,
                                     final Provider<AuthenticationContext> authenticationContextProvider) {
        this.exportService = exportService;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    @Override
    public void validate(ExportGenericMessage message) throws RTMValidationException {
        affirmArgument(message.getExportId() != null, "missing exportId");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_ELEMENT_EXPORT_ERROR_RESULT)
    @Override
    public void handle(Session session, ExportGenericMessage message) throws WriteResponseException {

        Mono<List<ExportErrorPayload>> exportErrors = exportService.getExportErrors(
                message.getExportId()).collectList();
        Mono<List<AmbrosiaReducerErrorLog>> ExportReducerErrors = exportService.getAmbrosiaReducerErrors(
                message.getExportId()).collectList();

        Flux.zip(exportErrors, ExportReducerErrors)
                        .flatMap(tupple2 -> {
                            return Flux.just(new BasicResponseMessage(AUTHOR_ELEMENT_EXPORT_ERROR_RESULT_OK, message.getId())
                                    .addField("exportAmbrosiaErrorList", tupple2.getT1())
                                    .addField("exportAmbrosiaReducerErrorList", tupple2.getT2()));
                        })
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .subscribe(basicResponseMessage -> {
                               Responses.writeReactive(session, basicResponseMessage);
                           },
                           ex -> {
                               log.debug("Unable to list export errors", new HashMap<String, Object>() {
                                   {
                                       put("message", message.toString());
                                       put("error", ex.getStackTrace());
                                   }
                               });
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       AUTHOR_ELEMENT_EXPORT_ERROR_RESULT_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Unable to list export errors");
                           }
                );

    }
}
