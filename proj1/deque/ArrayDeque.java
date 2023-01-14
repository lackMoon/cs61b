package deque;

public class ArrayDeque<T> implements Deque<T> {

    private int capacity = 8;
    private int size;
    private int nextFirst;
    private int nextLast;
    private T items[];

    public ArrayDeque(){
        items = (T[])new Object[capacity];
        size = 8;
        nextFirst = 0;
        nextLast = 0;
    }

    public double getUsage(){
        return (double)size/(double)capacity;
    }


    private void arrayCopy(T[] src,T[] dest){
        int oldCapacity=src.length;
        int startIndex =nextFirst+1==oldCapacity?0:nextFirst+1;
        int endIndex = nextLast-1<0?oldCapacity-1:nextLast-1;
        if(startIndex<=endIndex){
            System.arraycopy(src,startIndex,dest,0,size);
        }else {
            int len = oldCapacity-startIndex;
            System.arraycopy(src,startIndex,dest,0,len);
            System.arraycopy(src,0,dest,len,size-len);
        }
    }

    private void resize(int newSize){
        T[] oldItems = items;
        capacity = newSize;
        items = (T[])new Object[capacity];
        arrayCopy(oldItems,items);
        nextFirst = capacity-1;
        nextLast=size;
    }

    @Override
    public void addFirst(T item) {
        if(size==0){
            nextLast++;
        }
        items[nextFirst]=item;
        nextFirst=nextFirst-1<0?capacity-1:nextFirst-1;
        size++;
        if(size==capacity){
            resize(size<<1);
        }
    }

    @Override
    public void addLast(T item) {
        if(size==0){
            nextFirst=capacity-1;
        }
        items[nextLast]=item;
        nextLast=nextLast+1==capacity?0:nextLast+1;
        size++;
        if(size==capacity){
            resize(size<<1);
        }
    }

    @Override
    public boolean isEmpty() {
        return size==0?true:false;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        int firstIndex = nextFirst+1==capacity?0:nextFirst+1;
        int count=0;
        while (count<size){
            System.out.print(items[firstIndex]);
            System.out.print(" ");
            firstIndex = firstIndex+1==capacity?0:firstIndex+1;
            count++;
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if(size==0){
            return null;
        }
        nextFirst=nextFirst+1==capacity?0:nextFirst+1;
        T delItem=items[nextFirst];
        items[nextFirst]=null;
        size--;
        if(size!=0&& getUsage() < 0.25){
            resize(capacity>>1);
        }
        return delItem;
    }

    @Override
    public T removeLast() {
        if(size==0){
            return null;
        }
        nextLast=nextLast-1<0?capacity-1:nextLast-1;
        T delItem=items[nextLast];
        items[nextLast]=null;
        size--;
        if(size!=0&&getUsage()<0.25){
            resize(capacity>>1);
        }
        return delItem;
    }

    @Override
    public T get(int index) {
        if(index>capacity){
            return null;
        }
        return items[index-1]==null?null:items[index-1];
    }
}
