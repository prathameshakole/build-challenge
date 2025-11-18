package com.a1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * End-to-end integration tests for producer-consumer scenarios with various configurations and edge cases.
 */
public class EndToEndTest {
  @BeforeEach
  void setUp() {
    System.setOut(new java.io.PrintStream(new java.io.ByteArrayOutputStream()));
  }

  /**
   * Verifies a single producer and single consumer successfully transfer all items.
   */
  @Test
  @DisplayName("Scenario 1: Single Producer, Single Consumer")
  void testSingleProducerSingleConsumer() throws InterruptedException {
    MyBlockingQueue<Integer> queue = new MyBlockingQueue<>(10);
    List<Integer> source = Arrays.asList(1, 2, 3, 4, 5);
    List<Integer> destination = Collections.synchronizedList(new ArrayList<>());

    Producer producer = new Producer(queue, source, "P1");
    Consumer consumer = new Consumer(queue, destination, "C1", 5);

    Thread producerThread = new Thread(producer);
    Thread consumerThread = new Thread(consumer);

    consumerThread.start();
    producerThread.start();

    producerThread.join();
    consumerThread.join();

    assertEquals(5, producer.getItemsProduced());
    assertEquals(5, consumer.getItemsConsumed());
    assertEquals(5, destination.size());
    assertTrue(destination.containsAll(source));
    assertTrue(queue.isEmpty());
  }

  /**
   * Verifies multiple producers correctly feed a single consumer with all items transferred.
   */
  @Test
  @DisplayName("Scenario 2: Multiple Producers, Single Consumer")
  void testMultipleProducersSingleConsumer() throws InterruptedException {
    MyBlockingQueue<Integer> queue = new MyBlockingQueue<>(10);
    List<Integer> source1 = Arrays.asList(1, 2, 3, 4, 5);
    List<Integer> source2 = Arrays.asList(6, 7, 8, 9, 10);
    List<Integer> destination = Collections.synchronizedList(new ArrayList<>());

    Producer producer1 = new Producer(queue, source1, "P1");
    Producer producer2 = new Producer(queue, source2, "P2");
    Consumer consumer = new Consumer(queue, destination, "C1", 10);

    ExecutorService executor = Executors.newFixedThreadPool(3);
    executor.submit(producer1);
    executor.submit(producer2);
    executor.submit(consumer);

    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);

