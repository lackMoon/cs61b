package deque;


public interface Deque<T> extends Iterable<T>{
    //Adds an item of type T to the front of the deque.
    public void addFirst(T item);

    //Adds an item of type T to the back of the deque.
    public void addLast(T item);

    //Returns true if deque is empty, false otherwise.
    public default boolean isEmpty() {
        return size() == 0;
    }

    //Returns the number of items in the deque.
    public int size();

    //Prints the items in the deque from first to last, separated by a space.
    public default void printDeque() {
        for (T item: this) {
            System.out.print(item);
            System.out.print(" ");
        }
        System.out.println();
    }

    //Removes and returns the item at the front of the deque. If no such item exists, returns null.
    public T removeFirst();

    //Removes and returns the item at the back of the deque. If no such item exists, returns null.
    public T removeLast();

    //Gets the item at the given index, where 0 is the front,1 is the next item, and so forth.
    // If no such item exists, returns null.
    public T get(int index);

    // Returns whether or not the parameter o is equal to the Deque.
    // o is considered equal if it is a Deque and if it contains the same contents in the same order.
    public boolean equals(Object o);
}
