package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>,Iterable<T> {
    private LinkedNode<T> sentinel;
    private int size = 0;

    private static class LinkedNode<T> {
        private T item;
        private LinkedNode<T> prev;
        private LinkedNode<T> next;

        public LinkedNode(T item){
            this.item=item;
            this.prev=null;
            this.next=null;
        }

        public LinkedNode(T item,LinkedNode prev,LinkedNode next){
            this.item = item;
            this.prev = prev;
            this.next = next;
        }
    }

    public LinkedListDeque(){
        sentinel = new LinkedNode(-1);
    }
    @Override
    public void addFirst(T item) {
        if(sentinel.next==null){
            LinkedNode<T> newNode=new LinkedNode(item,sentinel,sentinel);
            sentinel.next=newNode;
            sentinel.prev=newNode;
        }else {
            sentinel.next=new LinkedNode(item,sentinel,sentinel.next);
        }
        size++;
    }

    @Override
    public void addLast(T item) {
        if(sentinel.prev==null){
            LinkedNode<T> newNode=new LinkedNode(item,sentinel,sentinel);
            sentinel.next=newNode;
            sentinel.prev=newNode;
        }else {
            sentinel.prev.next=new LinkedNode(item,sentinel.prev,sentinel);
            sentinel.prev = sentinel.prev.next;
        }
        size++;
    }

    @Override
    public int size() {
        return size;
    }


    @Override
    public T removeFirst() {
        if (sentinel.next!=null){
            LinkedNode<T> firstNode = sentinel.next;
            if (firstNode.next!=sentinel){
                firstNode.next.prev=sentinel;
                sentinel.next = firstNode.next;
            }else {
                sentinel.prev=null;
                sentinel.next=null;
            }
            size--;
            return firstNode.item;
        }else {
            return null;
        }
    }

    @Override
    public T removeLast() {
        if(sentinel.prev!=null){
            LinkedNode<T> lastNode = sentinel.prev;
            if(lastNode.prev!=sentinel){
                lastNode.prev.next = sentinel;
                sentinel.prev = lastNode.prev;
            }else {
                sentinel.prev = null;
                sentinel.next = null;
            }
            size--;
            return lastNode.item;
        }else {
            return null;
        }
    }

    @Override
    public T get(int index) {
        if (index > size) {
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
        LinkedNode<T> Node = sentinel.next;
        return recursionOnLinkedList(Node,index);
    }

    private T recursionOnLinkedList(LinkedNode<T> node,int index) {
        if (index == 0) {
            return node.item;
        }  else {
            return recursionOnLinkedList(node.next,--index);
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
        if (o instanceof Deque) {
            for (int i = 0; i < size; i++) {
                if (get(i) != ((Deque<T>) o).get(i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private class LinkedListDequeIterator<T> implements Iterator<T> {

        private LinkedNode<T> currentNode;

        public LinkedListDequeIterator() {
            currentNode = (LinkedNode<T>) sentinel.next;
        }
        @Override
        public boolean hasNext() {
            return currentNode != sentinel;
        }

        @Override
        public T next() {
            T nextVaule = currentNode.item;
            currentNode = currentNode.next;
            return currentNode.item;
        }
    }
}
