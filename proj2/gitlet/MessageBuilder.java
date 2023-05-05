package gitlet;


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
        if (message.equals("")) {
            appendRaw(message);
        } else {
            builder.append(message);
            builder.append(System.getProperty("line.separator"));
        }
    }

    public void appendPrefix(char symbol) {
        builder.append(symbol);
    }

    public void appendSuffix(String suffix) {
        this.append(" (" + suffix + ")");
    }

    public boolean isEmpty() {
        return builder.length() == 0;
    }
    @Override
    public String toString() {
        return builder.toString();
    }
}
