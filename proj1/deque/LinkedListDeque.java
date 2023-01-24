package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private LinkedNode<T> sentinel;

    private int size;

    private static class LinkedNode<T> {
        private T item;
        private LinkedNode<T> prev;
        private LinkedNode<T> next;

        LinkedNode(T item) {
            this.item = item;
            this.prev = null;
            this.next = null;
        }

        LinkedNode(T item, LinkedNode prev, LinkedNode next) {
            this.item = item;
            this.prev = prev;
            this.next = next;
        }
    }

    public LinkedListDeque() {
        sentinel = new LinkedNode(-1);
        size = 0;
    }

    private LinkedNode<T> addNode(T item) {
        LinkedNode<T> newNode;
        LinkedNode<T> firstNode = sentinel.next;
        if (firstNode == null) {
            newNode = new LinkedNode(item, null, null);
            newNode.prev = newNode;
            newNode.next = newNode;
            sentinel.next = newNode;
        } else {
            newNode = new LinkedNode(item, firstNode.prev, firstNode);
            firstNode.prev.next = newNode;
            firstNode.prev = newNode;
        }
        size++;
        return newNode;
    }

    private T removeNode(LinkedNode<T> currentNode) {
        T item = currentNode.item;
        if (size == 1) {
            sentinel.next = null;
        } else {
            currentNode.next.prev = currentNode.prev;
            currentNode.prev.next = currentNode.next;
            currentNode.prev = null;
            currentNode.next = null;
        }
        size--;
        return item;
    }
    @Override
    public void addFirst(T item) {
        sentinel.next = addNode(item);
    }

    @Override
    public void addLast(T item) {
        addNode(item);
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
        LinkedNode<T> firstNode = sentinel.next;
        sentinel.next = firstNode.next;
        return removeNode(firstNode);
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        LinkedNode<T> lastNode = sentinel.next.prev;
        return removeNode(lastNode);
    }

    @Override
    public T get(int index) {
        if (index > size || index < 0) {
            return null;
        }
        LinkedNode<T> currentNode = sentinel.next;
        while (index-- > 0) {
            currentNode = currentNode.next;
        }
        return currentNode.item;
    }

    //Same as get, but uses recursion.
    public T getRecursive(int index) {
        if (index > size) {
            return null;
        }
        LinkedNode<T> node = sentinel.next;
        return recursionOnLinkedList(node, index);
    }

    private T recursionOnLinkedList(LinkedNode<T> node, int index) {
        if (index == 0) {
            return node.item;
        }  else {
            return recursionOnLinkedList(node.next, --index);
        }
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
        return new LinkedListDequeIterator<T>();
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
            if (get(i) != deque.get(i)) {
                return false;
            }
        }
        return true;
    }

    private class LinkedListDequeIterator<T> implements Iterator<T> {

        private LinkedNode<T> currentNode;

        private int count;

        LinkedListDequeIterator() {
            currentNode = (LinkedNode<T>) sentinel.next;
            count = 0;
        }
        @Override
        public boolean hasNext() {
            return count < size;
        }

        @Override
        public T next() {
            T nextVaule = currentNode.item;
            currentNode = currentNode.next;
            count++;
            return nextVaule;
        }
    }
}
