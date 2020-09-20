package com.kayyagari;

public class RevisionInfo {
    private String hash;
    private String committerName;
    private String committerEmail;
    private long time; // UTC, always
    private String message;

    public RevisionInfo() {
    }

    public String getHash() {
        return hash;
    }

    public String getShortHash() {
        return hash.substring(0, 8);
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getCommitterName() {
        return committerName;
    }

    public void setCommitterName(String committerName) {
        this.committerName = committerName;
    }

    public String getCommitterEmail() {
        return committerEmail;
    }

    public void setCommitterEmail(String committerEmail) {
        this.committerEmail = committerEmail;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
