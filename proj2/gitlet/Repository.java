package gitlet;

import java.io.File;

import java.util.*;

import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *
 */
public class Repository {
    /**
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /** The objects directory. */
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");

    /** The commits directory. */
    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");

    /** The log directory. */
    public static final File LOG_DIR = join(GITLET_DIR, "logs");

    /** The refs directory. */
    public static final File REFS_DIR = join(GITLET_DIR, "refs");

    /** The local branch directory. */
    public static final File LOCAL = join(REFS_DIR, "local");

    /** The index file which stores content of staging files. */
    public static final File INDEX = join(GITLET_DIR, "index");

    /** The head file which indicate current branch of repo. */
    public static final File HEAD = join(GITLET_DIR, "HEAD");

    public static void error(String message) {
        System.out.println(message);
        System.exit(0);
    }
    public static void init() {
        if (GITLET_DIR.exists()) {
            error("A Gitlet version-control system "
                    + "already exists in the current directory.");
        }
        GITLET_DIR.mkdir();
        OBJECTS_DIR.mkdir();
        COMMITS_DIR.mkdir();
        LOG_DIR.mkdir();
        REFS_DIR.mkdir();
        LOCAL.mkdir();
        branch("master");
        Projects.updateBranch("master");
        commit("initial commit", new Date(0), null);
        HashMap<String, String> stagingArea = new HashMap<>();
        Projects.updateStagingArea(stagingArea);
    }

    public static void add(String fileName) {
        File targetFile = join(CWD, fileName);
        if (!targetFile.exists()) {
            error("File does not exist.");
        }
        HashMap<String, String> stagingArea = Projects.getStagingArea();
        Commit headCommit = Projects.getHeadCommit();
        String fBlobId = Blob.blob(targetFile);
        String sBlobId = stagingArea.get(fileName);
        String cBlobId = Objects.isNull(headCommit) ? null : headCommit.get(fileName);
        if (!fBlobId.equals(sBlobId)) {
            if (fBlobId.equals(cBlobId)) {
                stagingArea.remove(fileName);
            } else {
                stagingArea.put(fileName, fBlobId);
            }
        }
        Projects.updateStagingArea(stagingArea);
    }

    public static void rm(String fileName) {
        HashMap<String, String> stagingArea = Projects.getStagingArea();
        String sBlobId = stagingArea.get(fileName);
        String cBlobId = Projects.getHeadCommit().get(fileName);
        if (Objects.isNull(sBlobId) && Objects.isNull(cBlobId)) {
            error("No reason to remove the file.");
        }
        if (!(Objects.isNull(sBlobId) || sBlobId.equals(Projects.STAGED_REMOVAL))) {
            stagingArea.remove(fileName);
        } else if (!Objects.isNull(cBlobId)) {
            stagingArea.put(fileName, Projects.STAGED_REMOVAL);
            File file = join(CWD, fileName);
            if (file.exists()) {
                restrictedDelete(file);
            }
        }
        Projects.updateStagingArea(stagingArea);
    }

    public static void commit(String message, Date timestamp, String mergedParentId) {
        if (message.isBlank()) {
            error("Please enter a commit message.");
        }
        Commit commit = new Commit(message, timestamp);
        HashMap<String, String> stagingArea = Projects.getStagingArea();
        if (!Objects.isNull(stagingArea)) {
            if (stagingArea.isEmpty()) {
                error("No changes added to the commit.");
            }
            for (String fileName : stagingArea.keySet()) {
                String fileBlob = stagingArea.get(fileName);
                if (fileBlob.equals(Projects.STAGED_REMOVAL)) {
                    commit.remove(fileName);
                } else {
                    commit.add(fileName, fileBlob);
                }
            }
            stagingArea.clear();
            Projects.updateStagingArea(stagingArea);
        }
        if (!Objects.isNull(mergedParentId)) {
            commit.setMergedParentId(mergedParentId);
        }
        String commitId = commit.save();
        Projects.updateHeadCommit(commitId);
    }

    public static void checkout(String branchName) {
        String currentBranchName = Projects.getCurrentBranch();
        if (branchName.equals(currentBranchName)) {
            error("No need to checkout the current branch.");
        }
        File branch = join(LOCAL, branchName);
        if (!branch.exists()) {
            error("No such branch exists.");
        }
        switchToCommit(Utils.readContentsAsString(branch));
        Projects.updateBranch(branchName);
    }
    public static void checkout(String commitId, String fileName) {
        Commit commit = Objects.isNull(commitId) ? Projects.getHeadCommit()
                : Commit.acquire(Commit.findCommId(commitId));
        if (Objects.isNull(commit)) {
            error("No commit with that id exists.");
        }
        if (!commit.put(fileName)) {
            error("File does not exist in that commit.");
        }
    }

