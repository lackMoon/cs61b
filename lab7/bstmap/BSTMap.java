package bstmap;


import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    private static final int PREDECESSOR = 0;
    private static final int SUCCESSOR = 1;

    private BSTNode rootNode;

    private int size;

    private class BSTNode {
        private K key;
        private V value;
        private BSTNode left;
        private BSTNode right;

        public BSTNode(K key, V value) {
            this.key = key;
            this.value = value;
            this.left = null;
            this.right = null;
        }

        public boolean greaterThan(K targetkey) {
            return targetkey.compareTo(key) < 0;
        }

        public boolean lessThan(K targetkey) {
            return targetkey.compareTo(key) > 0;
        }

        public BSTNode delete(int nodeType) {
            if (nodeType == PREDECESSOR && this.left != null) {
                deletePredecessor(this.left);
            } else if (nodeType == SUCCESSOR && this.right != null) {
                deleteSucessor(this.right);
            }
            return this;
        }
        public BSTNode deletePredecessor(BSTNode node) {
            if (node.right == null) {
                this.key = node.key;
                this.value = node.value;
                return node.left;
            } else {
                node.right = deletePredecessor(node.right);
            }
            return node;
        }

        public BSTNode deleteSucessor(BSTNode node) {
            if (node.left == null) {
                this.key = node.key;
                this.value = node.value;
                return node.right;
            } else {
                node.left = deleteSucessor(node.right);
            }
            return node;
        }
    }
    @Override
    public void clear() {
        this.rootNode = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        return containsKey(rootNode, key);
    }

    private boolean containsKey(BSTNode node, K key) {
        if (node == null) {
            return false;
        }
        if (node.greaterThan(key)) {
            return containsKey(node.left, key);
        } else if (node.lessThan(key)) {
            return containsKey(node.right, key);
        } else {
            return true;
        }
    }
    @Override
    public V get(K key) {
        if (key == null) {
            return null;
        }
        return get(rootNode, key);
    }

    private V get(BSTNode node, K key) {
        if (node == null) {
            return null;
        }
        if (node.greaterThan(key)) {
            return get(node.left, key);
        } else if (node.lessThan(key)) {
            return get(node.right, key);
        } else {
            return node.value;
        }
    }
    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        rootNode = put(rootNode, key, value);
    }

    private BSTNode put(BSTNode node, K key, V value) {
        if (node == null) {
            size++;
            return new BSTNode(key, value);
        }
        if (node.greaterThan(key)) {
            node.left = put(node.left, key, value);
        } else if (node.lessThan(key)) {
            node.right = put(node.right, key, value);
        } else {
            node.value = value;
        }
        return node;
    }
    @Override
    public Set<K> keySet() {
        Set<K> keys = new TreeSet<>();
        return key(rootNode, keys);
    }

    private Set<K> key(BSTNode node, Set<K> set) {
        if (node == null) {
            return null;
        }
        if (node.left != null) {
            key(node.left, set);
        }
        set.add(node.key);
        if (node.right != null) {
            key(node.right, set);
        }
        return set;
    }
    @Override
    public V remove(K key) {
        if (key == null) {
            return null;
        }
        V value = get(key);
        if (value != null) {
            rootNode = remove(rootNode, key);
            size--;
        }
        return value;
    }

    @Override
    public V remove(K key, V value) {
        if (key == null) {
            return null;
        }
        V mapValue = get(key);
        if (mapValue != null && mapValue.equals(value)) {
            rootNode = remove(rootNode, key);
            size--;
            return mapValue;
        } else {
            return null;
        }
    }

    private BSTNode remove(BSTNode node, K key) {
        if (node == null) {
            return null;
        }
        if (node.greaterThan(key)) {
            node.left = remove(node.left, key);
        } else if (node.lessThan(key)) {
            node.right = remove(node.right, key);
        } else {
            return deleteNode(node, PREDECESSOR);
        }
        return node;
    }

    private BSTNode deleteNode(BSTNode node, int nodeType) {
        if (node.left == null && node.right == null) {
            return null;
        } else if (node.left == null || node.right == null) {
            return node.left == null ? node.right : node.left;
        } else {
            node.delete(nodeType);
            return node;
        }
    }

    @Override
    public Iterator<K> iterator() {
        return keySet().iterator();
    }

    public void printInOrder() {
        Iterator<K> iterator = iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }

}
