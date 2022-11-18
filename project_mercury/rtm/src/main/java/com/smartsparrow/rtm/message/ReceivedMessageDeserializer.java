package com.smartsparrow.rtm.message;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Provider;
import com.smartsparrow.config.service.ConfigurationService;
import com.smartsparrow.rtm.lang.InvalidMessageFormat;
import com.smartsparrow.rtm.lang.UnsupportedMessageType;

/**
 * Deserialize an incoming message to a registered message type.
 */
public class ReceivedMessageDeserializer {

    private ObjectMapper mapper;

    @Inject
    ReceivedMessageDeserializer(Map<String, Provider<Class<? extends MessageType>>> messageTypes, ConfigurationService configurationService) {
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ReceivedMessage.class, new Deserializer(messageTypes));
        mapper.registerModule(module);
        mapper.setInjectableValues(new InjectableValues.Std().addValue("configurationService", configurationService));
    }

    /**
     * Deserialize JSON to a registered type, based on the "type" field within the message.
     *
     * @param json the JSON to parse
     * @return a deserialized version of the JSON based on the "type" field.
     *
     * @throws IllegalArgumentException if the type is not registered or not field not included.
     * @throws IOException generally when the JSON is invalid or unable to be parsed.
     */
    public ReceivedMessage deserialize(String json) throws IllegalArgumentException, IOException {
        try {
            return mapper.readValue(json, ReceivedMessage.class);
        } catch(ClassCastException ex) {
            throw new UnsupportedMessageType("", null);
        }
    }

    /*
     * A custom polymorphic deserializer.
     */
    static class Deserializer extends StdDeserializer<ReceivedMessage> {

        private Map<String, Provider<Class<? extends MessageType>>> messageTypes;

        Deserializer(Map<String, Provider<Class<? extends MessageType>>> messageTypes) {
            super(ReceivedMessage.class);

            this.messageTypes = messageTypes;
        }

        /**
         * Deserialize JSON content into the value type this serializer handles.
         *
         * @param p the json parser.
         * @param ctxt the context
         * @return the deserialized object
         * @throws IOException when underlying parsing problems occur.
         * @throws UnsupportedMessageType when an attempt to deserialize a message with an invalid type (or empty)
         */
        @Override
        public ReceivedMessage deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            ObjectMapper mapper = (ObjectMapper) p.getCodec();
            ObjectNode root = mapper.readTree(p);

            // attempt to get the message id
            JsonNode replyTo = root.get("id");
            String replyToText = replyTo != null ? replyTo.asText() : null;

            // attempt to get the type field.
            JsonNode node = root.get("type");
            if(node == null) {
                throw new UnsupportedMessageType("", replyToText);
            }

            // convert to text
            String type = node.asText();
            // ensure it is a registered type
            if(!messageTypes.containsKey(type)) {
                throw new UnsupportedMessageType(type, replyToText);
            }

            // construct and return it
            Provider<Class<? extends MessageType>> classProvider = messageTypes.get(type);
            try {
                return (ReceivedMessage) mapper.convertValue(root, classProvider.get());
            } catch (IllegalArgumentException iae) {
                if (iae.getCause() instanceof InvalidFormatException) {
                    /* if received json message has a invalid format and can't be parsed,
                       throw a custom exception with message type to show a verbose message to the user */
                    throw new InvalidMessageFormat(type, ((InvalidFormatException) iae.getCause()), replyToText);
                } else {
                    //in case if some other exception was thrown
                    throw iae;
                }

            }
        }
    }
}
