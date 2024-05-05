package org.kla;

import jakarta.inject.Inject;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/hello")
public class LogicalCouplingResource {
    @Inject
    LogicalCouplingService couplingService;

    @Inject
    GitHubService gitHubService;

    @GET
    @Path("/owner/{owner}/repo/{repo}/commits")
    public String getCommits(@PathParam("owner") String owner, @PathParam("repo") String repo, @QueryParam("page") int page) {
        return gitHubService.getCommits(owner, repo, page);
    }

    @GET
    @Path("/owner/{owner}/repo/{repo}/ref/{ref}")
    public JsonObject getCommit(@PathParam("owner") String owner, @PathParam("repo") String repo, @PathParam("ref") String ref) {
        return gitHubService.getCommitDetail(owner, repo, ref);
    }

    @GET
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/owner/{owner}/repo/{repo}/commits/mapped")
    public JsonArray getCommitsMapped(@PathParam("owner") String owner, @PathParam("repo") String repo, @QueryParam("page") int page) {
        return gitHubService.getCommitsMapped(owner, repo, page);
    }

    @GET
    @Path("/find_coupling/{owner}/{repo}")
    public String findCoupling(@PathParam("owner") String owner, @PathParam("repo") String repo) {
        return couplingService.findCoupling(owner, repo);
    }
}