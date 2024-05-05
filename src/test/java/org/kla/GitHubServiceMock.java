package org.kla;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Alternative;
import org.kla.dto.CommitData;
import org.kla.dto.CommitDetails;

import jakarta.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static io.quarkus.bootstrap.util.IoUtils.readFile;

@Alternative
@Priority(1)
@Singleton
public class GitHubServiceMock extends GitHubService {
    public List<CommitData> getCommitsList(String owner, String repo) {
        String commits = null;
        List<CommitData> commitData = null;
        try {
            commits = readFile(Path.of("commitsList1.txt"));
            commitData = mapper.readValue(commits, new TypeReference<List<CommitData>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return commitData;
    }

    List<CommitDetails> getAllCommitsList(String owner, String repo, List<String> shaCommitList) {
        String commitsDetailsFile = null;
        List<CommitDetails> commitDetails = null;
        try {
            commitsDetailsFile = readFile(Path.of("commitsDetails1.txt"));
            commitDetails = mapper.readValue(commitsDetailsFile, new TypeReference<List<CommitDetails>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return commitDetails;
    }
}
