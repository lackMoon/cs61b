package gitlet;

import java.io.File;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Date;
import java.util.Locale;
import java.util.Collections;
import java.util.Objects;

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
        File blobFile = Blob.convertObjToBlob(targetFile);
        HashMap<String, String> stagingArea = Projects.getStagingArea();
        if (Objects.isNull(stagingArea)) {
            stagingArea = new HashMap<>();
        }
        Commit headCommit = Projects.getHeadCommit();
        String fBlobId = blobFile.getName();
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
        File commitBlob = Blob.convertObjToBlob(commit);
        Projects.updateHeadCommit(commitBlob.getName());
    }

    public static void checkout(String branchName) {
        String currentBranchName = Projects.getBranch();
        if (branchName.equals(currentBranchName)) {
            error("No need to checkout the current branch.");
        }
        File branch = join(LOCAL, branchName);
        if (!branch.exists()) {
            error("No such branch exists.");
        }
        Commit branchCommit =
                Blob.convertBlobToObj(Utils.readContentsAsString(branch), Commit.class);
        Set<String> checkoutFiles = branchCommit.getAll();
        Set<String> currentFiles = Projects.getHeadCommit().getAll();
        FileScanner scanner = new FileScanner();
        scanner.scan();
        List<String> untrackedFiles = scanner.getUntrackedFiles();
        if (Collections.disjoint(checkoutFiles, untrackedFiles)) {
            error("There is an untracked file in the way; delete it, or add and commit it first.");
        }
        branchCommit.putAll();
        if (currentFiles.removeAll(checkoutFiles)) {
            for (String fileName : checkoutFiles) {
                File file = join(Repository.CWD, fileName);
                restrictedDelete(file);
            }
        }
        Projects.updateStagingArea(new HashMap<>());
        Projects.updateHeadCommit(branchCommit.getCommitId());
        Projects.updateBranch(branchName);
    }
    public static void checkout(String commitId, String fileName) {
        Commit commit = Objects.isNull(commitId) ? Projects.getHeadCommit()
                : Blob.convertBlobToObj(Blob.findBlobId(commitId), Commit.class);
        if (Objects.isNull(commit)) {
            error("No commit with that id exists.");
        }
        if (!commit.put(fileName)) {
            error("File does not exist in that commit.");
        }
    }

    public static void branch(String name) {
        File branchFile = Utils.join(Repository.LOCAL, name);
        if (branchFile.exists()) {
            Repository.error("A branch with that name already exists.");
        }
        Commit headCommit = Projects.getHeadCommit();
        if (!Objects.isNull(headCommit)) {
            Utils.writeContents(branchFile, headCommit.getCommitId());
        }
    }
    public static void log() {
        File log = join(LOG_DIR, Projects.getBranch());
        StringBuilder logMessage = new StringBuilder();
        Commit commit = Projects.getHeadCommit();
        while (!Objects.isNull(commit)) {
            logMessage.append("===");
            logMessage.append(System.getProperty("line.separator"));
            logMessage.append("commit " + commit.getCommitId());
            logMessage.append(System.getProperty("line.separator"));
            logMessage.append("Date: " + String.format(Locale.ENGLISH,
                    "%1$ta %1$tb %1$te %1$tH:%1$tM:%1$tS %1$tY %1$tz",
                    commit.getTimeStamp()));
            logMessage.append(System.getProperty("line.separator"));
            logMessage.append(commit.getMessage());
            logMessage.append(System.getProperty("line.separator"));
            logMessage.append(System.getProperty("line.separator"));
            String parentId = commit.getParentId();
            commit = Objects.isNull(parentId)
                    ? null : Blob.convertBlobToObj(parentId, Commit.class);
        }
        System.out.println(logMessage);
        Utils.writeContents(log, logMessage.toString());
    }
}
