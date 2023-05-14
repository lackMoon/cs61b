package gitlet;

import java.io.File;

import static gitlet.Utils.*;

/** Represents a gitlet blob object.
 *  does at a high level.
 *
 */
class Blob {


    static String blob(File file) {
        String content = Utils.readContentsAsString(file);
        return blob(content);
    }

    static String blob(String content) {
        String blobId = sha1(content);
        return blob(blobId, content);
    }

    static String blob(String blobId, String content) {
        File dict = join(Repository.OBJECTS_DIR, blobId.substring(0, 2));
        if (!dict.exists()) {
            dict.mkdir();
        }
        File blobFile = join(dict, blobId);
        if (!blobFile.exists()) {
            Utils.writeContents(blobFile, content);
        }
        return blobId;
    }

    static String content(String blobId) {
        File dict = join(Repository.OBJECTS_DIR, blobId.substring(0, 2));
        if (!dict.exists()) {
            return null;
        }
        File blobFile = join(dict, blobId);
        if (!blobFile.exists()) {
            return null;
        }
        return Utils.readContentsAsString(blobFile);
    }

}
