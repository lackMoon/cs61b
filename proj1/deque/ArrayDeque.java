package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {

    private static final int INIT_CAPACITY = 8;

    private int capacity;
    private int size;
    private int nextFirst;
    private int nextLast;
    private T[] items;

    public ArrayDeque() {
        capacity = INIT_CAPACITY;
        items = (T[]) new Object[capacity];
        size = 0;
        nextFirst = capacity - 1;
        nextLast = 0;
    }

    private double getUsage() {
        return (double) size / (double) capacity;
    }

    private int getPrevIndex(int index) {
        return index - 1 < 0 ? capacity - 1 : index - 1;
    }

    private int getNextIndex(int index) {
        return index + 1 == capacity ? 0 : index + 1;
    }

    private void arrayCopy(T[] src, T[] dest) {
        int startIndex = getNextIndex(nextFirst);
        int endIndex = getPrevIndex(nextLast);
        if (startIndex <= endIndex) {
            System.arraycopy(src, startIndex, dest, 0, size);
        } else {
            int len = capacity - startIndex;
            System.arraycopy(src, startIndex, dest, 0, len);
            System.arraycopy(src, 0, dest, len, size - len);
        }
    }

    private void resize(int newSize) {
        T[] oldItems = items;
        items = (T[]) new Object[newSize];
        arrayCopy(oldItems, items);
        capacity = newSize;
        nextFirst = capacity - 1;
        nextLast = size;
    }

    @Override
    public void addFirst(T item) {
        items[nextFirst] = item;
        nextFirst = getPrevIndex(nextFirst);
        size++;
        if (size == capacity) {
            resize(size << 1);
        }
    }

    @Override
    public void addLast(T item) {
        items[nextLast] = item;
        nextLast = getNextIndex(nextLast);
        size++;
        if (size == capacity) {
            resize(size << 1);
        }
    }

    @Override
    public int size() {
        return size;
    }


    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        nextFirst = getNextIndex(nextFirst);
        T delItem = items[nextFirst];
        items[nextFirst] = null;
        size--;
        if (size != 0 && getUsage() < 0.25) {
            resize(capacity >> 1);
        }
        return delItem;
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        nextLast = getPrevIndex(nextLast);
        T delItem = items[nextLast];
        items[nextLast] = null;
        size--;
        if (size != 0 && getUsage() < 0.25) {
            resize(capacity >> 1);
        }
        return delItem;
    }

    @Override
    public T get(int index) {
        if (index >= capacity || index < 0) {
            return null;
        }
        int virtualIndex = nextFirst + index + 1;
        int actualIndex = virtualIndex < capacity ? virtualIndex : virtualIndex - capacity;
        return items[actualIndex] == null ? null : items[actualIndex];
    }

    public void printDeque() {
        for (T item: this) {
            System.out.print(item);
            System.out.print(" ");
        }
        System.out.println();
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator<>();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Deque)) {
            return false;
        }
        Deque<T> deque = (Deque<T>) o;
        if (deque.size() != size) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (!get(i).equals(deque.get(i))) {
                return false;
            }
        }
        return true;
    }

    private class ArrayDequeIterator<T> implements Iterator<T> {

        private int index;

        private int count;
        ArrayDequeIterator() {
            index = getNextIndex(nextFirst);
            count = 0;
        }
        @Override
        public boolean hasNext() {
            return count < size;
        }

        @Override
        public T next() {
            T nextVaule = (T) items[index];
            index = getNextIndex(index);
            count++;
            return nextVaule;
        }

    }
}