    private static void switchToCommit(String commitId) {
        Commit targetCommit = Commit.acquire(Commit.findCommId(commitId));
        if (Objects.isNull(targetCommit)) {
            error("No commit with that id exists.");
        }
        Commit currentCommit = Projects.getHeadCommit();
        HashMap<String, String> stagingArea = Projects.getStagingArea();
        Set<String> trackedFiles = new HashSet<>();
        Set<String> checkoutFiles = targetCommit.getAll();
        trackedFiles.addAll(currentCommit.getAll());
        trackedFiles.addAll(stagingArea.keySet());
        for (String fileName : checkoutFiles) {
            if (!trackedFiles.contains(fileName) && join(CWD, fileName).exists()) {
                error("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
            }
        }
        targetCommit.putAll();
        trackedFiles.removeAll(checkoutFiles);
        for (String file :trackedFiles) {
            restrictedDelete(file);
        }
        stagingArea.clear();
        Projects.updateStagingArea(stagingArea);
    }
    public static void reset(String commitId) {
        switchToCommit(commitId);
        Projects.updateHeadCommit(commitId);
    }

    public static void status() {
        MessageBuilder statusMessage = new MessageBuilder();
        statusMessage.append("=== Branches ===");
        List<String> branchs = plainFilenamesIn(LOCAL);
        Collections.sort(branchs);
        for (String branch : branchs) {
            if (branch.equals(Projects.getCurrentBranch())) {
                statusMessage.appendPrefix('*');
            }
            statusMessage.append(branch);
        }
        HashMap<String, String> stagingArea = Projects.getStagingArea();
        Set<String> trackedFiles = stagingArea.keySet();
        Set<String> stagingFiles = new TreeSet<>();
        Set<String> removedFiles = new TreeSet<>();
        for (String fileName : trackedFiles) {
            if (stagingArea.get(fileName).equals(Projects.STAGED_REMOVAL)) {
                removedFiles.add(fileName);
            } else {
                stagingFiles.add(fileName);
            }
        }
        statusMessage.appendSegment("=== Staged Files ===");
        for (String stagingFile : stagingFiles) {
            statusMessage.append(stagingFile);
        }
        statusMessage.appendSegment("=== Removed Files ===");
        for (String removedFile : removedFiles) {
            statusMessage.append(removedFile);
        }
        statusMessage.appendSegment("=== Modifications Not Staged For Commit ===");
        statusMessage.appendSegment("=== Untracked Files ===");
        System.out.println(statusMessage);
    }
    public static void branch(String name) {
        File branchFile = Utils.join(LOCAL, name);
        if (branchFile.exists()) {
            error("A branch with that name already exists.");
        }
        Commit headCommit = Projects.getHeadCommit();
        if (!Objects.isNull(headCommit)) {
            Utils.writeContents(branchFile, headCommit.getCommitId());
        }
    }

    public static void rmBranch(String name) {
        File branchFile = Utils.join(Repository.LOCAL, name);
        if (!branchFile.exists()) {
            error("A branch with that name does not exist.");
        }
        String currentBranch = Projects.getCurrentBranch();
        if (currentBranch.equals(name)) {
            error("Cannot remove the current branch.");
        }
        branchFile.delete();
    }

    private static boolean mergeFile(Set<String> fileSet, Commit headCommit,
                                     Commit mergeCommit, Commit splitCommit) {
        boolean isConflict = false;
        for (String fileName : fileSet) {
            String cBlobId = headCommit.get(fileName);
            String mBlobId = mergeCommit.get(fileName);
            String sBlobId = splitCommit.get(fileName);
            if (Objects.isNull(sBlobId)) {
                if (Objects.isNull(cBlobId) && !Objects.isNull(mBlobId)) {
                    //not in split nor head but in merged branch
                    Utils.writeContents(join(CWD, fileName), Blob.content(mBlobId));
                    add(fileName);
                }
            } else {
                if (Objects.isNull(cBlobId)) {
                    if (!(Objects.isNull(mBlobId) || sBlobId.equals(mBlobId))) {
                        //present in split but not in head and modified in merged branch
                        isConflict = true;
                        Projects.makeConflictFile(fileName, null, mBlobId);
                        add(fileName);
                    }
                } else {
                    if (Objects.isNull(mBlobId)) {
                        if (sBlobId.equals(cBlobId)) {
                            //present in split and unmodified in head but not in merged branch
                            rm(fileName);
                        } else {
                            //present in split but not in merged branch and modified in head
                            isConflict = true;
                            Projects.makeConflictFile(fileName, cBlobId, null);
                            add(fileName);
                        }
                    } else {
                        if (sBlobId.equals(cBlobId) && !sBlobId.equals(mBlobId)) {
                            //present in all,unmodified in head but modified in merged branch
                            Utils.writeContents(join(CWD, fileName), Blob.content(mBlobId));
                            add(fileName);
                        } else if (!(sBlobId.equals(cBlobId) || sBlobId.equals(mBlobId))) {
                            //present in all,modified in head and merged branch
                            isConflict = true;
                            Projects.makeConflictFile(fileName, cBlobId, mBlobId);
                            add(fileName);
                        }
                    }
                }
            }
        }
        return isConflict;
    }
    public static void merge(String name) {
        HashMap<String, String> stagingArea = Projects.getStagingArea();
        String currentBranch = Projects.getCurrentBranch();
        if (!stagingArea.isEmpty()) {
            error("You have uncommitted changes.");
        }
        if (currentBranch.equals(name)) {
            error("Cannot merge a branch with itself.");
        }
        File branchFile = Utils.join(LOCAL, name);
        if (!branchFile.exists()) {
            error("A branch with that name does not exist.");
        }
        String mergeId = Utils.readContentsAsString(branchFile);
        Commit headCommit = Projects.getHeadCommit();
        Commit mergeCommit = Commit.acquire(mergeId);
        String splitPoint = headCommit.findSplitPoint(mergeCommit);
        Commit splitCommit = Commit.acquire(splitPoint);
        if (splitPoint.equals(mergeCommit.getCommitId())) {
            error("Given branch is an ancestor of the current branch.");
        } else if (splitPoint.equals(headCommit.getCommitId())) {
            switchToCommit(mergeId);
            Projects.updateBranch(name);
            error("Current branch fast-forwarded.");
        }
        Set<String> untrackedSet = new HashSet<>();
        Set<String> fileSet = new HashSet<>();
        Set<String> headSet = headCommit.getAll();
        Set<String> mergeSet = mergeCommit.getAll();
        Set<String> splitSet = splitCommit.getAll();
        fileSet.addAll(headSet);
        fileSet.addAll(mergeSet);
        fileSet.addAll(splitSet);
        untrackedSet.addAll(plainFilenamesIn(CWD));
        untrackedSet.removeAll(headSet);
        if (!untrackedSet.isEmpty()) {
            for (String untrackedFile : untrackedSet) {
                if (mergeSet.contains(untrackedFile)) {
                    error("There is an untracked file in the way;"
                            + " delete it, or add and commit it first.");
                }
            }
        }
        boolean isConflict = mergeFile(fileSet, headCommit, mergeCommit, splitCommit);
        commit("Merged " + name + " into " + currentBranch + ".", new Date(), mergeId);
        Projects.getHeadCommit().putAll();
        if (isConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }
    public static void log() {
        File log = join(LOG_DIR, Projects.getCurrentBranch());
        Commit commit = Projects.getHeadCommit();
        MessageBuilder logMessage = new MessageBuilder();
        while (!Objects.isNull(commit)) {
            logMessage.appendCommitMessage(commit);
            String parentId = commit.getParentId();
            commit = Objects.isNull(parentId) ? null : Commit.acquire(parentId);
        }
        System.out.println(logMessage);
        Utils.writeContents(log, logMessage.toString());
    }

    public static void globalLog() {
        List<String> commitDir = plainFilenamesIn(COMMITS_DIR);
        MessageBuilder logMessage = new MessageBuilder();
        for (String commitId : commitDir) {
            Commit commit = Commit.acquire(commitId);
            logMessage.appendCommitMessage(commit);
        }
        System.out.println(logMessage);
    }

    public static void find(String commitMessage) {
        List<String> commitDir = plainFilenamesIn(COMMITS_DIR);
        MessageBuilder idMessage = new MessageBuilder();
        for (String commitId : commitDir) {
            Commit commit = Commit.acquire(commitId);
            if (commit.getMessage().equals(commitMessage)) {
                idMessage.append(commitId);
            }
        }
        if (idMessage.isEmpty()) {
            error("Found no commit with that message.");
        }
        System.out.println(idMessage);
    }
}
