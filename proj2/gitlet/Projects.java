package gitlet;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import static gitlet.Utils.join;

class Projects {

    static Commit headCommit;

    static HashMap<String, String> staging;

    static String currentBranch;

    static Boolean[] cachedArray;

    static final int COMMIT_INDEX = 0;
    static final int STAGING_INDEX = 1;
    static final int BRANCH_INDEX = 2;

    static final int CACHESIZE = 3;

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
            File branch = join(Repository.LOCAL, branchName);
            if (!branch.exists()) {
                return null;
            }
            String headCommitId = Utils.readContentsAsString(branch);
            Commit commit = Commit.acquire(headCommitId);
            headCommit = commit;
            cachedArray[COMMIT_INDEX] = true;
        }
        return headCommit;
    }

    static void updateHeadCommit(String commitId) {
        String branchName = getCurrentBranch();
        File branch = Utils.join(Repository.LOCAL, branchName);
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
        File index = Repository.INDEX;
        Utils.writeObject(index, stagingArea);
        cachedArray[STAGING_INDEX] = false;
    }

    static String makeConflictFile(String fileName, String headCommitId, String mergeCommitId) {
        MessageBuilder conflictMessage = new MessageBuilder();
        String headContent = Objects.isNull(headCommitId) ? null : Blob.content(headCommitId);
        String mergeContent = Objects.isNull(mergeCommitId) ? null : Blob.content(mergeCommitId);
        conflictMessage.append("<<<<<<< HEAD");
        conflictMessage.append(headContent);
        conflictMessage.append("=======");
        conflictMessage.append(mergeContent);
        conflictMessage.append(">>>>>>>");
        File conflictFile = join(Repository.CWD, fileName);
        Utils.writeContents(conflictFile, conflictMessage.toString());
        return Blob.blob(conflictFile);
    }

}
