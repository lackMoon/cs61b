package gitlet;

import java.io.File;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Objects;

import static gitlet.Utils.*;
import static gitlet.Projects.*;
public class FileScanner {

    private List<String> stagedFiles;

    private List<String> removedFiles;

    private List<String> deletedFiles;

    private List<String> modifiedFiles;

    private List<String> untrackedFiles;

    public FileScanner() {
        this.stagedFiles = new ArrayList<>();
        this.removedFiles = new ArrayList<>();
        this.deletedFiles = new ArrayList<>();
        this.modifiedFiles = new ArrayList<>();
        this.untrackedFiles = new ArrayList<>();
    }

    public List<String> getStagedFiles() {
        Collections.sort(stagedFiles);
        return stagedFiles;
    }

    public List<String> getRemovedFiles() {
        Collections.sort(removedFiles);
        return removedFiles;
    }

    public List<String> getDeletedFiles() {
        Collections.sort(deletedFiles);
        return deletedFiles;
    }

    public List<String> getModifiedFiles() {
        Collections.sort(modifiedFiles);
        return modifiedFiles;
    }

    public List<String> getUntrackedFiles() {
        Collections.sort(untrackedFiles);
        return untrackedFiles;
    }
    private boolean isFileChangedOrNotExist(String fileName, String targetBlob) {
        File file = Utils.join(Repository.CWD, fileName);
        String fileBlob = file.exists() ? sha1(readContentsAsString(file)) : null;
        if (Objects.isNull(fileBlob)) {
            deletedFiles.add(fileName);
            return true;
        } else if (!targetBlob.equals(fileBlob)) {
            modifiedFiles.add(fileName);
            return true;
        }
        return false;
    }

    public void scan() {
        Commit currentCommit = Projects.getHeadCommit();
        HashMap<String, String> stagingArea = getStagingArea();
        Set<String> scanFiles = new HashSet<>();
        scanFiles.addAll(plainFilenamesIn(Repository.CWD));
        scanFiles.addAll(stagingArea.keySet());
        scanFiles.addAll(currentCommit.getAll());
        for (String fileName : scanFiles) {
            String stagingBlob = stagingArea.get(fileName);
            String commitBlob = currentCommit.get(fileName);
            if (Objects.isNull(stagingBlob) && Objects.isNull(commitBlob)) {
                //unTracked File
                untrackedFiles.add(fileName);
            } else if (!Objects.isNull(stagingBlob)) {
                //Staged File
                if (!stagingBlob.equals(STAGED_REMOVAL)) {
                    //Stage for Addition
                    if (!isFileChangedOrNotExist(fileName, stagingBlob)) {
                        stagedFiles.add(fileName);
                    }
                } else {
                    //Stage for Removal
                    removedFiles.add(fileName);
                }
            } else if (!Objects.isNull(commitBlob)) {
                //Tracked File
                isFileChangedOrNotExist(fileName, commitBlob);
            }
        }
    }

}
