package com.smartsparrow.eval.deserializer;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.smartsparrow.eval.ScenarioField;
import com.smartsparrow.eval.parser.LiteralContext;
import com.smartsparrow.eval.parser.ResolverContext;
import com.smartsparrow.eval.parser.ScopeContext;
import com.smartsparrow.eval.resolver.Resolver;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.util.Enums;

public class ResolverDeserializer extends StdDeserializer<ResolverContext> {

    public ResolverDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ResolverContext deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode root = mapper.readTree(p);
        return parseResolver(root, mapper);
    }

    /**
     * Parse a json node to a {@link Resolver} object
     *
     *
     * @param node the node to parse
     * @return a resolver object
     * @throws IOException when failing to parse
     * @throws IllegalArgumentFault when a required json entry is missing from the json node
     */
    ResolverContext parseResolver(final JsonNode node, ObjectMapper mapper) throws IOException {

        JsonNode resolverNode = node.get(ScenarioField.RESOLVER.toString());
        JsonNode typeNode = resolverNode.get(ScenarioField.TYPE.toString());

        affirmArgument(typeNode != null, "`type` node is required for Resolver");

        Resolver.Type type = Enums.of(Resolver.Type.class, typeNode.asText());

        switch (type) {
        case LITERAL:
            return mapper.readValue(resolverNode.toString(), LiteralContext.class);
        case WEB:
            throw new UnsupportedOperationException("WEB resolver currently not supported");
        case SCOPE:
            return mapper.readValue(resolverNode.toString(), ScopeContext.class);
        default:
            throw new UnsupportedOperationException(String.format("unsupported resolver type %s", type));
        }
    }


}
