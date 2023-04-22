package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import static gitlet.Utils.*;

/** Represents a gitlet blob object.
 *  does at a high level.
 *
 */
class Blob {

    static String Blob(File file) {
        String blobId = sha1(Utils.readContentsAsString(file));
        File dict = join(Repository.OBJECTS_DIR, blobId.substring(0, 2));
        if (!dict.exists()) {
            dict.mkdir();
        }
        File blobFile = join(dict, blobId);
        if (!blobFile.exists()) {
            Utils.writeContents(blobFile, Utils.readContents(file));
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
