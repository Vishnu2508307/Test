package com.smartsparrow.rest.resource.r;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.io.InputStream;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.smartsparrow.exception.BadRequestException;
import com.smartsparrow.exception.NotAuthorizedException;
import com.smartsparrow.exception.UnprocessableEntityException;
import com.smartsparrow.iam.service.DeveloperKey;
import com.smartsparrow.iam.service.DeveloperKeyService;
import com.smartsparrow.plugin.lang.PluginPublishException;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.plugin.service.PublishedPlugin;

@Path("/plugin")
public class PluginResource {

    private static final Logger log = LoggerFactory.getLogger(PluginResource.class);

    private final PluginService pluginService;
    private final DeveloperKeyService developerKeyService;

    @Inject
    public PluginResource(PluginService pluginService,
                          DeveloperKeyService developerKeyService) {
        this.pluginService = pluginService;
        this.developerKeyService = developerKeyService;
    }

    @POST
    @Path("/publish")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response publish(@FormDataParam("file") InputStream inputStream,
                            @FormDataParam("file") FormDataContentDisposition contentDisposition,
                            @FormDataParam("devKey") String devKey,
                            @FormDataParam("pluginId") UUID pluginId)
            throws BadRequestException, UnprocessableEntityException, NotAuthorizedException {

        affirmArgument(inputStream != null, "Multipart file data required");
        affirmArgument(contentDisposition != null, "Multipart field Content-Disposition required");
        affirmArgument(!Strings.isNullOrEmpty(devKey), "Developer key required");

        DeveloperKey developerKey = developerKeyService.fetchByValue(devKey).block();
        if (developerKey == null) {
            throw new NotAuthorizedException("Invalid developer key supplied");
        }

        PublishedPlugin plugin;
        try {
            plugin = pluginService.publish(inputStream, contentDisposition.getFileName(), developerKey.getAccountId(), pluginId);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        } catch (PluginPublishException e) {
            log.error(e.getMessage(), e);
            throw new UnprocessableEntityException(e.getMessage());
        }

        return Response.status(HttpStatus.SC_CREATED).entity(plugin).build();
    }
}
