package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  does at a high level.
 */
public class Commit implements Serializable {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;

    /** The timestamp of this Commit. */
    private Date timeStamp;

    /** The parent commit of this Commit. */
    private String parentId;

    private TreeMap<String, String> commitFile;

    public String getCommitId() {
        return sha1(serialize(this));
    }
    public Commit(String message, Date timestamp) {
        this.message = message;
        this.timeStamp = timestamp;
        Commit headCommit = Projects.getHeadCommit();
        if (Objects.isNull(headCommit)) {
            this.commitFile = new TreeMap<>();
            this.parentId = null;
        } else {
            this.commitFile = headCommit.commitFile;
            this.parentId = sha1(serialize(headCommit));
        }
    }

    public String getMessage() {
        return message;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public String getParentId() {
        return parentId;
    }

    public String get(String name) {
        return commitFile.get(name);
    }

    public Set<String> getAll() {
        return commitFile.keySet();
    }

    public void add(String fileName, String blobId) {
        this.commitFile.put(fileName, blobId);
    }

    public void remove(String fileName) {
        this.commitFile.remove(fileName);
    }

    /** put given tracked file in commit to CWD directory */
    public boolean put(String name) {
        File file = join(Repository.CWD, name);
        String blobId = get(name);
        if (Objects.isNull(blobId)) {
            return false;
        }
        Utils.writeContents(file, Blob.content(blobId));
        return true;
    }

    /** put all tracked files in commit to CWD directory */
    public void putAll() {
        Set<String> branchFiles = commitFile.keySet();
        for (String name : branchFiles) {
            put(name);
        }
    }

    public String save() {
        String commitId = this.getCommitId();
        File commitFile = join(Repository.COMMITS_DIR, commitId);
        Utils.writeObject(commitFile, this);
        return commitId;
    }
    public static Commit acquire(String commitId) {
        if (Objects.isNull(commitId)) {
            return null;
        }
        File commitFile = join(Repository.COMMITS_DIR, commitId);
        if (!commitFile.exists()) {
            return null;
        }
        return Utils.readObject(commitFile, Commit.class);
    }

    public static String findCommId(String prefix) {
        List<String> commitList = Utils.plainFilenamesIn(Repository.COMMITS_DIR);
        for (String commitId : commitList) {
            if (commitId.startsWith(prefix)) {
                return commitId;
            }
        }
        return null;
    }

}
