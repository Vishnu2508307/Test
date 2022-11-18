package com.smartsparrow.rest.resource.r;

import com.smartsparrow.workspace.service.ProjectService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Path("/alfresco")
public class AlfrescoResource {

    private final ProjectService projectService;

    @Inject
    public AlfrescoResource(final ProjectService projectService) {
        this.projectService = projectService;
    }

    @GET
    @Path("/site/{pid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProjectConfig(@PathParam("pid") String pid) {
        String config = projectService.findProjectConfig(UUID.fromString(pid)).block();
        return Response.ok(config).build();
    }
}
