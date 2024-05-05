package org.kla.dto;

import com.fasterxml.jackson.annotation.JsonSetter;

public class File {
    private String filename;
    private Integer additions;
    private Integer deletions;
    private Integer changes;
    private String rawUrl;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Integer getAdditions() {
        return additions;
    }

    public void setAdditions(Integer additions) {
        this.additions = additions;
    }

    public Integer getDeletions() {
        return deletions;
    }

    public void setDeletions(Integer deletions) {
        this.deletions = deletions;
    }

    public Integer getChanges() {
        return changes;
    }

    public void setChanges(Integer changes) {
        this.changes = changes;
    }

    public String getRawUrl() {
        return rawUrl;
    }

    @JsonSetter("raw_url")
    public void setRawUrl(String rawUrl) {
        this.rawUrl = rawUrl;
    }
}
