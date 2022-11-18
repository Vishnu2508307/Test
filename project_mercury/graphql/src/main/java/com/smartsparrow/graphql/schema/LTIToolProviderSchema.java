package com.smartsparrow.graphql.schema;

import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;
import static com.smartsparrow.util.Warrants.affirmValidUri;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.graphql.type.LTILaunchRequestParam;
import com.smartsparrow.graphql.type.LTISignedLaunch;
import com.smartsparrow.graphql.type.LTIToolProviderContext;
import com.smartsparrow.plugin.data.LTIProviderCredential;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.sso.lang.OAuthHandlerException;
import com.smartsparrow.sso.service.LTIMessageSignatures;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import net.oauth.OAuthMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class LTIToolProviderSchema {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(LTIToolProviderSchema.class);

    private static final String METHOD = "POST";

    private final PluginService pluginService;

    @Inject
    public LTIToolProviderSchema(final PluginService pluginService) {
        this.pluginService = pluginService;
    }

    @GraphQLQuery(name = "signLaunch", description = "sign a launch to a tool provider")
    public CompletableFuture<LTISignedLaunch> signLaunch(@GraphQLContext LTIToolProviderContext context,
                                                         //
                                                         @GraphQLNonNull
                                                         @GraphQLArgument(name = "key",
                                                                 description = "the tool provider key") //
                                                         final String key,
                                                         //
                                                         @GraphQLNonNull @GraphQLArgument(name = "url",
                                                                 description = "the tool provider url") //
                                                         final String url,
                                                         //
                                                         @GraphQLNonNull
                                                         @GraphQLArgument(name = "params",
                                                                 description = "the lti launch request parameters") //
                                                         final List<LTILaunchRequestParam> suppliedParams) {
        //
        affirmValidUri(url, "invalid url");
        affirmArgument(url.startsWith("http"), "invalid url");
        affirmArgumentNotNullOrEmpty(key, "key is required");

        UUID pluginId = context.getLtiContext().getPluginSummary().getId();
        Mono<LTIProviderCredential> ltiProviderCredentialMono = pluginService
                .findLTIProviderCredential(pluginId, key)
                .single()
                .doOnError(throwable -> {
                    throw new IllegalArgumentFault("invalid request");
                });

        Mono<Map<String, String>> cleanedRequestParamsMapMono = Mono.just(suppliedParams)
                .zipWith(ltiProviderCredentialMono)
                .map(tuple2 -> {
                    List<LTILaunchRequestParam> requestParams = tuple2.getT1();
                    LTIProviderCredential credential = tuple2.getT2();

                    return requestParams
                            .stream()
                            .filter(ltiLaunchRequestParam -> credential.getAllowedFields().contains(
                                    ltiLaunchRequestParam.getName()))
                            .collect(Collectors.toList());
                })
                .flatMapMany(Flux::fromIterable)
                .collectMap(LTILaunchRequestParam::getName, LTILaunchRequestParam::getValue);

        return cleanedRequestParamsMapMono
                .zipWith(ltiProviderCredentialMono)
                .map(tuple2 -> {
                    Map<String, String> paramsMap = tuple2.getT1();
                    LTIProviderCredential credential = tuple2.getT2();
                    try {
                        Map<String, String> signedParameters = LTIMessageSignatures
                                .sign(new OAuthMessage(METHOD,
                                                       url,
                                                       paramsMap.entrySet()),
                                      credential.getKey(),
                                      credential.getSecret());

                        // build the launch response.
                        return new LTISignedLaunch() //
                                .setMethod(METHOD)
                                .setUrl(url)
                                .setFormParameters(signedParameters);

                    } catch (OAuthHandlerException e) {
                        throw new IllegalStateFault("could not create signature");
                    }
                })
                .toFuture();
    }
}
