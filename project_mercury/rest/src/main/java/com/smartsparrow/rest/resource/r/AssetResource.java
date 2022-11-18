package com.smartsparrow.rest.resource.r;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.data.AssetTemplate;
import com.smartsparrow.asset.data.AssetVisibility;
import com.smartsparrow.asset.lang.AssetUploadException;
import com.smartsparrow.asset.lang.InvalidAssetUploadException;
import com.smartsparrow.asset.service.AssetUploadService;
import com.smartsparrow.exception.BadRequestException;
import com.smartsparrow.exception.InternalServerError;
import com.smartsparrow.exception.UnprocessableEntityException;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

@Path("/asset")
public class AssetResource {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AssetResource.class);

    private Provider<AuthenticationContext> authenticationContextProvider;
    private final AssetUploadService assetUploadService;

    @Inject
    public AssetResource(Provider<AuthenticationContext> authenticationContextProvider,
                         AssetUploadService assetUploadService) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.assetUploadService = assetUploadService;
    }

    @GET
    @Path("/test")
    @Produces(MediaType.APPLICATION_JSON)
    public Response test() {
        AuthenticationContext context = authenticationContextProvider.get();
        return Response.ok(context.getAccount()).build();
    }

    @POST
    @Path("/save")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response save(@FormDataParam("file") InputStream fileInputStream,
                         @FormDataParam("file") FormDataContentDisposition cdh,
                         @FormDataParam("visibility") AssetVisibility visibility,
                         @FormDataParam("subscriptionId") UUID subscriptionId,
                         @FormDataParam("metadata") Map<String, String> metadata)
            throws BadRequestException, UnprocessableEntityException {

        try {
            checkArgument(fileInputStream != null && cdh != null, "File is required");
            checkArgument(visibility != null, "asset visibility param is required");
            checkArgument(subscriptionId != null, "subscriptionId is required");
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }

        Account account = authenticationContextProvider.get().getAccount();

        AssetTemplate assetTemplate = new AssetTemplate(cdh.getFileName())
                .setInputStream(fileInputStream)
                .setProvider(AssetProvider.AERO)
                .setVisibility(visibility)
                .setOwnerId(account.getId())
                .setSubscriptionId(subscriptionId)
                .setMetadata(metadata);

        return assetUploadService.save(assetTemplate)
                .doOnError(throwable -> {
                    if (throwable instanceof InvalidAssetUploadException) {
                        throw new BadRequestException(throwable.getMessage());
                    } else if (throwable instanceof  AssetUploadException) {
                        throw new UnprocessableEntityException(throwable.getMessage());
                    } else {
                        log.jsonError("error while uploading asset", new HashMap<String, Object>(){{put("throwable",throwable);}}, throwable);
                        throw new InternalServerError();
                    }
                })
                .map(assetPayload-> Response.ok(assetPayload).build())
                .block();
    }

}
