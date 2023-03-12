package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import java.util.TreeMap;
import java.util.Objects;
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

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
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
    public boolean put(String name) {
        File file = join(Repository.CWD, name);
        String blobId = get(name);
        if (Objects.isNull(blobId)) {
            return false;
        }
        Utils.writeContents(file, Blob.convertBlobToObj(blobId, String.class));
        return true;
    }

    public void putAll() {
        Set<String> branchFiles = commitFile.keySet();
        for (String name : branchFiles) {
            put(name);
        }
    }

}
