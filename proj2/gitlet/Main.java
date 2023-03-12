package gitlet;

import java.util.Date;

/** Driver class for Gitlet, a subset of the Git version-control system.
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            Repository.error("Must have at least one argument");
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                validateArgs(args, new int[]{1});
                Repository.init();
                break;
            case "add":
                validateArgs(args, new int[]{2});
                Repository.add(args[1]);
                break;
            case "commit":
                validateArgs(args, new int[]{2});
                Repository.commit(args[1], new Date());
                break;
            case "checkout":
                validateArgs(args, new int[]{2, 3, 4});
                if (args.length == 2) {
                    Repository.checkout(args[1]);
                } else if (args.length == 4) {
                    Repository.checkout(args[1], args[3]);
                } else {
                    Repository.checkout(null, args[2]);
                }
                break;
            case "log":
                validateArgs(args, new int[]{1});
                Repository.log();
                break;
            default:
                Repository.error("No command with that name exists.");
        }
    }

    public static void validateArgs(String[] args, int[] nums) {
        boolean isLengthCorrect = false;
        int len = args.length;
        for (int num : nums) {
            if (len == num) {
                isLengthCorrect = true;
                break;
            }
        }
        if (!isLengthCorrect) {
            Repository.error("Incorrect operands.");
        }
        if (!(args[0].equals("init") || Repository.GITLET_DIR.exists())) {
            Repository.error("Not in an initialized Gitlet directory.");
        }
    }
}
