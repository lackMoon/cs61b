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
        commit("initial commit", new Date(0));
    }

    public static void add(String fileName) {
        File targetFile = join(CWD, fileName);
        if (!targetFile.exists()) {
            error("File does not exist.");
        }
        HashMap<String, String> stagingArea = Projects.getStagingArea();
        if (Objects.isNull(stagingArea)) {
            stagingArea = new HashMap<>();
        }
        Commit headCommit = Projects.getHeadCommit();
        String fBlobId = Blob.Blob(targetFile);
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

    public static void commit(String message, Date timestamp) {
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
                String fileBlob = stagingArea.remove(fileName);
                if (fileBlob.equals(Projects.STAGED_REMOVAL)) {
                    commit.remove(fileName);
                } else {
                    commit.add(fileName, fileBlob);
                }
            }
            Projects.updateStagingArea(stagingArea);
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
        reset(Utils.readContentsAsString(branch));
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

    public static void reset(String commitId) {
        Commit targetCommit = Commit.acquire(Commit.findCommId(commitId));
        if (Objects.isNull(targetCommit)) {
            error("No commit with that id exists.");
        }
        Commit currentCommit = Projects.getHeadCommit();
        HashMap<String,String> stagingArea = Projects.getStagingArea();
        Set<String> checkoutFiles = targetCommit.getAll();
        Set<String> trackedFiles = currentCommit.getAll();
        trackedFiles.addAll(stagingArea.keySet());
        for (String fileName : checkoutFiles) {
            if (Objects.isNull(currentCommit.get(fileName))
                    && Objects.isNull(stagingArea.get(fileName))) {
                error("There is an untracked file in the way; delete it, or add and commit it first.");
            }
        }
        targetCommit.putAll();
        if (trackedFiles.removeAll(checkoutFiles)) {
            for (String file :trackedFiles) {
                restrictedDelete(file);
            }
        }
        Projects.updateStagingArea(new HashMap<>());
        Projects.updateHeadCommit(commitId);
    }

    public static void status() {
        StringBuilder status = new StringBuilder();
        status.append("=== Branches ===");
        List<String> branchs = plainFilenamesIn(LOCAL);
        Collections.sort(branchs);
        for (String branch : branchs) {
            if (branch.equals(Projects.getCurrentBranch())) {
                status.append("*");
            }
            status.append(branch);
            status.append(System.getProperty("line.separator"));
        }
        status.append(System.getProperty("line.separator"));

        HashMap<String,String> stagingArea = Projects.getStagingArea();
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
        status.append("=== Staged Files ===");
        for (String stagingFile : stagingFiles) {
            status.append(stagingFile);
            status.append(System.getProperty("line.separator"));
        }
        status.append(System.getProperty("line.separator"));
        status.append("=== Removed Files ===");
        for (String removedFile : removedFiles) {
            status.append(removedFile);
            status.append(System.getProperty("line.separator"));
        }
        status.append(System.getProperty("line.separator"));

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
        restrictedDelete(branchFile);
    }
    public static void log() {
        File log = join(LOG_DIR, Projects.getCurrentBranch());
        Commit commit = Projects.getHeadCommit();
        StringBuilder logMessage = new StringBuilder();
        while (!Objects.isNull(commit)) {
            Projects.addMessage(logMessage, commit);
            String parentId = commit.getParentId();
            commit = Objects.isNull(parentId) ? null : Commit.acquire(parentId);
        }
        System.out.println(logMessage);
        Utils.writeContents(log, logMessage.toString());
    }

    public static void globalLog() {
        List<String> commitDirs = plainFilenamesIn(COMMITS_DIR);
        StringBuilder logMessage = new StringBuilder();
        for (String commitDir : commitDirs) {
            List<String> ids = plainFilenamesIn(commitDir);
            for (String id : ids) {
                Commit commit = Commit.acquire(id);
                Projects.addMessage(logMessage, commit);
            }
        }
        System.out.println(logMessage);
    }

    public static void find(String commitMessage) {
        List<String> commitDirs = plainFilenamesIn(COMMITS_DIR);
        StringBuilder idMessage = new StringBuilder();
        for (String commitDir : commitDirs) {
            List<String> ids = plainFilenamesIn(commitDir);
            for (String id : ids) {
                Commit commit = Commit.acquire(id);
                if (commit.getMessage().equals(commitMessage)) {
                    idMessage.append(id);
                    idMessage.append(System.getProperty("line.separator"));
                }
            }
        }
        if (idMessage.isEmpty()) {
            error("Found no commit with that message.");
        }
        System.out.println(idMessage);
    }
}
