package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;

    private int items;
    private int size;
    private double maxLoad;

    private static final int DEFAULT_SIZE = 16;
    private static final double DEFAULT_LOAD_FACTOR = 0.75;


    /** Constructors */
    public MyHashMap() {
        this(DEFAULT_SIZE, DEFAULT_LOAD_FACTOR);
    }

    public MyHashMap(int initialSize) {
        this(initialSize, DEFAULT_LOAD_FACTOR);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        if (initialSize < 1 || maxLoad <= 0.0) {
            throw new IllegalArgumentException();
        }
        this.size = initialSize;
        this.maxLoad = maxLoad;
        initBuckets(size);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new ArrayList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        return new Collection[tableSize];
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    private void initBuckets(int targetSize) {
        buckets = createTable(targetSize);
        for (int i = 0; i < targetSize; i++) {
            buckets[i] = createBucket();
        }
    }

    private void resize(int targetSize) {
        Set<Node> nodeSet = nodeSet();
        this.size = targetSize;
        this.items = 0;
        initBuckets(targetSize);
        for (Node node : nodeSet) {
            put(node.key, node.value);
        }
    }

    private Set<Node> nodeSet() {
        Set<Node> nodeSet = new HashSet();
        for (int i = 0; i < size; i++) {
            for (Node node : buckets[i]) {
                nodeSet.add(node);
            }
        }
        return nodeSet;
    }
    private int hash(K key) {
        return key == null ? -1 : Math.floorMod(key.hashCode(), size);
    }

    private Collection<Node> findBucket(K key) {
        return buckets[hash(key)];
    }

    private Node find(K key) {
        Collection<Node> bucket = findBucket(key);
        if (bucket != null) {
            for (Node node : bucket) {
                if (node.key.equals(key)) {
                    return node;
                }
            }
        }
        return null;
    }

    @Override
    public void clear() {
        this.items = 0;
        for (int i = 0; i < size; i++) {
            buckets[i] = createBucket();
        }
    }

    @Override
    public boolean containsKey(K key) {
        return find(key) != null;
    }

    @Override
    public V get(K key) {
        Node node = find(key);
        return node == null ? null : node.value;
    }

    @Override
    public int size() {
        return items;
    }

    @Override
    public void put(K key, V value) {
        Collection<Node> bucket = findBucket(key);
        for (Node node : bucket) {
            if (node.key.equals(key)) {
                node.value = value;
                return;
            }
        }
        bucket.add(createNode(key, value));
        items++;
        if (items / size > maxLoad) {
            resize(size << 1);
        }
    }

    @Override
    public Set<K> keySet() {
        Set<K> keySet = new HashSet();
        Set<Node> nodeSet = nodeSet();
        for (Node node: nodeSet) {
            keySet.add(node.key);
        }
        return keySet;
    }

    @Override
    public V remove(K key) {
        Collection<Node> bucket = findBucket(key);
        for (Node node : bucket) {
            if (node.key.equals(key)) {
                V value = node.value;
                bucket.remove(node);
                items--;
                return value;
            }
        }
        return null;
    }

    @Override
    public V remove(K key, V value) {
        Collection<Node> bucket = findBucket(key);
        for (Node node : bucket) {
            if (node.key.equals(key) && node.value.equals(value)) {
                bucket.remove(node);
                items--;
                return value;
            }
        }
        return null;
    }

    @Override
    public Iterator<K> iterator() {
        return keySet().iterator();
    }

}
