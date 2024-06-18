package com.kayyagari;

/*
   Copyright [2024] [Kiran Ayyagari]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

/**
 * @author Kiran Ayyagari (kayyagari@apache.org)
 */
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
