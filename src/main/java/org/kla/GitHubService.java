package org.kla;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.bootstrap.util.IoUtils;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.kla.dto.CommitData;
import org.kla.dto.CommitDetails;
import org.kla.dto.File;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class GitHubService {

    public static final int PAGE_LIMIT = 50;
    @RestClient
    private GitHubRestClient restClient;

    @Inject
    ObjectMapper mapper;

    public String getCommits(String owner, String repo, int page) {
        return restClient.getCommits(owner, repo, page);
    }

    public JsonObject getCommitDetail(String owner, String repo, String ref) {
        return restClient.getCommitDetail(owner, repo, ref);
    }

    public JsonArray getCommitsMapped(String owner, String repo, int page) {
        return restClient.getCommitsMapped(owner, repo, page);
    }

    public List<CommitData> getCommitsList(String owner, String repo) {
        int page = 1;
        List<CommitData> commitDataList = new ArrayList<>();

        String commits = restClient.getCommits(owner, repo, 1);
        List<CommitData> commitsMapped = mapCommit(commits);
        while (!commitsMapped.isEmpty() && page < PAGE_LIMIT) {
            commitDataList.addAll(commitsMapped);
            commitsMapped = mapCommit(restClient.getCommits(owner, repo, ++page));
        }
//        saveFile(commitDataList);
        return commitDataList;
    }

    private List<CommitData> mapCommit(String commits) {
        try {
            return mapper.readValue(commits, new TypeReference<List<CommitData>>() {});
        } catch (JsonProcessingException e) {
            Log.error("Can't parse commit string");
        }
        return Collections.emptyList();
    }

    private void saveFile(List<CommitData> allCommits) {
        try {
            IoUtils.writeFile(Path.of("commitsList3.txt"), mapper.writeValueAsString(allCommits));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getContributorsList(List<CommitData> commitDataList) {
        return commitDataList.stream()
                .map(CommitData::getAuthorName)
                .distinct()
                .toList();
    }

    public List<String> getShaOfCommitsList(List<CommitData> commitDataList) {
        return commitDataList.stream()
                .map(CommitData::getSha)
                .distinct()
                .toList();
    }

    List<CommitDetails> getAllCommitsList(String owner, String repo, List<String> shaCommitList) {
        try (ExecutorService executor = Executors.newFixedThreadPool(10)) {
            List<CommitDetails> list = shaCommitList.stream()
                    .map(c -> CompletableFuture.supplyAsync(() -> {
                        Log.info("Get commits detail: " + c + "Time: " + LocalTime.now());
                        return restClient.getCommitDetailString(owner, repo, c);
                    }, executor))
                    .map(CompletableFuture::join)
                    .map(this::mapCommitDetails)
                    .toList();
//            saveCommitDetails(list);
            return list;
        }
    }
    private CommitDetails mapCommitDetails(String commits) {
        try {
            return mapper.readValue(commits, CommitDetails.class);
        } catch (JsonProcessingException e) {
            Log.error("Can't parse commit string");
        }
        return null;
    }

    private void saveCommitDetails(List<CommitDetails> allCommits) {
        try {
            IoUtils.writeFile(Path.of("commitsDetails3.txt"), mapper.writeValueAsString(allCommits));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String[] getFilenameFromCommits(List<CommitDetails> allCommits) {
        return allCommits.stream()
                .flatMap(c -> c.getFiles().stream())
                .map(File::getFilename)
                .distinct()
                .toArray(String[]::new);
    }

    public String[][] getArrayOfShaOfFilesAndNumberOfChangesFromCommit(CommitDetails commit) {
        ArrayList<File> files = commit.getFiles();

        String[][] filesAndChanges = new String[files.size()][2];
        for (int i = 0; i < files.size(); i++) {
            filesAndChanges[i][0] = files.get(i).getFilename();
            filesAndChanges[i][1] = files.get(i).getChanges().toString();
        }
        return filesAndChanges;
    }
}
