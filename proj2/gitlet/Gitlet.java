package gitlet;

import java.io.File;
import java.util.*;

import static gitlet.Utils.*;

class Gitlet {

    static Commit headCommit;

    static HashMap<String, String> staging;

    static HashMap<String, String> remoteRepository;

    static String currentBranch;

    static Boolean[] cachedArray;

    static final int COMMIT_INDEX = 0;
    static final int STAGING_INDEX = 1;
    static final int BRANCH_INDEX = 2;

    static final int REMOTE_INDEX = 3;

    static final int CACHESIZE = 4;

    static final String STAGED_REMOVAL = "00";

    static {
        cachedArray = new Boolean[CACHESIZE];
        Arrays.fill(cachedArray, false);
    }

    static boolean isCached(int index) {
        return cachedArray[index];
    }

    static String getCurrentBranch() {
        if (!isCached(BRANCH_INDEX)) {
            File head = Repository.HEAD;
            if (!head.exists()) {
                return null;
            }
            String name = Utils.readContentsAsString(head);
            currentBranch = name;
            cachedArray[BRANCH_INDEX] = true;
        }
        return currentBranch;
    }

    static void updateBranch(String branchName) {
        File head = Repository.HEAD;
        Utils.writeContents(head, branchName);
        cachedArray[BRANCH_INDEX] = false;
    }

    public static Commit getHeadCommit() {
        if (!isCached(COMMIT_INDEX)) {
            String branchName = getCurrentBranch();
            if (Objects.isNull(branchName)) {
                return null;
            }
            File branch = getBranch(branchName);
            if (!branch.exists()) {
                return null;
            }
            Commit commit = getBranchCommit(branch);
            headCommit = commit;
            cachedArray[COMMIT_INDEX] = true;
        }
        return headCommit;
    }

    static void updateHeadCommit(String commitId) {
        String branchName = getCurrentBranch();
        File branch = getBranch(branchName);
        Utils.writeContents(branch, commitId);
        cachedArray[COMMIT_INDEX] = false;
    }

    static HashMap<String, String> getStagingArea() {
        if (!isCached(STAGING_INDEX)) {
            File index = Repository.INDEX;
            if (!index.exists()) {
                return null;
            }
            HashMap<String, String> map = Utils.readObject(index, HashMap.class);
            staging = map;
            cachedArray[STAGING_INDEX] = true;
        }
        return staging;
    }

    static void updateStagingArea(HashMap<String, String> stagingArea) {
        Utils.writeObject(Repository.INDEX, stagingArea);
        cachedArray[STAGING_INDEX] = false;
    }

    static HashMap<String, String> getRemoteInformation() {
        if (!isCached(REMOTE_INDEX)) {
            File remote = Repository.REMOTE;
            if (!remote.exists()) {
                return new HashMap<>();
            }
            HashMap<String, String> repository = Utils.readObject(remote, HashMap.class);
            remoteRepository = repository;
            cachedArray[REMOTE_INDEX] = true;
        }
        return remoteRepository;
    }

    static void updateRemoteInformation(HashMap<String, String> repository) {
        Utils.writeObject(Repository.REMOTE, repository);
        cachedArray[REMOTE_INDEX] = false;
    }

    static Set<String> getTrackedFiles() {
        Set<String> trackedFiles = new TreeSet<>();
        trackedFiles.addAll(getStagingArea().keySet());
        trackedFiles.addAll(getHeadCommit().getAll());
        return trackedFiles;
    }

    static Set<String> getUntrackedFiles() {
        Set<String> untrackedFiles = new TreeSet<>();
        untrackedFiles.addAll(plainFilenamesIn(Repository.CWD));
        untrackedFiles.removeAll(getTrackedFiles());
        return untrackedFiles;
    }

    static File getBranch(String branchName) {
        return branchName.contains("/") ?
                join(Repository.REMOTES, branchName) : join(Repository.LOCAL, branchName);
    }

    static Commit getBranchCommit(File branch) {
        return Commit.acquire(Utils.readContentsAsString(branch));
    }

    static void fetchSnapShots(Commit commit, HashSet<Commit> fetchCommits,
                               HashMap<String, String> fetchBlobs) {
        fetchCommits.add(commit);
        for (String fileName : commit.getAll()) {
            String blobId = commit.get(fileName);
            fetchBlobs.put(blobId, Blob.content(blobId));
        }
    }

    static void pushSnapShots(HashSet<Commit> fetchCommits, HashMap<String, String> fetchBlobs) {
        for (Commit remoteCommit : fetchCommits) {
            String commitId = remoteCommit.getCommitId();
            File commitFile = join(Repository.COMMITS_DIR, commitId);
            if (!commitFile.exists()) {
                writeObject(commitFile, remoteCommit);
            }
        }
        for (String blobId : fetchBlobs.keySet()) {
            Blob.blob(blobId, fetchBlobs.get(blobId));
        }
    }

    static String makeConflictMessage(String headCommitId, String mergeCommitId) {
        MessageBuilder conflictMessage = new MessageBuilder();
        String headContent = Objects.isNull(headCommitId) ? "" : Blob.content(headCommitId);
        String mergeContent = Objects.isNull(mergeCommitId) ? "" : Blob.content(mergeCommitId);
        conflictMessage.append("<<<<<<< HEAD");
        conflictMessage.append(headContent);
        conflictMessage.append("=======");
        conflictMessage.append(mergeContent);
        conflictMessage.appendRaw(">>>>>>>");
        return Blob.blob(conflictMessage.toString());
    }

    public static void makeCommitMessage(MessageBuilder builder, Commit commit) {
        String commId = commit.getCommitId();
        String mergedId = commit.getMergedParentId();
        builder.append("===");
        builder.append("commit " + commId);
        if (!Objects.isNull(mergedId)) {
            builder.append("Merge: " + commId.substring(0, 7)
                    + " " + mergedId.substring(0, 7));
        }
        builder.append("Date: " + String.format(Locale.ENGLISH,
                "%1$ta %1$tb %1$te %1$tH:%1$tM:%1$tS %1$tY %1$tz",
                commit.getTimeStamp()));
        builder.append(commit.getMessage());
        builder.appendRaw(System.getProperty("line.separator"));
    }

}
