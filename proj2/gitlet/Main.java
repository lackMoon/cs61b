package gitlet;

import java.io.File;
import java.util.Date;

/** Driver class for Gitlet, a subset of the Git version-control system.
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            Repository.error("Please enter a command.");
        }
        String firstArg = args[0];
        Repository.changeRepository(Repository.GITLET_DIR);
        switch (firstArg) {
            case "init":
                validateArgs(args, 1);
                Repository.init();
                break;
            case "add":
                validateArgs(args, 2);
                Repository.add(args[1]);
                break;
            case "rm":
                validateArgs(args, 2);
                Repository.rm(args[1]);
                break;
            case "commit":
                validateArgs(args, 2);
                Repository.commit(args[1], new Date(), null);
                break;
            case "checkout":
                validateArgs(args, 2, 4);
                if (args.length == 2) {
                    Repository.checkout(args[1]);
                } else if (args.length == 4) {
                    Repository.checkout(args[1], args[3]);
                } else {
                    Repository.checkout(null, args[2]);
                }
                break;
            case "branch":
                validateArgs(args, 2);
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                validateArgs(args, 2);
                Repository.rmBranch(args[1]);
                break;
            case "reset":
                validateArgs(args, 2);
                Repository.reset(args[1]);
                break;
            case "merge":
                validateArgs(args, 2);
                Repository.merge(args[1]);
                break;
            case "log":
                validateArgs(args, 1);
                Repository.log();
                break;
            case "global-log":
                validateArgs(args, 1);
                Repository.globalLog();
                break;
            case "find":
                validateArgs(args, 2);
                Repository.find(args[1]);
                break;
            case "status":
                validateArgs(args, 1);
                Repository.status();
                break;
            case "add-remote":
                validateArgs(args, 3);
                Repository.addRemote(args[1], args[2]);
                break;
            case "rm-remote":
                validateArgs(args, 2);
                Repository.rmRemote(args[1]);
                break;
            case "push":
                validateArgs(args, 3);
                Repository.push(args[1], args[2]);
                break;
            case "fetch":
                validateArgs(args, 3);
                Repository.fetch(args[1], args[2]);
                break;
            case "pull":
                validateArgs(args, 3);
                Repository.pull(args[1], args[2]);
                break;
            default:
                Repository.error("No command with that name exists.");
        }
    }

    public static void validateArgs(String[] args, int num) {
        validateArgs(args, num, num);
    }

    public static void validateArgs(String[] args, int min, int max) {
        int len = args.length;
        if (len < min || len > max) {
            Repository.error("Incorrect operands.");
        }
        if (args[0].equals("checkout") && len >= 3) {
            validateFormat(args);
        }
        if (!(args[0].equals("init") || Repository.GITLET_DIR.exists())) {
            Repository.error("Not in an initialized Gitlet directory.");
        }
    }

    public static void validateFormat(String[] args) {
        int index = args.length - 2;
        if (!args[index].equals("--")) {
            Repository.error("Incorrect operands.");
        }
    }
}
