package com.smartsparrow.rtm.util;

import com.smartsparrow.util.Maps;
import com.smartsparrow.util.log.MercuryLogger;
import org.apache.commons.lang.CharEncoding;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * The static methods of this class form additional capabilities in newRelic.
 */
public class NewRelic {

    /**
     * Attaches a custom attribute (a key/value pair) to the current transaction.
     * @param transactionAttribute
     * @param id
     * @param log
     */
    public static void addCustomAttribute(String transactionAttribute, String id, MercuryLogger log){
        try {
            com.newrelic.api.agent.NewRelic.addCustomParameter(transactionAttribute,  URLDecoder.decode(id, CharEncoding.UTF_8));
        } catch (UnsupportedEncodingException e){
            log.jsonError("Failed to decode "+transactionAttribute, Maps.of(transactionAttribute, id), e);
        }
    }
}
