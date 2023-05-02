package gitlet;

public class MessageBuilder {
    private StringBuilder builder;
    public MessageBuilder() {
        builder = new StringBuilder();
    }

    public void append(String message) {
        builder.append(message);
    }
    public void appendln(String message) {
        builder.append(message);
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
