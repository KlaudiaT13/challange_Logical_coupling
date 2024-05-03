package org.kla;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;


//@RegisterRestClient(baseUri = "http://localhost:8000/")
@RegisterRestClient(baseUri = "https://api.github.com")
//@ClientHeaderParam(name = "Authorization", value = "Bearer ")
public interface GitHubService {

    @GET
    @Produces(("application/vnd.github+json"))
    @Path("/users/{username}")
    String getUser(@PathParam("username") String username);

    @GET
    @Produces(("application/vnd.github+json"))
    @Path("/repos/{owner}/{repo}/commits")
    String getCommits(@PathParam("owner") String username, @PathParam("repo") String repo);

    @GET
    @Produces("application/vnd.github+json")
    @Path("/repos/{owner}/{repo}/commits/{ref}")
    JsonObject getCommit(@PathParam("owner") String username, @PathParam("repo") String repo, @PathParam("ref") String ref);

    @GET
    @Produces(("application/vnd.github+json"))
    @Path("/repos/{owner}/{repo}/commits")
    JsonArray getCommitsMapped(@PathParam("owner") String username, @PathParam("repo") String repo);

    //commits or contributors
    //contributors?per_page=1


}
