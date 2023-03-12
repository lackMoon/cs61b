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
    static String findBlobId(String prefix) {
        File dict = join(Repository.OBJECTS_DIR, prefix.substring(0, 2));
        if (!dict.exists()) {
            return null;
        }
        List<String> blobList = Utils.plainFilenamesIn(dict);
        for (String blob : blobList) {
            if (blob.startsWith(prefix)) {
                return blob;
            }
        }
        return null;
    }
    static File convertObjToBlob(Serializable obj) {
        String blobId = obj instanceof File
                ? sha1(Utils.readContentsAsString((File) obj)) : sha1(serialize(obj));
        File dict = join(Repository.OBJECTS_DIR, blobId.substring(0, 2));
        if (!dict.exists()) {
            dict.mkdir();
        }
        File blobFile = join(dict, blobId);
        if (!blobFile.exists()) {
            if (obj instanceof File) {
                Utils.writeContents(blobFile, Utils.readContents((File) obj));
            } else {
                Utils.writeObject(blobFile, obj);
            }
        }
        return blobFile;
    }

    static <T extends Serializable> T convertBlobToObj(String blobId, Class<T> expectedClass) {
        File dict = join(Repository.OBJECTS_DIR, blobId.substring(0, 2));
        if (!dict.exists()) {
            return null;
        }
        File blobFile = join(dict, blobId);
        if (!blobFile.exists()) {
            return null;
        }
        if (expectedClass.isAssignableFrom(String.class)) {
            return (T) Utils.readContentsAsString(blobFile);
        } else {
            return Utils.readObject(blobFile, expectedClass);
        }
    }

}
