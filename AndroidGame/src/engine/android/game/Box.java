package engine.android.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 游戏中的数据存储集合<br>
 * 功能：防止多线程并发操作引起异常
 * 
 * @author Daimon
 * @since 5/11/2012
 */
public final class Box<E> implements List<E> {

    private final LinkedList<E> origin = new LinkedList<E>();               // 原始数据
    private final ArrayList<E> buffer = new ArrayList<E>();                 // 缓存数据

    private volatile boolean isChanged;                                     // 数据是否改变

    /** Daimon:ReentrantReadWriteLock **/
    private final ReentrantReadWriteLock lock                               // 数据存取锁
    = new ReentrantReadWriteLock();
    private final Lock r = lock.readLock();
    private final Lock w = lock.writeLock();

    @Override
    public void add(int location, E object) {
        w.lock();
        try {
            isChanged = true;
            origin.add(location, object);
        } finally {
            w.unlock();
        }
    }

    @Override
    public boolean add(E object) {
        w.lock();
        try {
            isChanged = true;
            return origin.add(object);
        } finally {
            w.unlock();
        }
    }

    @Override
    public boolean addAll(int location, Collection<? extends E> collection) {
        w.lock();
        try {
            isChanged = true;
            return origin.addAll(location, collection);
        } finally {
            w.unlock();
        }
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        w.lock();
        try {
            isChanged = true;
            return origin.addAll(collection);
        } finally {
            w.unlock();
        }
    }

    @Override
    public void clear() {
        w.lock();
        try {
            buffer.clear();
            origin.clear();
            isChanged = false;
        } finally {
            w.unlock();
        }
    }

    @Override
    public boolean contains(Object object) {
        return getList().contains(object);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return getList().containsAll(collection);
    }

    @Override
    public E get(int location) {
        return getList().get(location);
    }

    @Override
    public int indexOf(Object object) {
        return getList().indexOf(object);
    }

    @Override
    public boolean isEmpty() {
        return getList().isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return getList().iterator();
    }

    @Override
    public int lastIndexOf(Object object) {
        return getList().lastIndexOf(object);
    }

    @Override
    public ListIterator<E> listIterator() {
        return getList().listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int location) {
        return getList().listIterator(location);
    }

    @Override
    public E remove(int location) {
        w.lock();
        try {
            isChanged = true;
            return origin.remove(location);
        } finally {
            w.unlock();
        }
    }

    @Override
    public boolean remove(Object object) {
        w.lock();
        try {
            isChanged = true;
            return origin.remove(object);
        } finally {
            w.unlock();
        }
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        w.lock();
        try {
            isChanged = true;
            return origin.removeAll(collection);
        } finally {
            w.unlock();
        }
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        w.lock();
        try {
            isChanged = true;
            return origin.retainAll(collection);
        } finally {
            w.unlock();
        }
    }

    @Override
    public E set(int location, E object) {
        w.lock();
        try {
            isChanged = true;
            return origin.set(location, object);
        } finally {
            w.unlock();
        }
    }

    @Override
    public int size() {
        return getList().size();
    }

    @Override
    public List<E> subList(int start, int end) {
        return getList().subList(start, end);
    }

    @Override
    public Object[] toArray() {
        return getList().toArray();
    }

    @Override
    public <T> T[] toArray(T[] array) {
        return getList().toArray(array);
    }

    /**
     * 获取数据集合
     */
    private List<E> getList() {
        r.lock();
        try {
            if (isChanged)
            {
                // 数据有变化

                // Must release read lock before acquiring write lock
                r.unlock();
                w.lock();

                // Recheck state because another thread might have acquired
                // write lock and changed state before we did.
                if (isChanged)
                {
                    buffer.clear();
                    buffer.addAll(origin);
                    isChanged = false;
                }

                // Downgrade by acquiring read lock before releasing write lock
                r.lock();
                w.unlock();
            }

            return buffer;
        } finally {
            r.unlock();
        }
    }
}