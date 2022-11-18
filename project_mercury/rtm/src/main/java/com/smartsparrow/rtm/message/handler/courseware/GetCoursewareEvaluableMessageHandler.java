package com.smartsparrow.rtm.message.handler.courseware;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.courseware.service.WalkableService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.GetCoursewareEvaluableMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;

/**
 * This message handler has been kept to avoid breaking changes in the FE.
 * Once the backfill for BRNT-9603 is completed the evaluationMode will be accessible via the walkable.
 *
 * This message handler and all its relative tests/methods should be deleted once the FE switches to read the
 * evaluationMode from the walkable object (activity/interactive)
 */
public class GetCoursewareEvaluableMessageHandler implements MessageHandler<GetCoursewareEvaluableMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GetCoursewareEvaluableMessageHandler.class);

    public static final String AUTHOR_EVALUABLE_GET = "author.evaluable.get";
    public static final String AUTHOR_EVALUABLE_GET_OK = "author.evaluable.get.ok";
    public static final String AUTHOR_EVALUABLE_GET_ERROR = "author.evaluable.get.error";
    private final WalkableService walkableService;

    @Inject
    public GetCoursewareEvaluableMessageHandler(final WalkableService walkableService) {
        this.walkableService = walkableService;
    }

    @Override
    public void validate(final GetCoursewareEvaluableMessage message) throws RTMValidationException {
        affirmArgument(message.getElementId() != null, "missing elementId");
        affirmArgument(message.getElementType() != null, "missing elementType");
    }

    @Override
    public void handle(final Session session,
                       final GetCoursewareEvaluableMessage message) throws WriteResponseException {

        walkableService.fetchEvaluationMode(message.getElementId(), message.getElementType())
                .doOnEach(log.reactiveErrorThrowable("error while fetching courseware evaluable",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("elementId", message.getElementId());
                                                             put("elementType", message.getElementType());
                                                         }
                                                     }))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .subscribe(evaluationMode -> {
                    // use a map so we don't change the return fields that might cause breaking changes
                    Map<String, Object> evaluable = new HashMap<>();
                    evaluable.put("elementId", message.getElementId());
                    evaluable.put("elementType", message.getElementType());
                    evaluable.put("evaluationMode", evaluationMode);

                    Responses.writeReactive(session, new BasicResponseMessage(AUTHOR_EVALUABLE_GET_OK, message.getId())
                            .addField("evaluable", evaluable));
                }, ex -> {
                    log.jsonError("could not fetch evaluable", new HashMap<String, Object>() {
                        {
                            put("elementId", message.getElementId());
                            put("elementType", message.getElementType());
                        }
                    }, ex);
                    Responses.errorReactive(session, message.getId(), AUTHOR_EVALUABLE_GET_ERROR,
                                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "could not fetch evaluable");
                });

    }
}
