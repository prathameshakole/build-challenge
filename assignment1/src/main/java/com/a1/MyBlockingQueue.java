package com.a1;

import java.util.LinkedList;
import java.util.Queue;

/**
 *  This class represents a thread-safe blocking queue with fixed capacity that supports blocking
 *  put and take operations. Uses wait/notify mechanism to block producers when full and consumers
 *  when empty.
 *
 * @param <T> the type of elements held in this queue
 */
public class MyBlockingQueue<T> {
    private final Queue<T> queue;
    private final int capacity;
    private final Object lock = new Object();
    
    public MyBlockingQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.capacity = capacity;
        this.queue = new LinkedList<>();
    }

    /**
     * Adds an item to the queue, blocking if the queue is full until space becomes available.
     */
    public void put(T item) throws InterruptedException {
        synchronized (lock) {
            while (queue.size() >= capacity) {
                lock.wait();
            }
            queue.offer(item);
            lock.notifyAll();
        }
    }

    /**
     * Removes and returns an item from the queue, blocking if empty until an item is available.
     */
     public T take() throws InterruptedException {
        synchronized (lock) {
            while (queue.isEmpty()) {
                lock.wait();
            }
            T item = queue.poll();
            lock.notifyAll();
            return item;
        }
    }

    /**
     * Returns true if the queue contains no elements.
     */
    public boolean isEmpty() {
        synchronized (lock) {
            return queue.isEmpty();
        }
    }

    /**
     * Returns true if the queue has reached its capacity.
     */
    public boolean isFull() {
        synchronized (lock) {
            return queue.size() >= capacity;
        }
    }

    /**
     * Returns the current number of elements in the queue.
     */
    public int size() {
        synchronized (lock) {
            return queue.size();
        }
    }
}