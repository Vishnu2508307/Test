package com.smartsparrow.rtm.wiring;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.smartsparrow.dataevent.BroadcastMessage;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.MessageType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.event.EventPublisher;
import com.smartsparrow.util.Generics;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

/**
 * Allows to bind a set of RTM apis
 */
public class RTMMessageOperations {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(RTMMessageOperations.class);

    private MapBinder<String, Class<? extends MessageType>> messageTypes;
    private MapBinder<String, AuthorizationPredicate<? extends MessageType>> messageAuthorizers;
    private MapBinder<String, MessageHandler<? extends ReceivedMessage>> messageHandlers;
    private MapBinder<String, EventPublisher<? extends BroadcastMessage>> eventPublishers;

    public RTMMessageOperations(Binder binder) {
        messageTypes = MapBinder.newMapBinder(binder,  //
                new TypeLiteral<String>() {
                }, //
                new TypeLiteral<Class<? extends MessageType>>() {
                });

        // setup the message handlers as a collection implementation allowing for multiple handlers per message type.
        messageHandlers = MapBinder.newMapBinder(binder,
                new TypeLiteral<String>() {
                },
                new TypeLiteral<MessageHandler<? extends ReceivedMessage>>() {
                })
                .permitDuplicates();

        // setup the authorizer predicates as a collection implementation allowing for multiple authorizers per msg type
        messageAuthorizers = MapBinder.newMapBinder(binder,
                new TypeLiteral<String>() {
                },
                new TypeLiteral<AuthorizationPredicate<? extends MessageType>>() {
                })
                .permitDuplicates();

        // setup the event publisher as a collection implementation allowing for multiple event publishers per msg type
        eventPublishers = MapBinder.newMapBinder(binder,
                new TypeLiteral<String>() {
                },
                new TypeLiteral<EventPublisher<? extends BroadcastMessage>>() {

                })
                .permitDuplicates();
    }

    public MapBinder<String, Class<? extends MessageType>> getMessageTypes() {
        return messageTypes;
    }

    public MapBinder<String, AuthorizationPredicate<? extends MessageType>> getMessageAuthorizers() {
        return messageAuthorizers;
    }

    public MapBinder<String, MessageHandler<? extends ReceivedMessage>> getMessageHandlers() {
        return messageHandlers;
    }

    public MapBinder<String, EventPublisher<? extends BroadcastMessage>> getEventPublishers() {
        return eventPublishers;
    }

    public BinderBuilder bind(String type) {
        return new BinderBuilder(type);
    }

    public class BinderBuilder {
        private final String type;
        private Class<? extends MessageType> typeClass;

        /**
         * Create a builder for the named type
         *
         * @param type the type value
         */
        private BinderBuilder(String type) {
            this.type = type;
        }

        /**
         * Bind the message type
         *
         * @param typeClass the POJO to represent the message type
         * @return this
         */
        public final BinderBuilder toMessageType(final Class<? extends MessageType> typeClass) {
            // bind the message type
            messageTypes.addBinding(type).toInstance(typeClass);
            this.typeClass = typeClass;
            log.info("bind type {} as {}", type, typeClass.getName());
            return this;
        }

        /**
         * Bind the authorizer predicates
         *
         * @param authorizers the message authorizers to test prior to message processing
         * @return this
         */
        @SuppressWarnings("unchecked")
        @SafeVarargs
        //we can use the annotation because we are sure that there are no un-safe operations on 'authorizers' which can lead to ClassCastException
        public final BinderBuilder withAuthorizers(final Class<? extends AuthorizationPredicate<? extends MessageType>>... authorizers) throws RTMMessageBindingException {
            //
            for (Class<? extends AuthorizationPredicate<? extends MessageType>> authorizer : authorizers) {

                validateParameterizedType(getGeneric(authorizer));

                messageAuthorizers.addBinding(type).to(authorizer);
                log.info("authorizing type {} with {}", type, authorizer.getName());
            }
            return this;
        }

        /**
         * Bind the message handlers
         *
         * @param handlers the message handlers which process the provided message type
         * @return this
         */
        @SafeVarargs
        public final BinderBuilder withMessageHandlers(final Class<? extends MessageHandler<? extends ReceivedMessage>>... handlers)
                throws RTMMessageBindingException {
            //
            for (Class<? extends MessageHandler<? extends ReceivedMessage>> handler : handlers) {

                validateParameterizedType(getGeneric(handler));

                messageHandlers.addBinding(type).to(handler);
                log.info("bind handler for type {} with {}", type, handler.getName());
            }

            return this;
        }

        /**
         * Bind the event publisher to the message type
         *
         * @param publishers the publishers that will publish an event for the message type
         * @return this
         */
        @SafeVarargs
        public final BinderBuilder withEventPublishers(final Class<? extends EventPublisher<? extends BroadcastMessage>>... publishers) {
            for (Class<? extends EventPublisher<? extends BroadcastMessage>> publisher : publishers) {

                eventPublishers.addBinding(type).to(publisher);
                log.info("bind publisher for type {} with {}", type, publisher.getName());
            }

            return this;
        }

        /**
         * Check that the generic type is assignable to the bound message type
         *
         * @param generic the supplied generic {@link Class}
         * @throws RTMMessageBindingException when the generic is not assignable to the typeClass
         */
        @SuppressWarnings("unchecked")
        private void validateParameterizedType(Class generic) throws RTMMessageBindingException {
            if (!generic.isAssignableFrom(typeClass)) {
                throw new RTMMessageBindingException(typeClass.getTypeName(), generic.getTypeName());
            }
        }

        /**
         * Extract the parameterized type
         *
         * @param arg the {@link Class} containing the parameterized type
         * @return the parameterized {@link Class} type for the supplied argument
         * @throws RTMMessageBindingException when the class is not found
         */
        private <T> Class getGeneric(Class<? extends T> arg) throws RTMMessageBindingException {
            try {
                return Generics.parameterizedClassFor(arg);
            } catch (ClassNotFoundException e) {
                throw new RTMMessageBindingException("Exception while extracting parameterized type for class " + arg, e);
            }
        }
    }
}
