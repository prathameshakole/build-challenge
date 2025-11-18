package com.a1;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for MyBlockingQueue verifying thread-safe blocking operations and FIFO behavior.
 */

public class MyBlockingQueueTest {
    
    private MyBlockingQueue<Integer> queue;
    private static final int CAPACITY = 5;
    
    @BeforeEach
    void setUp() {
        queue = new MyBlockingQueue<>(CAPACITY);
    }

    /**
     * Verifies basic put and take operations maintain FIFO ordering.
     */
    @Test
    void testPutAndTake() throws InterruptedException {
        queue.put(1);
        queue.put(2);
        assertEquals(1, queue.take());
        assertEquals(2, queue.take());
    }

    /**
     * Verifies a newly created queue is initially empty.
     */
    @Test
    void testQueueIsEmptyInitially() {
        assertTrue(queue.isEmpty());
    }

    /**
     * Verifies the queue correctly reports full status when at capacity.
     */
    @Test
    void testQueueIsFull() throws InterruptedException {
        for (int i = 0; i < CAPACITY; i++) {
            queue.put(i);
        }
        assertTrue(queue.isFull());
    }

    /**
     * Verifies put operation blocks when queue is full until space becomes available.
     */
    @Test
    void testPutBlocksWhenFull() throws InterruptedException {
        for (int i = 0; i < CAPACITY; i++) {
            queue.put(i);
        }
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger result = new AtomicInteger(-1);
        
        Thread thread = new Thread(() -> {
            try {
                latch.countDown();
                queue.put(999);
                result.set(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        thread.start();
        latch.await();

        Thread.sleep(100);

        assertEquals(-1, result.get());

        queue.take();
        thread.join(1000);
        assertTrue(result.get() > 0 || queue.size() == CAPACITY);
    }

    /**
     * Verifies take operation blocks when queue is empty until an item becomes available.
     */
    @Test
    void testTakeBlocksWhenEmpty() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger result = new AtomicInteger(-1);
        
        Thread thread = new Thread(() -> {
            try {
                latch.countDown();
                result.set(queue.take());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        thread.start();
        latch.await();

        Thread.sleep(100);

        assertEquals(-1, result.get());

        queue.put(42);
        thread.join(1000);
        assertEquals(42, result.get());
    }

    /**
     * Verifies thread safety with multiple concurrent producers and consumers operating on the queue.
     */
    @Test
    void testMultipleProducersAndConsumers() throws InterruptedException {
        int numProducers = 3;
        int numConsumers = 2;
        int itemsPerProducer = 10;
        
        List<Thread> producers = new ArrayList<>();
        List<Thread> consumers = new ArrayList<>();
        List<Integer> consumedItems = new ArrayList<>();
        Object lock = new Object();
        

        for (int i = 0; i < numConsumers; i++) {
            Thread consumer = new Thread(() -> {
                try {
                    for (int j = 0; j < (numProducers * itemsPerProducer) / numConsumers; j++) {
                        Integer item = queue.take();
                        synchronized (lock) {
                            consumedItems.add(item);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            consumers.add(consumer);
        }
        

        for (int i = 0; i < numProducers; i++) {
            final int producerId = i;
            Thread producer = new Thread(() -> {
                try {
                    for (int j = 0; j < itemsPerProducer; j++) {
                        queue.put(producerId * 100 + j);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            producers.add(producer);
        }

        consumers.forEach(Thread::start);
        producers.forEach(Thread::start);

        for (Thread producer : producers) {
            producer.join();
        }
        for (Thread consumer : consumers) {
            consumer.join();
        }

        assertEquals(numProducers * itemsPerProducer, consumedItems.size());
    }

    /**
     * Verifies the size method accurately reflects the current number of items in the queue.
     */
    @Test
    void testSize() throws InterruptedException {
        assertEquals(0, queue.size());
        queue.put(1);
        assertEquals(1, queue.size());
        queue.put(2);
        assertEquals(2, queue.size());
        queue.take();
        assertEquals(1, queue.size());
    }
}
