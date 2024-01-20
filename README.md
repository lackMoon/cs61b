
# Gitlet
* [Overview of Gitlet](#overview-of-gitlet)
* [Internal Structures](#internal-structures)
* [The Commands](#the-commands)
  * [init](#init)
  * [add](#add)
  * [commit](#commit)
  * [rm](#rm)
  * [log](#log)
  * [global-log](#global-log)
  * [find](#find)
  * [status](#status)
  * [checkout](#checkout)
  * [branch](#branch)
  * [rm-branch](#rm-branch)
  * [reset](#reset)
  * [merge](#merge)
  * [add-remote](#add-remote)
  * [rm-remote](#rm-remote)
  * [push](#push)
  * [fetch](#fetch)
  * [pull](#pull)

## Overview of Gitlet

Gitlet is a version-control system that mimics some of the basic features of the popular system Git. Ours is smaller and simpler, however, so we have named it Gitlet.

A version-control system is essentially a backup system for related collections of files. The main functionality that Gitlet supports is:

1.  Saving the contents of entire directories of files. In Gitlet, this is called _committing_, and the saved contents themselves are called _commits_.

2.  Restoring a version of one or more files or entire commits. In Gitlet, this is called _checking out_ those files or that commit.

3.  Viewing the history of your backups. In Gitlet, you view this history in something called the _log_.

4.  Maintaining related sequences of commits, called _branches_.

5.  Merging changes made in one branch into another.

The point of a version-control system is to help you when creating complicated \(or even not-so-complicated\) projects, or when collaborating with others on a project. You save versions of the project periodically. If at some later point in time you accidentally mess up your code, then you can restore your source to a previously committed version \(without losing any of the changes you made since then\). If your collaborators make changes embodied in a commit, you can incorporate \(_merge_\) these changes into your own version.

In Gitlet, you don’t just commit individual files at a time. Instead, you can commit a coherent set of files at the same time. We like to think of each commit as a _snapshot_ of your entire project at one point in time. However, for simplicity, many of the examples in the remainder of this document involve changes to just one file at a time. Just keep in mind you could change multiple files in each commit.

In this project, it will be helpful for us to visualize the commits we make over time. Suppose we have a project consisting just of the file wug.txt, we add some text to it, and commit it. Then we modify the file and commit these changes. Then we modify the file again, and commit the changes again. Now we have saved three total versions of this file, each one later in time than the previous. We can visualize these commits like so:

![Three commits](https://sp21.datastructur.es/materials/proj/proj2/image/three_commits.png)

Here we’ve drawn an arrow indicating that each commit contains some kind of reference to the commit that came before it. We call the commit that came before it the _parent commit_–this will be important later. But for now, does this drawing look familiar\? That’s right; it’s a linked list\!

The big idea behind Gitlet is that we can visualize the history of the different versions of our files in a list like this. Then it’s easy for us to restore old versions of files. You can imagine making a command like: “Gitlet, please revert to the state of the files at commit #2”, and it would go to the second node in the linked list and restore the copies of files found there, while removing any files that are in the first node, but not the second.

If we tell Gitlet to revert to an old commit, the front of the linked list will no longer reflect the current state of your files, which might be a little misleading. In order to fix this problem, we introduce something called the _head_ pointer \(also called the HEAD pointer\). The head pointer keeps track of where in the linked list we currently are. Normally, as we make commits, the head pointer will stay at the front of the linked list, indicating that the latest commit reflects the current state of the files:

![Simple head](https://sp21.datastructur.es/materials/proj/proj2/image/simple_head.png)

However, let’s say we revert to the state of the files at commit #2 \(technically, this is the _reset_ command, which you’ll see later in the spec\). We move the head pointer back to show this:

![Reverted head](https://sp21.datastructur.es/materials/proj/proj2/image/reverted_head.png)

Here we say that we are in a _detatched head state_ which you may have encountered yourself before. This is what it means\!

All right, now, if this were all Gitlet could do, it would be a pretty simple system. But Gitlet has one more trick up its sleeve: it doesn’t just maintain older and newer versions of files, it can maintain _differing_ versions. Imagine you’re coding a project, and you have two ideas about how to proceed: let’s call one Plan A, and the other Plan B. Gitlet allows you to save both versions, and switch between them at will. Here’s what this might look like, in our pictures:

![Two versions](https://sp21.datastructur.es/materials/proj/proj2/image/two_versions.png)

It’s not really a linked list anymore. It’s more like a tree. We’ll call this thing the _commit tree_. Keeping with this metaphor, each of the separate versions is called a _branch_ of the tree. You can develop each version separately:

![Two developed versions](https://sp21.datastructur.es/materials/proj/proj2/image/two_developed_versions.png)

There are two pointers into the tree, representing the furthest point of each branch. At any given time, only one of these is the currently active pointer, and this is what’s called the head pointer. The head pointer is the pointer at the front of the current branch.

That’s it for our brief overview of the Gitlet system\! Don’t worry if you don’t fully understand it yet; the section above was just to give you a high level picture of what its meant to do. A detailed spec of what you’re supposed to do for this project follows this section.

But a last word here: commit trees are _immutable_: once a commit node has been created, it can never be destroyed \(or changed at all\). We can only add new things to the commit tree, not modify existing things. This is an important feature of Gitlet\! One of Gitlet’s goals is to allow us to save things so we don’t delete them accidentally.

## Internal Structures

Real Git distinguishes several different kinds of _objects_. For our purposes, the important ones are

* **_blobs_**: The saved contents of files. Since Gitlet saves many versions of files, a single file might correspond to multiple blobs: each being tracked in a different commit.
* **_trees_**: Directory structures mapping names to references to blobs and other trees \(subdirectories\).
* **_commits_**: Combinations of log messages, other metadata \(commit date, author, etc.\), a reference to a tree, and references to parent commits. The repository also maintains a mapping from _branch heads_ to references to commits, so that certain important commits have symbolic names.

Gitlet simplifies from Git still further by

* Incorporating trees into commits and not dealing with subdirectories \(so there will be one “flat” directory of plain files for each repository\).
* Limiting ourselves to merges that reference two parents \(in real Git, there can be any number of parents.\)
* Having our metadata consist only of a timestamp and log message. A commit, therefore, will consist of a log message, timestamp, a mapping of file names to blob references, a parent reference, and \(for merges\) a second parent reference.

Every object–every blob and every commit in our case–has a unique integer id that serves as a reference to the object. An interesting feature of Git is that these ids are _universal_: unlike a typical Java implementation, two objects with exactly the same content will have the same id on all systems \(i.e. my computer, your computer, and anyone else’s computer will compute this same exact id\). In the case of blobs, “same content” means the same file contents. In the case of commits, it means the same metadata, the same mapping of names to references, and the same parent reference. The objects in a repository are thus said to be _content addressable_.

Both Git and Gitlet accomplish this the same way: by using a _cryptographic hash function_ called SHA-1 \(Secure Hash 1\), which produces a 160-bit integer hash from any sequence of bytes. Cryptographic hash functions have the property that it is extremely difficult to find two different byte streams with the same hash value \(or indeed to find _any_ byte stream given just its hash value\), so that essentially, we may assume that the probability that any two objects with different contents have the same SHA-1 hash value is 2\-160 or about 10\-48. Basically, we simply ignore the possibility of a hashing collision, so that the system has, in principle, a fundamental bug that in practice never occurs\!

Fortunately, there are library classes for computing SHA-1 values, so you won’t have to deal with the actual algorithm. All you have to do is to make sure that you correctly label all your objects. In particular, this involves

* Including all metadata and references when hashing a commit.
* Distinguishing somehow between hashes for commits and hashes for blobs. A good way of doing this involves a well-thought out directory structure within the `.gitlet` directory. Another way to do so is to hash in an extra word for each object that has one value for blobs and another for commits.

By the way, the SHA-1 hash value, rendered as a 40-character hexadecimal string, makes a convenient file name for storing your data in your `.gitlet` directory \(more on that below\). It also gives you a convenient way to compare two files \(blobs\) to see if they have the same contents: if their SHA-1s are the same, we simply assume the files are the same.

For remotes \(like `skeleton` which we’ve been using all semester\), we’ll simply use other Gitlet repositories. Pushing simply means copying all commits and blobs that the remote repository does not yet have to the remote repository, and resetting a branch reference. Pulling is the same, but in the other direction. Remotes are extra credit in this project and not required for full credit.

Reading and writing your internal objects from and to files is actually pretty easy, thanks to Java’s _serialization_ facilities. The interface `java.io.Serializable` has no methods, but if a class implements it, then the Java runtime will automatically provide a way to convert to and from a stream of bytes, which you can then write to a file using the I/O class `java.io.ObjectOutputStream` and read back \(and deserialize\) with `java.io.ObjectInputStream`. The term “serialization” refers to the conversion from some arbitrary structure \(array, tree, graph, etc.\) to a serial sequence of bytes. You should have seen and gotten practice with serialization in lab 6. You’ll be using a very similar approach here, so do use your lab6 as a resource when it comes to persistence and serialization.

Here is a summary example of the structures discussed in this section. As you can see, each commit \(rectangle\) points to some blobs \(circles\), which contain file contents. The commits contain the file names and references to these blobs, as well as a parent link. These references, depicted as arrows, are represented in the `.gitlet` directory using their SHA-1 hash values \(the small hexadecimal numerals above the commits and below the blobs\). The newer commit contains an updated version of `wug1.txt`, but shares the same version of `wug2.txt` as the older commit. Your commit class will somehow store all of the information that this diagram shows: a careful selection of internal data structures will make the implementation easier or harder, so it behooves you to spend time planning and thinking about the best way to store everything.

![Two commits and their blobs](https://sp21.datastructur.es/materials/proj/proj2/image/commits-and-blobs.png)

## The Commands

#### init

* **Usage**: `java gitlet.Main init`

* **Description**: Creates a new Gitlet version-control system in the current directory. This system will automatically start with one commit: a commit that contains no files and has the commit message `initial commit` \(just like that, with no punctuation\). It will have a single branch: `master`, which initially points to this initial commit, and `master` will be the current branch. The timestamp for this initial commit will be 00:00:00 UTC, Thursday, 1 January 1970 in whatever format you choose for dates \(this is called “The \(Unix\) Epoch”, represented internally by the time 0.\) Since the initial commit in all repositories created by Gitlet will have exactly the same content, it follows that all repositories will automatically share this commit \(they will all have the same UID\) and all commits in all repositories will trace back to it.

#### add

* **Usage**: `java gitlet.Main add [file name]`

* **Description**: Adds a copy of the file as it currently exists to the _staging area_ \(see the description of the `commit` command\). For this reason, adding a file is also called _staging_ the file _for addition_. Staging an already-staged file overwrites the previous entry in the staging area with the new contents. The staging area should be somewhere in `.gitlet`. If the current working version of the file is identical to the version in the current commit, do not stage it to be added, and remove it from the staging area if it is already there \(as can happen when a file is changed, added, and then changed back to it’s original version\). The file will no longer be staged for removal \(see `gitlet rm`\), if it was at the time of the command.

#### commit

* **Usage**: `java gitlet.Main commit [message]`

* **Description**: Saves a snapshot of tracked files in the current commit and staging area so they can be restored at a later time, creating a new commit. The commit is said to be _tracking_ the saved files. By default, each commit’s snapshot of files will be exactly the same as its parent commit’s snapshot of files; it will keep versions of files exactly as they are, and not update them. A commit will only update the contents of files it is tracking that have been staged for addition at the time of commit, in which case the commit will now include the version of the file that was staged instead of the version it got from its parent. A commit will save and start tracking any files that were staged for addition but weren’t tracked by its parent. Finally, files tracked in the current commit may be untracked in the new commit as a result being _staged for removal_ by the `rm` command \(below\).

  The bottom line: By default a commit has the same file contents as its parent. Files staged for addition and removal are the updates to the commit. Of course, the date \(and likely the mesage\) will also different from the parent.

  Some additional points about commit:

  * The staging area is cleared after a commit.

  * The commit command never adds, changes, or removes files in the working directory \(other than those in the `.gitlet` directory\). The `rm` command _will_ remove such files, as well as staging them for removal, so that they will be untracked after a `commit`.

  * Any changes made to files after staging for addition or removal are ignored by the `commit` command, which _only_ modifies the contents of the `.gitlet` directory. For example, if you remove a tracked file using the Unix `rm` command \(rather than Gitlet’s command of the same name\), it has no effect on the next commit, which will still contain the \(now deleted\) version of the file.

  * After the commit command, the new commit is added as a new node in the commit tree.

  * The commit just made becomes the “current commit”, and the head pointer now points to it. The previous head commit is this commit’s parent commit.

  * Each commit should contain the date and time it was made.

  * Each commit has a log message associated with it that describes the changes to the files in the commit. This is specified by the user. The entire message should take up only one entry in the array `args` that is passed to `main`. To include multiword messages, you’ll have to surround them in quotes.

  * Each commit is identified by its SHA-1 id, which must include the file \(blob\) references of its files, parent reference, log message, and commit time.

![Before and after commit](https://sp21.datastructur.es/materials/proj/proj2/image/before_and_after_commit.png)

#### rm

* **Usage**: `java gitlet.Main rm [file name]`

* **Description**: Unstage the file if it is currently staged for addition. If the file is tracked in the current commit, stage it for removal and remove the file from the working directory if the user has not already done so \(do _not_ remove it unless it is tracked in the current commit\).

#### log

* **Usage**: `java gitlet.Main log`

* **Description**: Starting at the current head commit, display information about each commit backwards along the commit tree until the initial commit, following the first parent commit links, ignoring any second parents found in merge commits. \(In regular Git, this is what you get with `git log \--first-parent`\). This set of commit nodes is called the commit’s _history_. For every node in this history, the information it should display is the commit id, the time the commit was made, and the commit message. Here is an example of the _exact_ format it should follow:

```
===
commit a0da1ea5a15ab613bf9961fd86f010cf74c7ee48
Date: Thu Nov 9 20:00:05 2017 -0800
A commit message.

===
commit 3e8bf1d794ca2e9ef8a4007275acf3751c7170ff
Date: Thu Nov 9 17:01:33 2017 -0800
Another commit message.

===
commit e881c9575d180a215d1a636545b8fd9abfb1d2bb
Date: Wed Dec 31 16:00:00 1969 -0800
initial commit

```

There is a `===` before each commit and an empty line after it. As in real Git, each entry displays the unique SHA-1 id of the commit object. The timestamps displayed in the commits reflect the current timezone, not UTC; as a result, the timestamp for the initial commit does not read Thursday, January 1st, 1970, 00:00:00, but rather the equivalent Pacific Standard Time. Your timezone might be different depending on where you live, and that’s fine.

Display commits with the most recent at the top. By the way, you’ll find that the Java classes `java.util.Date` and `java.util.Formatter` are useful for getting and formatting times. Look into them instead of trying to construct it manually yourself\!

Of course, the SHA1 identifiers are going to be different, so don’t worry about those. Our tests will ensure that you have something that “looks like” a SHA1 identifier \(more on that in the testing section below\).

For merge commits \(those that have two parent commits\), add a line just below the first, as in

```
===
commit 3e8bf1d794ca2e9ef8a4007275acf3751c7170ff
Merge: 4975af1 2c1ead1
Date: Sat Nov 11 12:30:00 2017 -0800
Merged development into master.

```

where the two hexadecimal numerals following “Merge:” consist of the first seven digits of the first and second parents’ commit ids, in that order. The first parent is the branch you were on when you did the merge; the second is that of the merged-in branch. This is as in regular Git.

Here’s a picture of the history of a particular commit. If the current branch’s head pointer happened to be pointing to that commit, log would print out information about the circled commits:

![History](https://sp21.datastructur.es/materials/proj/proj2/image/history.png)

The history ignores other branches and the future. Now that we have the concept of history, let’s refine what we said earlier about the commit tree being immutable. It is immutable precisely in the sense that _the history of a commit with a particular id may never change, ever_. If you think of the commit tree as nothing more than a collection of histories, then what we’re really saying is that each history is immutable.

#### global-log

* **Usage**: `java gitlet.Main global-log`

* **Description**: Like log, except displays information about all commits ever made. The order of the commits does not matter. Hint: there is a useful method in `gitlet.Utils` that will help you iterate over files within a directory.

#### find

* **Usage**: `java gitlet.Main find [commit message]`

* **Description**: Prints out the ids of all commits that have the given commit message, one per line. If there are multiple such commits, it prints the ids out on separate lines. The commit message is a single operand; to indicate a multiword message, put the operand in quotation marks, as for the `commit` command below. Hint: the hint for this command is the same as the one for `global-log`.

#### status

* **Usage**: `java gitlet.Main status`

* **Description**: Displays what branches currently exist, and marks the current branch with a `*`. Also displays what files have been staged for addition or removal. An example of the _exact_ format it should follow is as follows.

  ```
  === Branches ===
  *master
  other-branch
  
  === Staged Files ===
  wug.txt
  wug2.txt
  
  === Removed Files ===
  goodbye.txt
  
  === Modifications Not Staged For Commit ===
  junk.txt (deleted)
  wug3.txt (modified)
  
  === Untracked Files ===
  random.stuff
  
  ```

  The last two sections \(modifications not staged and untracked files\) are extra credit, worth 32 points. Feel free to leave them blank \(leaving just the headers\).

  There is an empty line between sections, and the entire status ends in an empty line as well. Entries should be listed in lexicographic order, using the Java string-comparison order \(the asterisk doesn’t count\). A file in the working directory is “modified but not staged” if it is

  * Tracked in the current commit, changed in the working directory, but not staged; or
  * Staged for addition, but with different contents than in the working directory; or
  * Staged for addition, but deleted in the working directory; or
  * Not staged for removal, but tracked in the current commit and deleted from the working directory.

  The final category \(“Untracked Files”\) is for files present in the working directory but neither staged for addition nor tracked. This includes files that have been staged for removal, but then re-created without Gitlet’s knowledge. Ignore any subdirectories that may have been introduced, since Gitlet does not deal with them.

#### checkout

Checkout is a kind of general command that can do a few different things depending on what its arguments are. There are 3 possible use cases. In each section below, you’ll see 3 numbered points. Each corresponds to the respective usage of checkout.

* **Usages**:

    1.  `java gitlet.Main checkout \-- [file name]`

    2.  `java gitlet.Main checkout [commit id] \-- [file name]`

    3.  `java gitlet.Main checkout [branch name]`

* **Descriptions**:

    1.  Takes the version of the file as it exists in the head commit and puts it in the working directory, overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.

    2.  Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory, overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.

    3.  Takes all files in the commit at the head of the given branch, and puts them in the working directory, overwriting the versions of the files that are already there if they exist. Also, at the end of this command, the given branch will now be considered the current branch \(HEAD\). Any files that are tracked in the current branch but are not present in the checked-out branch are deleted. The staging area is cleared, unless the checked-out branch is the current branch \(see **Failure cases** below\).

#### branch

* **Usage**: `java gitlet.Main branch [branch name]`

* **Description**: Creates a new branch with the given name, and points it at the current head commit. A branch is nothing more than a name for a reference \(a SHA-1 identifier\) to a commit node. This command does NOT immediately switch to the newly created branch \(just as in real Git\). Before you ever call branch, your code should be running with a default branch called “master”.

#### rm-branch

* **Usage**: `java gitlet.Main rm-branch [branch name]`

* **Description**: Deletes the branch with the given name. This only means to delete the pointer associated with the branch; it does not mean to delete all commits that were created under the branch, or anything like that.

#### reset

* **Usage**: `java gitlet.Main reset [commit id]`

* **Description**: Checks out all the files tracked by the given commit. Removes tracked files that are not present in that commit. Also moves the current branch’s head to that commit node. See the intro for an example of what happens to the head pointer after using reset. The `[commit id]` may be abbreviated as for `checkout`. The staging area is cleared. The command is essentially `checkout` of an arbitrary commit that also changes the current branch head.

#### merge

* **Usage**: `java gitlet.Main merge [branch name]`

* **Description**: Merges files from the given branch into the current branch. This method is a bit complicated, so here’s a more detailed description:

  * First consider what we call the **split point** of the current branch and the given branch. For example, if `master` is the current branch and `branch` is the given branch: ![Split point](https://sp21.datastructur.es/materials/proj/proj2/image/split_point.png) The split point is a _latest common ancestor_ of the current and given branch heads: - A _common ancestor_ is a commit to which there is a path \(of 0 or more parent pointers\) from both branch heads. - A _latest_ common ancestor is a common ancestor that is not an ancestor of any other common ancestor. For example, although the leftmost commit in the diagram above is a common ancestor of `master` and `branch`, it is also an ancestor of the commit immediately to its right, so it is not a latest common ancestor. If the split point _is_ the same commit as the given branch, then we do nothing; the merge is complete, and the operation ends with the message `Given branch is an ancestor of the current branch.` If the split point is the current branch, then the effect is to check out the given branch, and the operation ends after printing the message `Current branch fast-forwarded.` Otherwise, we continue with the steps below.

    1.  Any files that have been _modified_ in the given branch since the split point, but not modified in the current branch since the split point should be changed to their versions in the given branch \(checked out from the commit at the front of the given branch\). These files should then all be automatically staged. To clarify, if a file is “modified in the given branch since the split point” this means the version of the file as it exists in the commit at the front of the given branch has different content from the version of the file at the split point. Remember: blobs are content addressable\!

    2.  Any files that have been modified in the current branch but not in the given branch since the split point should stay as they are.

    3.  Any files that have been modified in both the current and given branch in the same way \(i.e., both files now have the same content or were both removed\) are left unchanged by the merge. If a file was removed from both the current and given branch, but a file of the same name is present in the working directory, it is left alone and continues to be absent \(not tracked nor staged\) in the merge.

    4.  Any files that were not present at the split point and are present only in the current branch should remain as they are.

    5.  Any files that were not present at the split point and are present only in the given branch should be checked out and staged.

    6.  Any files present at the split point, unmodified in the current branch, and absent in the given branch should be removed \(and untracked\).

    7.  Any files present at the split point, unmodified in the given branch, and absent in the current branch should remain absent.

    8.  Any files modified in different ways in the current and given branches are _in conflict_. “Modified in different ways” can mean that the contents of both are changed and different from other, or the contents of one are changed and the other file is deleted, or the file was absent at the split point and has different contents in the given and current branches. In this case, replace the contents of the conflicted file with

```
<<<<<<< HEAD
contents of file in current branch
=======
contents of file in given branch
>>>>>>>
```

\(replacing “contents of…” with the indicated file’s contents\) and stage the result. Treat a deleted file in a branch as an empty file. Use straight concatenation here. In the case of a file with no newline at the end, you might well end up with something like this:

```
<<<<<<< HEAD
contents of file in current branch=======
contents of file in given branch>>>>>>>
```

This is fine; people who produce non-standard, pathological files because they don’t know the difference between a line terminator and a line separator deserve what they get.

Once files have been updated according to the above, and the split point was not the current branch or the given branch, merge automatically commits with the log message `Merged [given branch name] into [current branch name].` Then, if the merge encountered a conflict, print the message `Encountered a merge conflict.` on the terminal \(not the log\). Merge commits differ from other commits: they record as parents both the head of the current branch \(called the _first parent_\) and the head of the branch given on the command line to be merged in.

A video walkthrough of this command can be found [here](https://www.youtube.com/watch?v=JR3OYCMv9b4&t=929s).

By the way, we hope you’ve noticed that the set of commits has progressed from a simple sequence to a tree and now, finally, to a full directed acyclic graph.

#### add-remote

* **Usage**: `java gitlet.Main add-remote [remote name] [name of remote directory]/.gitlet`

* **Description**: Saves the given login information under the given remote name. Attempts to push or pull from the given remote name will then attempt to use this `.gitlet` directory. By writing, e.g., java gitlet.Main add-remote other ../testing/otherdir/.gitlet you can provide tests of remotes that will work from all locations \(on your home machine or within the grading program’s software\). Always use forward slashes in these commands. Have your program convert all the forward slashes into the path separator character \(forward slash on Unix and backslash on Windows\). Java helpfully defines the class variable `java.io.File.separator` as this character.

#### rm-remote

* **Usage**: `java gitlet.Main rm-remote [remote name]`

* **Description**: Remove information associated with the given remote name. The idea here is that if you ever wanted to change a remote that you added, you would have to first remove it and then re-add it.

#### push

* **Usage**: `java gitlet.Main push [remote name] [remote branch name]`

* **Description**: Attempts to append the current branch’s commits to the end of the given branch at the given remote. Details:

  This command only works if the remote branch’s head is in the history of the current local head, which means that the local branch contains some commits in the future of the remote branch. In this case, append the future commits to the remote branch. Then, the remote should reset to the front of the appended commits \(so its head will be the same as the local head\). This is called fast-forwarding.

  If the Gitlet system on the remote machine exists but does not have the input branch, then simply add the branch to the remote Gitlet.

#### fetch

* **Usage**: `java gitlet.Main fetch [remote name] [remote branch name]`

* **Description**: Brings down commits from the remote Gitlet repository into the local Gitlet repository. Basically, this copies all commits and blobs from the given branch in the remote repository \(that are not already in the current repository\) into a branch named `[remote name]/[remote branch name]` in the local `.gitlet` \(just as in real Git\), changing `[remote name]/[remote branch name]` to point to the head commit \(thus copying the contents of the branch from the remote repository to the current one\). This branch is created in the local repository if it did not previously exist.


#### pull

* **Usage**: `java gitlet.Main pull [remote name] [remote branch name]`

* **Description**: Fetches branch `[remote name]/[remote branch name]` as for the `fetch` command, and then merges that fetch into the current branch.

This project was created by Joseph Moghadam. Modifications for Fall 2015, Fall 2017, and Fall 2019 by Paul Hilfinger.
