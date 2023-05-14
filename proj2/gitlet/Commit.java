package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Utils.*;
import static gitlet.Gitlet.*;

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


    /** The merged parent commit of this Commit. */
    private String mergedParentId;

    private TreeMap<String, String> commitFiles;

    public String getCommitId() {
        return sha1(serialize(this));
    }
    public Commit(String message, Date timestamp) {
        this.message = message;
        this.timeStamp = timestamp;
        Commit headCommit = getHeadCommit();
        if (Objects.isNull(headCommit)) {
            this.commitFiles = new TreeMap<>();
            this.parentId = null;
        } else {
            this.commitFiles = headCommit.commitFiles;
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

    public String getMergedParentId() {
        return mergedParentId;
    }
    public void setMergedParentId(String mergedParentId) {
        this.mergedParentId = mergedParentId;
    }

    public boolean contains(String name) {
        return commitFiles.containsKey(name);
    }

    public String get(String name) {
        return commitFiles.get(name);
    }

    public Set<String> getAll() {
        return commitFiles.keySet();
    }

    public void add(String fileName, String blobId) {
        this.commitFiles.put(fileName, blobId);
    }

    public void remove(String fileName) {
        this.commitFiles.remove(fileName);
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
        Set<String> branchFiles = commitFiles.keySet();
        for (String name : branchFiles) {
            put(name);
        }
    }

    public String save() {
        String commitId = this.getCommitId();
        File commFile = join(Repository.COMMITS_DIR, commitId);
        Utils.writeObject(commFile, this);
        return commitId;
    }

    public String findSplitPoint(Commit commit) {
        Queue<Commit> queue = new ArrayDeque<>();
        Set<String> markedSet = new HashSet<>();
        queue.add(this);
        queue.add(commit);
        while (!queue.isEmpty()) {
            Commit currentCommit = queue.poll();
            if (!Objects.isNull(currentCommit)) {
                String currentId = currentCommit.getCommitId();
                if (markedSet.contains(currentId)) {
                    return currentId;
                } else {
                    markedSet.add(currentId);
                }
                if (!Objects.isNull(currentCommit.parentId)) {
                    queue.add(Commit.acquire(currentCommit.parentId));
                }
                if (!Objects.isNull(currentCommit.mergedParentId)) {
                    queue.add(Commit.acquire(currentCommit.mergedParentId));
                }
            }
        }
        return null;
    }


    public static Commit acquire(String commitId) {
        if (Objects.isNull(commitId)) {
            return null;
        }
        File commFile = join(Repository.COMMITS_DIR, commitId);
        if (!commFile.exists()) {
            return null;
        }
        return Utils.readObject(commFile, Commit.class);
    }

    public static String findCommId(String prefix) {
        List<String> commitList = Utils.plainFilenamesIn(Repository.COMMITS_DIR);
        if (!Objects.isNull(commitList)) {
            for (String commitId : commitList) {
                if (commitId.startsWith(prefix)) {
                    return commitId;
                }
            }
        }
        return null;
    }

}
