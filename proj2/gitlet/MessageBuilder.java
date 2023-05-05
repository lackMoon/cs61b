package gitlet;

import java.util.Locale;
import java.util.Objects;

public class MessageBuilder {
    private StringBuilder builder;
    public MessageBuilder() {
        builder = new StringBuilder();
    }

    public void appendSegment(String segment) {
        builder.append(System.getProperty("line.separator"));
        this.append(segment);
    }
    public void appendRaw(String message) {
        builder.append(message);
    }
    public void append(String message) {
        builder.append(message);
        builder.append(System.getProperty("line.separator"));
    }

    public void appendPrefix(char symbol) {
        builder.append(symbol);
    }

    public void appendCommitMessage(Commit commit) {
        String commId = commit.getCommitId();
        String mergedId = commit.getMergedParentId();
        this.append("===");
        this.append("commit " + commId);
        if (!Objects.isNull(mergedId)) {
            this.append("Merge: " + commId.substring(0, 7)
                    + " " + mergedId.substring(0, 7));
        }
        this.append("Date: " + String.format(Locale.ENGLISH,
                "%1$ta %1$tb %1$te %1$tH:%1$tM:%1$tS %1$tY %1$tz",
                commit.getTimeStamp()));
        this.append(commit.getMessage());
        builder.append(System.getProperty("line.separator"));
    }

    public boolean isEmpty() {
        return builder.length() == 0;
    }
    @Override
    public String toString() {
        return builder.toString();
    }
}
