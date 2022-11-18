package com.smartsparrow.dataevent.data;

import java.util.HashMap;
import java.util.function.Function;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.http.common.HttpOperationFailedException;

import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class HttpOperationFailedProcessor implements Processor {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(HttpOperationFailedProcessor.class);

    private final Function<Exchange, Object> function;

    public HttpOperationFailedProcessor(Function<Exchange, Object> function) {
        this.function = function;
    }

    @Override
    public void process(Exchange exchange) {
        HttpOperationFailedException ex = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, HttpOperationFailedException.class);

        log.error("camel http operation failed", new HashMap<String, Object>() {
            {put("error", ex.getMessage());}
            {put("body", ex.getResponseBody());}
            {put("statusCode", ex.getStatusCode());}
        });

        exchange.getIn().setBody(ex.getResponseBody());
        exchange.getOut().setBody(function.apply(exchange));
    }
}
