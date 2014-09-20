package judge.tool;

import java.util.Date;

/**
 * LRU list with fixed capacity. Most recent used element is place at the front.
 * 
 * @author Isun
 *
 * @param <T> Element type
 */
public class LRUList<T> {
    
    private T[] values;
    
    private Date lastVisitTime;
    
    
    public LRUList(int capacity) {
        if (capacity < 1) {
            throw new RuntimeException("LRUList capacity should be positive.");
        }
        values = (T[])new Object[capacity];
        lastVisitTime = new Date();
    }
    
    synchronized public Date getLastVisitTime() {
        return lastVisitTime;
    }
    
    synchronized public boolean contains(T e) {
        lastVisitTime = new Date();
        if (e == null) {
            return false;
        }
        for (T element : values) {
            if (e.equals(element)) {
                return true;
            }
        }
        return false;
    }
    
    synchronized public void put(T e) {
        lastVisitTime = new Date();
        if (e == null) {
            throw new RuntimeException("Null element is not supported.");
        }
        
        T[] tmp = (T[])new Object[values.length];
        int index = 0;
        tmp[index++] = e;
        for (T v : values) {
            if (index < values.length && !e.equals(v)) {
                tmp[index++] = v; 
            }
        }
        values = tmp;
    }
    
    synchronized public boolean remove(T e) {
        lastVisitTime = new Date();
        if (e == null) {
            throw new RuntimeException("Null element is not supported.");
        }
        boolean found = false;
        
        T[] tmp = (T[])new Object[values.length];
        int index = 0;
        for (T v : values) {
            if (!e.equals(v)) {
                tmp[index++] = v; 
            } else {
                found = true;
            }
        }
        values = tmp;
        
        return found;
    }
}