    assertEquals(5, producer1.getItemsProduced());
    assertEquals(5, producer2.getItemsProduced());
    assertEquals(10, consumer.getItemsConsumed());
    assertEquals(10, destination.size());
    assertTrue(queue.isEmpty());
  }

  /**
   * Verifies a single producer correctly distributes items to multiple consumers.
   */
  @Test
  @DisplayName("Scenario 3: Single Producer, Multiple Consumers")
  void testSingleProducerMultipleConsumers() throws InterruptedException {
    MyBlockingQueue<Integer> queue = new MyBlockingQueue<>(10);
    List<Integer> source = IntStream.rangeClosed(1, 10).boxed().collect(java.util.stream.Collectors.toList());
    List<Integer> destination = Collections.synchronizedList(new ArrayList<>());

    Producer producer = new Producer(queue, source, "P1");
    Consumer consumer1 = new Consumer(queue, destination, "C1", 5);
    Consumer consumer2 = new Consumer(queue, destination, "C2", 5);

    ExecutorService executor = Executors.newFixedThreadPool(3);
    executor.submit(producer);
    executor.submit(consumer1);
    executor.submit(consumer2);

    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);

    assertEquals(10, producer.getItemsProduced());
    assertEquals(5, consumer1.getItemsConsumed());
    assertEquals(5, consumer2.getItemsConsumed());
    assertEquals(10, destination.size());
    assertTrue(queue.isEmpty());
  }

  /**
   * Verifies balanced production and consumption with equal numbers of producers and consumers.
   */
  @Test
  @DisplayName("Scenario 4: Multiple Producers, Multiple Consumers (Balanced)")
  void testMultipleProducersMultipleConsumersBalanced() throws InterruptedException {
    MyBlockingQueue<Integer> queue = new MyBlockingQueue<>(5);
    List<Integer> source1 = Arrays.asList(1, 2, 3, 4, 5);
    List<Integer> source2 = Arrays.asList(6, 7, 8, 9, 10);
    List<Integer> destination = Collections.synchronizedList(new ArrayList<>());

    Producer producer1 = new Producer(queue, source1, "P1");
    Producer producer2 = new Producer(queue, source2, "P2");
    Consumer consumer1 = new Consumer(queue, destination, "C1", 5);
    Consumer consumer2 = new Consumer(queue, destination, "C2", 5);

    ExecutorService executor = Executors.newFixedThreadPool(4);
    executor.submit(producer1);
    executor.submit(producer2);
    executor.submit(consumer1);
    executor.submit(consumer2);

    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);

    assertEquals(5, producer1.getItemsProduced());
    assertEquals(5, producer2.getItemsProduced());
    assertEquals(5, consumer1.getItemsConsumed());
    assertEquals(5, consumer2.getItemsConsumed());
    assertEquals(10, destination.size());
    assertTrue(queue.isEmpty());
  }

  /**
   * Verifies correct behavior when there are more producers than consumers.
   */
  @Test
  @DisplayName("Scenario 5: More Producers Than Consumers")
  void testMoreProducersThanConsumers() throws InterruptedException {
    MyBlockingQueue<Integer> queue = new MyBlockingQueue<>(5);
    List<Integer> source1 = Arrays.asList(1, 2, 3);
    List<Integer> source2 = Arrays.asList(4, 5, 6);
    List<Integer> source3 = Arrays.asList(7, 8, 9);
    List<Integer> destination = Collections.synchronizedList(new ArrayList<>());

    Producer producer1 = new Producer(queue, source1, "P1");
    Producer producer2 = new Producer(queue, source2, "P2");
    Producer producer3 = new Producer(queue, source3, "P3");
    Consumer consumer1 = new Consumer(queue, destination, "C1", 5);
    Consumer consumer2 = new Consumer(queue, destination, "C2", 4);

    ExecutorService executor = Executors.newFixedThreadPool(5);
    executor.submit(producer1);
    executor.submit(producer2);
    executor.submit(producer3);
    executor.submit(consumer1);
    executor.submit(consumer2);

    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);

    assertEquals(3, producer1.getItemsProduced());
    assertEquals(3, producer2.getItemsProduced());
    assertEquals(3, producer3.getItemsProduced());
    assertEquals(5, consumer1.getItemsConsumed());
    assertEquals(4, consumer2.getItemsConsumed());
    assertEquals(9, destination.size());
    assertTrue(queue.isEmpty());
  }

  /**
   * Verifies correct behavior when there are more consumers than producers.
   */
  @Test
  @DisplayName("Scenario 6: More Consumers Than Producers")
  void testMoreConsumersThanProducers() throws InterruptedException {
    MyBlockingQueue<Integer> queue = new MyBlockingQueue<>(5);
    List<Integer> source1 = Arrays.asList(1, 2, 3, 4, 5);
    List<Integer> source2 = Arrays.asList(6, 7, 8, 9, 10);
    List<Integer> destination = Collections.synchronizedList(new ArrayList<>());

    Producer producer1 = new Producer(queue, source1, "P1");
    Producer producer2 = new Producer(queue, source2, "P2");
    Consumer consumer1 = new Consumer(queue, destination, "C1", 4);
    Consumer consumer2 = new Consumer(queue, destination, "C2", 4);
    Consumer consumer3 = new Consumer(queue, destination, "C3", 2);

    ExecutorService executor = Executors.newFixedThreadPool(5);
    executor.submit(producer1);
    executor.submit(producer2);
    executor.submit(consumer1);
    executor.submit(consumer2);
    executor.submit(consumer3);

    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);

    assertEquals(5, producer1.getItemsProduced());
    assertEquals(5, producer2.getItemsProduced());
    assertEquals(4, consumer1.getItemsConsumed());
    assertEquals(4, consumer2.getItemsConsumed());
    assertEquals(2, consumer3.getItemsConsumed());
    assertEquals(10, destination.size());
    assertTrue(queue.isEmpty());
  }

  /**
   * Stress tests the system with a small queue capacity to verify blocking behavior.
   */
  @Test
  @DisplayName("Scenario 7: Small Queue Capacity (Stress Test)")
  void testSmallQueueCapacity() throws InterruptedException {
    MyBlockingQueue<Integer> queue = new MyBlockingQueue<>(2);
    List<Integer> source = IntStream.rangeClosed(1, 20).boxed().collect(java.util.stream.Collectors.toList());
    List<Integer> destination = Collections.synchronizedList(new ArrayList<>());

    Producer producer = new Producer(queue, source, "P1");
    Consumer consumer1 = new Consumer(queue, destination, "C1", 10);
    Consumer consumer2 = new Consumer(queue, destination, "C2", 10);

    ExecutorService executor = Executors.newFixedThreadPool(3);
    executor.submit(producer);
    executor.submit(consumer1);
    executor.submit(consumer2);

    executor.shutdown();
    executor.awaitTermination(10, TimeUnit.SECONDS);

    assertEquals(20, producer.getItemsProduced());
    assertEquals(10, consumer1.getItemsConsumed());
    assertEquals(10, consumer2.getItemsConsumed());
    assertEquals(20, destination.size());
    assertTrue(queue.isEmpty());
  }

  /**
   * Verifies the system handles large datasets with multiple producers and consumers.
   */
  @Test
  @DisplayName("Scenario 8: Large Dataset")
  void testLargeDataset() throws InterruptedException {
    MyBlockingQueue<Integer> queue = new MyBlockingQueue<>(50);
    List<Integer> source = IntStream.rangeClosed(1, 1000).boxed().collect(java.util.stream.Collectors.toList());
    List<Integer> destination = Collections.synchronizedList(new ArrayList<>());

    Producer producer1 = new Producer(queue, source.subList(0, 250), "P1");
    Producer producer2 = new Producer(queue, source.subList(250, 500), "P2");
    Producer producer3 = new Producer(queue, source.subList(500, 750), "P3");
    Producer producer4 = new Producer(queue, source.subList(750, 1000), "P4");

    Consumer consumer1 = new Consumer(queue, destination, "C1", 250);
    Consumer consumer2 = new Consumer(queue, destination, "C2", 250);
    Consumer consumer3 = new Consumer(queue, destination, "C3", 250);
    Consumer consumer4 = new Consumer(queue, destination, "C4", 250);

    ExecutorService executor = Executors.newFixedThreadPool(8);
    executor.submit(producer1);
    executor.submit(producer2);
    executor.submit(producer3);
    executor.submit(producer4);
    executor.submit(consumer1);
    executor.submit(consumer2);
    executor.submit(consumer3);
    executor.submit(consumer4);

    executor.shutdown();
    executor.awaitTermination(30, TimeUnit.SECONDS);

    assertEquals(250, producer1.getItemsProduced());
    assertEquals(250, producer2.getItemsProduced());
    assertEquals(250, producer3.getItemsProduced());
    assertEquals(250, producer4.getItemsProduced());
    assertEquals(250, consumer1.getItemsConsumed());
    assertEquals(250, consumer2.getItemsConsumed());
    assertEquals(250, consumer3.getItemsConsumed());
    assertEquals(250, consumer4.getItemsConsumed());
    assertEquals(1000, destination.size());
    assertTrue(queue.isEmpty());
  }

  /**
   * Verifies correct behavior when producer is faster than consumer with queue filling up.
   */
  @Test
  @DisplayName("Scenario 9: Producer Faster Than Consumer")
  void testProducerFasterThanConsumer() throws InterruptedException {
    MyBlockingQueue<Integer> queue = new MyBlockingQueue<>(5);
    List<Integer> source = IntStream.rangeClosed(1, 10).boxed().collect(java.util.stream.Collectors.toList());
    List<Integer> destination = Collections.synchronizedList(new ArrayList<>());

    Producer producer = new Producer(queue, source, "P1");
    Consumer consumer = new Consumer(queue, destination, "C1", 10);

    Thread producerThread = new Thread(producer);
    Thread consumerThread = new Thread(consumer);

    consumerThread.start();
    Thread.sleep(100);
    producerThread.start();

    producerThread.join();
    consumerThread.join();

    assertEquals(10, producer.getItemsProduced());
    assertEquals(10, consumer.getItemsConsumed());
    assertEquals(10, destination.size());
    assertTrue(queue.isEmpty());
  }

  /**
   * Verifies correct behavior when consumer is faster than producer with queue emptying frequently.
   */
  @Test
  @DisplayName("Scenario 10: Consumer Faster Than Producer")
  void testConsumerFasterThanProducer() throws InterruptedException {
    MyBlockingQueue<Integer> queue = new MyBlockingQueue<>(5);
    List<Integer> source = IntStream.rangeClosed(1, 10).boxed().collect(java.util.stream.Collectors.toList());
    List<Integer> destination = Collections.synchronizedList(new ArrayList<>());

    Producer producer = new Producer(queue, source, "P1");
    Consumer consumer = new Consumer(queue, destination, "C1", 10);

    Thread producerThread = new Thread(producer);
    Thread consumerThread = new Thread(consumer);

    producerThread.start();
    Thread.sleep(100);
    consumerThread.start();

    producerThread.join();
    consumerThread.join();

    assertEquals(10, producer.getItemsProduced());
    assertEquals(10, consumer.getItemsConsumed());
    assertEquals(10, destination.size());
    assertTrue(queue.isEmpty());
  }

  /**
   * Verifies all items are correctly transferred without loss or duplication.
   */
  @Test
  @DisplayName("Scenario 11: All Items Transferred Correctly")
  void testAllItemsTransferredCorrectly() throws InterruptedException {
    MyBlockingQueue<Integer> queue = new MyBlockingQueue<>(10);
    List<Integer> source = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    List<Integer> destination = Collections.synchronizedList(new ArrayList<>());

    Producer producer1 = new Producer(queue, source.subList(0, 5), "P1");
    Producer producer2 = new Producer(queue, source.subList(5, 10), "P2");
    Consumer consumer1 = new Consumer(queue, destination, "C1", 5);
    Consumer consumer2 = new Consumer(queue, destination, "C2", 5);

    ExecutorService executor = Executors.newFixedThreadPool(4);
    executor.submit(producer1);
    executor.submit(producer2);
    executor.submit(consumer1);
    executor.submit(consumer2);

    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);

    assertEquals(source.size(), destination.size());
    assertTrue(destination.containsAll(source));
    assertTrue(source.containsAll(destination));
  }

  /**
   * Verifies thread safety with multiple concurrent producers and consumers.
   */
  @Test
  @DisplayName("Scenario 12: Thread Safety Verification")
  void testThreadSafety() throws InterruptedException {
    MyBlockingQueue<Integer> queue = new MyBlockingQueue<>(10);
    List<Integer> destination = Collections.synchronizedList(new ArrayList<>());

    int numProducers = 5;
    int numConsumers = 3;
    int itemsPerProducer = 20;
    int totalItems = numProducers * itemsPerProducer;

    List<Producer> producers = new ArrayList<>();
    List<Consumer> consumers = new ArrayList<>();
    List<Thread> threads = new ArrayList<>();

    for (int i = 0; i < numProducers; i++) {
      List<Integer> source = IntStream.rangeClosed(i * itemsPerProducer + 1,
              (i + 1) * itemsPerProducer).boxed().collect(java.util.stream.Collectors.toList());
      Producer producer = new Producer(queue, source, "P" + (i + 1));
      producers.add(producer);
      threads.add(new Thread(producer));
    }

    int baseItemsPerConsumer = totalItems / numConsumers;
    int remainder = totalItems % numConsumers;

    for (int i = 0; i < numConsumers; i++) {
      int itemsForThisConsumer = baseItemsPerConsumer + (i < remainder ? 1 : 0);
      Consumer consumer = new Consumer(queue, destination, "C" + (i + 1), itemsForThisConsumer);
      consumers.add(consumer);
      threads.add(new Thread(consumer));
    }

    threads.forEach(Thread::start);

    for (Thread thread : threads) {
      thread.join(10000);
    }

    int totalProduced = producers.stream().mapToInt(Producer::getItemsProduced).sum();
    int totalConsumed = consumers.stream().mapToInt(Consumer::getItemsConsumed).sum();

    assertEquals(totalItems, totalProduced);
    assertEquals(totalItems, totalConsumed);
    assertEquals(totalItems, destination.size());
    assertTrue(queue.isEmpty());
  }

  /**
   * Verifies producer correctly blocks and waits when the queue is full.
   */
  @Test
  @DisplayName("Scenario 13: Verify Wait on Full Queue")
  void testProducerWaitsWhenQueueFull() throws InterruptedException {
    MyBlockingQueue<Integer> queue = new MyBlockingQueue<>(2);
    List<Integer> source = Arrays.asList(1, 2, 3, 4, 5);
    List<Integer> destination = Collections.synchronizedList(new ArrayList<>());

    Producer producer = new Producer(queue, source, "P1");
    Thread producerThread = new Thread(producer);

    producerThread.start();
    Thread.sleep(300);

    assertTrue(producerThread.isAlive(), "Producer should still be running");
    assertTrue(producer.getItemsProduced() < 5, "Producer should be blocked before finishing all items");

    assertTrue(queue.isFull(), "Queue should be full");
    assertEquals(2, queue.size(), "Queue size should be 2");

    Consumer consumer = new Consumer(queue, destination, "C1", 5);
    Thread consumerThread = new Thread(consumer);
    consumerThread.start();

    producerThread.join(5000);
    consumerThread.join(5000);

    assertEquals(5, producer.getItemsProduced());
    assertEquals(5, consumer.getItemsConsumed());
    assertEquals(5, destination.size());
    assertTrue(queue.isEmpty());
  }

  /**
   * Verifies consumer correctly blocks and waits when the queue is empty.
   */
  @Test
  @DisplayName("Scenario 14: Verify Wait on Empty Queue")
  void testConsumerWaitsWhenQueueEmpty() throws InterruptedException {
    MyBlockingQueue<Integer> queue = new MyBlockingQueue<>(5);
    List<Integer> destination = Collections.synchronizedList(new ArrayList<>());
    CountDownLatch consumerStarted = new CountDownLatch(1);

    Consumer consumer = new Consumer(queue, destination, "C1", 5);
    Thread consumerThread = new Thread(() -> {
      consumerStarted.countDown();
      consumer.run();
    });
    consumerThread.start();
    consumerStarted.await();
    Thread.sleep(100);

    assertTrue(queue.isEmpty());
    assertTrue(consumerThread.isAlive());
    assertEquals(0, destination.size());

    Producer producer = new Producer(queue, Arrays.asList(1, 2, 3, 4, 5), "P1");
    Thread producerThread = new Thread(producer);
    producerThread.start();

    producerThread.join();
    consumerThread.join();

    assertEquals(5, destination.size());
  }

  /**
   * Verifies notifyAll wakes all blocked producers when queue space becomes available.
   */
  @Test
  @DisplayName("Scenario 15: Multiple Producers Blocked, All Notified")
  void testNotifyAllWakesAllProducers() throws InterruptedException {
    MyBlockingQueue<Integer> queue = new MyBlockingQueue<>(1);
    List<Integer> destination = Collections.synchronizedList(new ArrayList<>());


    Producer p1 = new Producer(queue, Arrays.asList(1, 2), "P1");
    Producer p2 = new Producer(queue, Arrays.asList(3, 4), "P2");
    Producer p3 = new Producer(queue, Arrays.asList(5, 6), "P3");

    Thread t1 = new Thread(p1);
    Thread t2 = new Thread(p2);
    Thread t3 = new Thread(p3);

    t1.start();
    t2.start();
    t3.start();
    Thread.sleep(200);

    Consumer consumer = new Consumer(queue, destination, "C1", 6);
    Thread consumerThread = new Thread(consumer);
    consumerThread.start();

    t1.join(5000);
    t2.join(5000);
    t3.join(5000);
    consumerThread.join(5000);

    assertEquals(2, p1.getItemsProduced());
    assertEquals(2, p2.getItemsProduced());
    assertEquals(2, p3.getItemsProduced());
    assertEquals(6, destination.size());
  }

  /**
   * Verifies threads handle interruption gracefully without data corruption.
   */
  @Test
  @DisplayName("Scenario 16: Handle Thread Interruption")
  void testThreadInterruption() throws InterruptedException {
    MyBlockingQueue<Integer> queue = new MyBlockingQueue<>(1);
    List<Integer> source = IntStream.rangeClosed(1, 100).boxed()
            .collect(java.util.stream.Collectors.toList());

    Producer producer = new Producer(queue, source, "P1");
    Thread producerThread = new Thread(producer);
    producerThread.start();

    Thread.sleep(100);
    producerThread.interrupt();
    producerThread.join(1000);

    assertFalse(producerThread.isAlive());
    assertTrue(producer.getItemsProduced() < 100);
  }

  /**
   * Verifies multiple waiting consumers handle spurious wakeups correctly.
   */
  @Test
  @DisplayName("Scenario 17: Multiple Consumers Waiting - Spurious Wakeup Protection")
  void testMultipleConsumersWaitingOnEmpty() throws InterruptedException {
    MyBlockingQueue<Integer> queue = new MyBlockingQueue<>(5);
    List<Integer> destination = Collections.synchronizedList(new ArrayList<>());
    CountDownLatch consumersReady = new CountDownLatch(2);

    Consumer consumer1 = new Consumer(queue, destination, "C1", 5);
    Consumer consumer2 = new Consumer(queue, destination, "C2", 5);

    Thread c1Thread = new Thread(() -> {
      consumersReady.countDown();
      consumer1.run();
    });

    Thread c2Thread = new Thread(() -> {
      consumersReady.countDown();
      consumer2.run();
    });

    c1Thread.start();
    c2Thread.start();
    consumersReady.await();
    Thread.sleep(200);

    assertTrue(queue.isEmpty());
    assertTrue(c1Thread.isAlive());
    assertTrue(c2Thread.isAlive());
    assertEquals(0, destination.size());


    List<Integer> source = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    Producer producer = new Producer(queue, source, "P1");
    Thread producerThread = new Thread(producer);
    producerThread.start();

    producerThread.join(10000);
    c1Thread.join(10000);
    c2Thread.join(10000);


    assertEquals(10, producer.getItemsProduced());
    assertEquals(5, consumer1.getItemsConsumed());
    assertEquals(5, consumer2.getItemsConsumed());
    assertEquals(10, destination.size());
    assertTrue(queue.isEmpty());

  }

  /**
   * Verifies multiple consumers correctly share items produced slowly one at a time.
   */
  @Test
  @DisplayName("Scenario 18: Multiple Consumers with Single Item Production")
  void testMultipleConsumersWithSlowProduction() throws InterruptedException {
    MyBlockingQueue<Integer> queue = new MyBlockingQueue<>(1);
    List<Integer> destination = Collections.synchronizedList(new ArrayList<>());

    Consumer c1 = new Consumer(queue, destination, "C1", 2);
    Consumer c2 = new Consumer(queue, destination, "C2", 2);
    Consumer c3 = new Consumer(queue, destination, "C3", 2);

    Thread t1 = new Thread(c1);
    Thread t2 = new Thread(c2);
    Thread t3 = new Thread(c3);

    t1.start();
    t2.start();
    t3.start();
    Thread.sleep(200);

    for (int i = 1; i <= 6; i++) {
      queue.put(i);
      Thread.sleep(100);
    }

    t1.join(5000);
    t2.join(5000);
    t3.join(5000);

    assertEquals(2, c1.getItemsConsumed());
    assertEquals(2, c2.getItemsConsumed());
    assertEquals(2, c3.getItemsConsumed());
    assertEquals(6, destination.size());
    assertTrue(queue.isEmpty());
  }

  /**
   * Verifies multiple producers correctly resume when queue space becomes available.
   */
  @Test
  @DisplayName("Scenario 19: Multiple Producers Waiting on Full Queue")
  void testMultipleProducersWaitingOnFull() throws InterruptedException {
    MyBlockingQueue<Integer> queue = new MyBlockingQueue<>(1);
    List<Integer> destination = Collections.synchronizedList(new ArrayList<>());

    Producer p1 = new Producer(queue, Arrays.asList(1, 2), "P1");
    Producer p2 = new Producer(queue, Arrays.asList(3, 4), "P2");
    Producer p3 = new Producer(queue, Arrays.asList(5, 6), "P3");

    Thread t1 = new Thread(p1);
    Thread t2 = new Thread(p2);
    Thread t3 = new Thread(p3);

    t1.start();
    t2.start();
    t3.start();
    Thread.sleep(200);

    assertTrue(queue.isFull());

    Consumer consumer = new Consumer(queue, destination, "C1", 6);
    Thread consumerThread = new Thread(consumer);
    consumerThread.start();

    t1.join(5000);
    t2.join(5000);
    t3.join(5000);
    consumerThread.join(5000);

    assertEquals(2, p1.getItemsProduced());
    assertEquals(2, p2.getItemsProduced());
    assertEquals(2, p3.getItemsProduced());
    assertEquals(6, destination.size());
  }
}
