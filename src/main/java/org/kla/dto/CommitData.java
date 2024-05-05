package org.kla.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CommitData {

    private String sha;
    private Commit commit;

    public String getSha() {
        return sha;
    }

    @JsonIgnore
    public String getAuthorName() {
        return commit.getAuthor().getName();
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public Commit getCommit() {
        return commit;
    }

    public void setCommit(Commit commit) {
        this.commit = commit;
    }
}
