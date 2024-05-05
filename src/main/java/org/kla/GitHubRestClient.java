package org.kla;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;


//@RegisterRestClient(baseUri = "http://localhost:8000/")
@RegisterRestClient(baseUri = "https://api.github.com")
@ClientHeaderParam(name = "Authorization", value = "Bearer ${git_token}")
public interface GitHubRestClient {

    @GET
    @Produces(("application/vnd.github+json"))
    @Path("/users/{username}")
    String getUser(@PathParam("username") String username);

    @GET
    @Produces(("application/vnd.github+json"))
    @Path("/repos/{owner}/{repo}/commits")
    String getCommits(@PathParam("owner") String username, @PathParam("repo") String repo, @QueryParam("page") int page);

    @GET
    @Produces("application/vnd.github+json")
    @Path("/repos/{owner}/{repo}/commits/{ref}")
    JsonObject getCommitDetail(@PathParam("owner") String username, @PathParam("repo") String repo, @PathParam("ref") String ref);

    @GET
    @Produces(("application/vnd.github+json"))
    @Path("/repos/{owner}/{repo}/commits")
    JsonArray getCommitsMapped(@PathParam("owner") String username, @PathParam("repo") String repo, @QueryParam("page") int page);


    @GET
    @Produces(("application/vnd.github+json"))
    @Path("/repos/{owner}/{repo}/commits")
    String getCommitsList(@PathParam("owner") String username, @PathParam("repo") String repo, @QueryParam("page") int page);

    @GET
    @Produces("application/vnd.github+json")
    @Path("/repos/{owner}/{repo}/commits/{ref}")
    String getCommitDetailString(@PathParam("owner") String username, @PathParam("repo") String repo, @PathParam("ref") String ref);

    //commits or contributors
    //contributors?per_page=1


}
