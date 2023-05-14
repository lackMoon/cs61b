package gitlet;

import java.io.File;

import java.util.*;

import static gitlet.Utils.*;
import static gitlet.Gitlet.*;


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
    static File OBJECTS_DIR;

    /** The commits directory. */
    static File COMMITS_DIR;

    /** The refs directory. */
    static File REFS_DIR;

    /** The local branch directory. */
    static File LOCAL;

    /** The remote branch directory. */
    static File REMOTES;

    /** The index file which stores content of staging files. */
    static File INDEX;

    /** The remote file which stores address of remote repositories. */
    static File REMOTE;

    /** The head file which indicate current branch of repo. */
    static File HEAD;

    public static void error(String message) {
        System.out.println(message);
        System.exit(0);
    }

    public static void changeRepository(File gitRepository) {
        OBJECTS_DIR = join(gitRepository, "objects");
        COMMITS_DIR = join(gitRepository, "commits");
        REFS_DIR = join(gitRepository, "refs");
        LOCAL = join(REFS_DIR, "local");
        REMOTES = join(REFS_DIR, "remotes");
        INDEX = join(gitRepository, "index");
        REMOTE = join(gitRepository, "remote");
        HEAD = join(gitRepository, "HEAD");
    }
    public static void init() {
        if (GITLET_DIR.exists()) {
            error("A Gitlet version-control system "
                    + "already exists in the current directory.");
        }
        GITLET_DIR.mkdir();
        OBJECTS_DIR.mkdir();
        COMMITS_DIR.mkdir();
        REFS_DIR.mkdir();
        LOCAL.mkdir();
        REMOTES.mkdir();
        branch("master");
        updateBranch("master");
        commit("initial commit", new Date(0), null);
        HashMap<String, String> stagingArea = new HashMap<>();
        updateStagingArea(stagingArea);
    }

    private static void add(String fileName, String blobId) {
        File file = join(CWD, fileName);
        writeContents(file, Blob.content(blobId));
        add(fileName);
    }
    public static void add(String fileName) {
        File targetFile = join(CWD, fileName);
        if (!targetFile.exists()) {
            error("File does not exist.");
        }
        HashMap<String, String> stagingArea = getStagingArea();
        Commit headCommit = getHeadCommit();
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
        updateStagingArea(stagingArea);
    }

    public static void rm(String fileName) {
        HashMap<String, String> stagingArea = getStagingArea();
        String sBlobId = stagingArea.get(fileName);
        String cBlobId = getHeadCommit().get(fileName);
        if (Objects.isNull(sBlobId) && Objects.isNull(cBlobId)) {
            error("No reason to remove the file.");
        }
        if (!(Objects.isNull(sBlobId) || sBlobId.equals(STAGED_REMOVAL))) {
            stagingArea.remove(fileName);
        } else if (!Objects.isNull(cBlobId)) {
            stagingArea.put(fileName, STAGED_REMOVAL);
            File file = join(CWD, fileName);
            if (file.exists()) {
                restrictedDelete(file);
            }
        }
        updateStagingArea(stagingArea);
    }

    public static void commit(String message, Date timestamp, String mergedParentId) {
        if (message.isBlank()) {
            error("Please enter a commit message.");
        }
        Commit commit = new Commit(message, timestamp);
        HashMap<String, String> stagingArea = getStagingArea();
        if (!Objects.isNull(stagingArea)) {
            if (stagingArea.isEmpty()) {
                error("No changes added to the commit.");
            }
            for (String fileName : stagingArea.keySet()) {
                String fileBlob = stagingArea.get(fileName);
                if (fileBlob.equals(STAGED_REMOVAL)) {
                    commit.remove(fileName);
                } else {
                    commit.add(fileName, fileBlob);
                }
            }
            stagingArea.clear();
            updateStagingArea(stagingArea);
        }
        if (!Objects.isNull(mergedParentId)) {
            commit.setMergedParentId(mergedParentId);
        }
        String commitId = commit.save();
        updateHeadCommit(commitId);
    }

    public static void checkout(String branchName) {
        String currentBranchName = getCurrentBranch();
        if (branchName.equals(currentBranchName)) {
            error("No need to checkout the current branch.");
        }
        File branchFile = getBranch(branchName);
        if (!branchFile.exists()) {
            error("No such branch exists.");
        }
        switchToCommit(Utils.readContentsAsString(branchFile));
        updateBranch(branchName);
    }
    public static void checkout(String commitId, String fileName) {
        Commit commit = Objects.isNull(commitId) ? getHeadCommit()
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
        HashMap<String, String> stagingArea = getStagingArea();
        Set<String> trackedFiles = getTrackedFiles();
        Set<String> checkoutFiles = targetCommit.getAll();
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
        updateStagingArea(stagingArea);
    }
    public static void reset(String commitId) {
        switchToCommit(commitId);
        updateHeadCommit(commitId);
    }

    public static void status() {
        MessageBuilder statusMessage = new MessageBuilder();
        statusMessage.append("=== Branches ===");
        List<String> branchs = plainFilenamesIn(LOCAL);
        Collections.sort(branchs);
        for (String branch : branchs) {
            if (branch.equals(getCurrentBranch())) {
                statusMessage.appendPrefix('*');
            }
            statusMessage.append(branch);
        }
        HashMap<String, String> stagingArea = getStagingArea();
        Commit headCommit = getHeadCommit();
        Set<String> trackedFiles = getTrackedFiles();
        Set<String> untrackedFiles = getUntrackedFiles();
        Set<String> stagingFiles = new TreeSet<>();
        Set<String> removedFiles = new TreeSet<>();
        TreeMap<String, Boolean> modifiedFiles = new TreeMap<>();
        for (String fileName : trackedFiles) {
            String sBlobId = stagingArea.get(fileName);
            String cBlobId = headCommit.get(fileName);
            File trackedFile = join(CWD, fileName);
            if (Objects.isNull(sBlobId)) {
                if (!trackedFile.exists()) {
                    modifiedFiles.put(fileName, false);
                } else if (!sha1(Utils.readContents(trackedFile)).equals(cBlobId)) {
                    modifiedFiles.put(fileName, true);
                }
            } else {
                if (sBlobId.equals(STAGED_REMOVAL)) {
                    removedFiles.add(fileName);
                } else if (!trackedFile.exists()) {
                    modifiedFiles.put(fileName, false);
                } else if (!sha1(Utils.readContents(trackedFile)).equals(sBlobId)) {
                    modifiedFiles.put(fileName, true);
                } else {
                    stagingFiles.add(fileName);
                }
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
        for (String modifiedFile : modifiedFiles.keySet()) {
            statusMessage.appendRaw(modifiedFile);
            if (modifiedFiles.get(modifiedFile)) {
                statusMessage.appendSuffix("modified");
            } else {
                statusMessage.appendSuffix("deleted");
            }
        }
        statusMessage.appendSegment("=== Untracked Files ===");
        for (String untrackedFile : untrackedFiles) {
            statusMessage.append(untrackedFile);
        }
        System.out.println(statusMessage);
    }
    public static void branch(String name) {
        File branchFile = getBranch(name);
        if (branchFile.exists()) {
            error("A branch with that name already exists.");
        }
        Commit headCommit = getHeadCommit();
        if (!Objects.isNull(headCommit)) {
            Utils.writeContents(branchFile, headCommit.getCommitId());
        }
    }

    public static void rmBranch(String name) {
        File branchFile = getBranch(name);
        if (!branchFile.exists()) {
            error("A branch with that name does not exist.");
        }
        String currentBranch = getCurrentBranch();
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
                    add(fileName, mBlobId);
                }
            } else {
                if (Objects.isNull(cBlobId)) {
                    if (!(Objects.isNull(mBlobId) || sBlobId.equals(mBlobId))) {
                        //present in split but not in head and modified in merged branch
                        isConflict = true;
                        add(fileName, makeConflictMessage(null, mBlobId));
                    }
                } else {
                    if (Objects.isNull(mBlobId)) {
                        if (sBlobId.equals(cBlobId)) {
                            //present in split and unmodified in head but not in merged branch
                            rm(fileName);
                        } else {
                            //present in split but not in merged branch and modified in head
                            isConflict = true;
                            add(fileName, makeConflictMessage(cBlobId, null));
                        }
                    } else {
                        if (sBlobId.equals(cBlobId) && !sBlobId.equals(mBlobId)) {
                            //present in all,unmodified in head but modified in merged branch
                            add(fileName, mBlobId);
                        } else if (!(sBlobId.equals(cBlobId) || sBlobId.equals(mBlobId))) {
                            //present in all,modified in head and merged branch
                            isConflict = true;
                            add(fileName, makeConflictMessage(cBlobId, mBlobId));
                        }
                    }
                }
            }
        }
        return isConflict;
    }
    public static void merge(String name) {
        HashMap<String, String> stagingArea = getStagingArea();
        String currentBranch = getCurrentBranch();
        if (!stagingArea.isEmpty()) {
            error("You have uncommitted changes.");
        }
        if (currentBranch.equals(name)) {
            error("Cannot merge a branch with itself.");
        }
        File branchFile = getBranch(name);
        if (!branchFile.exists()) {
            error("A branch with that name does not exist.");
        }
        String mergeId = Utils.readContentsAsString(branchFile);
        Commit headCommit = getHeadCommit();
        Commit mergeCommit = Commit.acquire(mergeId);
        String splitPoint = headCommit.findSplitPoint(mergeCommit);
        Commit splitCommit = Commit.acquire(splitPoint);
        if (splitPoint.equals(mergeCommit.getCommitId())) {
            error("Given branch is an ancestor of the current branch.");
        } else if (splitPoint.equals(headCommit.getCommitId())) {
            switchToCommit(mergeId);
            updateBranch(name);
            error("Current branch fast-forwarded.");
        }
        Set<String> untrackedSet = getUntrackedFiles();
        Set<String> fileSet = new HashSet<>();
        fileSet.addAll(headCommit.getAll());
        fileSet.addAll(mergeCommit.getAll());
        fileSet.addAll(splitCommit.getAll());
        if (!untrackedSet.isEmpty()) {
            for (String untrackedFile : untrackedSet) {
                if (mergeCommit.contains(untrackedFile)) {
                    error("There is an untracked file in the way;"
                            + " delete it, or add and commit it first.");
                }
            }
        }
        boolean isConflict = mergeFile(fileSet, headCommit, mergeCommit, splitCommit);
        commit("Merged " + name + " into " + currentBranch + ".", new Date(), mergeId);
        getHeadCommit().putAll();
        if (isConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }
    public static void log() {
        Commit commit = getHeadCommit();
        MessageBuilder logMessage = new MessageBuilder();
        while (!Objects.isNull(commit)) {
            makeCommitMessage(logMessage, commit);
            String parentId = commit.getParentId();
            commit = Objects.isNull(parentId) ? null : Commit.acquire(parentId);
        }
        System.out.print(logMessage);
    }

    public static void globalLog() {
        List<String> commitDir = plainFilenamesIn(COMMITS_DIR);
        MessageBuilder logMessage = new MessageBuilder();
        for (String commitId : commitDir) {
            Commit commit = Commit.acquire(commitId);
            makeCommitMessage(logMessage, commit);
        }
        System.out.print(logMessage);
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

    public static void addRemote(String name, String address) {
        HashMap<String, String> remote = getRemoteInformation();
        if (remote.containsKey(name)) {
            error("A remote with that name already exists.");
        }
        StringBuilder pathBuilder = new StringBuilder();
        for (String path : address.split("/")) {
            pathBuilder.append(path);
            pathBuilder.append(File.separator);
        }
        remote.put(name, pathBuilder.toString());
        updateRemoteInformation(remote);
    }

    public static void rmRemote(String name) {
        HashMap<String, String> remote = getRemoteInformation();
        if (!remote.containsKey(name)) {
            error("A remote with that name does not exist.");
        }
        remote.remove(name);
        updateRemoteInformation(remote);
    }

    public static void fetch(String name, String branchName) {
        HashMap<String, String> remote = getRemoteInformation();
        File repository = join(CWD, remote.get(name));
        if (!repository.exists()) {
            error("Remote directory not found.");
        }
        changeRepository(repository);
        File branch = getBranch(branchName);
        if (!branch.exists()) {
            error("That remote does not have that branch.");
        }
        HashSet<Commit> fetchCommits = new HashSet<>();
        HashMap<String, String> fetchBlobs = new HashMap<>();
        Commit commit = getBranchCommit(branch);
        String remoteHeadId = commit.getCommitId();
        while (!Objects.isNull(commit)) {
            fetchSnapShots(commit, fetchCommits, fetchBlobs);
            commit = Commit.acquire(commit.getParentId());
        }
        changeRepository(GITLET_DIR);
        File remoteDir = Utils.join(REMOTES, name);
        if (!remoteDir.exists()) {
            remoteDir.mkdir();
        }
        Utils.writeContents(Utils.join(remoteDir, branchName), remoteHeadId);
        pushSnapShots(fetchCommits, fetchBlobs);
    }

    public static void pull(String name, String branchName) {
        String remoteBranchName = name + "/" + branchName;
        fetch(name, branchName);
        merge(remoteBranchName);
    }

    public static void push(String name, String branchName) {
        File repository = join(CWD, getRemoteInformation().get(name));
        if (!repository.exists()) {
            error("Remote directory not found.");
        }
        changeRepository(repository);
        File branch = getBranch(branchName);
        if (!branch.exists()) {
            Commit remoteHead = getHeadCommit();
            Utils.writeObject(branch, remoteHead);
            System.exit(0);
        }
        String remoteCommitId = getBranchCommit(branch).getCommitId();
        changeRepository(GITLET_DIR);
        Commit commit = getHeadCommit();
        String headCommitId = commit.getCommitId();
        Boolean isAncestor = false;
        HashSet<Commit> fetchCommits = new HashSet<>();
        HashMap<String, String> fetchBlobs = new HashMap<>();
        while (!Objects.isNull(commit)) {
            String commitId = commit.getCommitId();
            if (commitId.equals(remoteCommitId)) {
                isAncestor = true;
                break;
            }
            fetchSnapShots(commit, fetchCommits, fetchBlobs);
        }
        if (!isAncestor) {
            error("Please pull down remote changes before pushing.");
        }
        changeRepository(repository);
        pushSnapShots(fetchCommits, fetchBlobs);
        reset(headCommitId);
        changeRepository(GITLET_DIR);
    }
}
