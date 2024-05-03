package org.kla;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/hello")
public class LogicalCouplingResource {
    @Inject
    ObjectMapper mapper;

    @Inject
    LogicalCouplingService couplingService;

    @RestClient
    private GitHubService gitHubService;

    @GET
    @Path("/owner/{owner}/repo/{repo}/commits")
    public String getCommits(@PathParam("owner") String owner, @PathParam("repo") String repo, @QueryParam("page") int page) {
        return couplingService.getCommits(owner, repo, page);
    }

    @GET
    @Path("/owner/{owner}/repo/{repo}/ref/{ref}")
    public JsonObject getCommit(@PathParam("owner") String owner, @PathParam("repo") String repo, @PathParam("ref") String ref) {
        return couplingService.getCommit(owner, repo, ref);
    }

    @GET
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/owner/{owner}/repo/{repo}/commits/mapped")
    public JsonArray getCommitsMapped(@PathParam("owner") String owner, @PathParam("repo") String repo) {
        return couplingService.getCommitsMapped(owner, repo);
    }

    @GET
    @Path("/find_coupling/{owner}/{repo}")
    public String findCoupling(@PathParam("owner") String owner, @PathParam("repo") String repo) {
        return couplingService.findCoupling(owner, repo);
    }
}