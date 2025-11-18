package com.a1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Unit tests for the Consumer class verifying blocking queue consumption behavior.
 */
public class ConsumerTest {

  private MyBlockingQueue<Integer> queue;
  private List<Integer> destination;

  @BeforeEach
  void setUp() {
    queue = new MyBlockingQueue<>(10);
    destination = new ArrayList<>();
  }

  /**
   * Verifies that a consumer correctly consumes all items from a pre-populated queue.
   */
  @Test
  void testConsumerConsumesAllItems() throws InterruptedException {
    for (int i = 1; i <= 5; i++) {
      queue.put(i);
    }

    Consumer consumer = new Consumer(queue, destination, "TestConsumer", 5);
    Thread thread = new Thread(consumer);
    thread.start();
    thread.join();

    assertEquals(5, consumer.getItemsConsumed());
    assertEquals(5, destination.size());
    assertTrue(destination.containsAll(List.of(1, 2, 3, 4, 5)));
  }

  /**
   * Verifies that a consumer blocks when the queue is empty and resumes when items are added.
   */
  @Test
  void testConsumerBlocksWhenQueueEmpty() throws InterruptedException {
    Consumer consumer = new Consumer(queue, destination, "TestConsumer", 3);
    Thread thread = new Thread(consumer);
    thread.start();

    Thread.sleep(100);
    assertEquals(0, consumer.getItemsConsumed());

    queue.put(1);
    queue.put(2);
    queue.put(3);

    thread.join(2000);
    assertEquals(3, consumer.getItemsConsumed());
    assertEquals(3, destination.size());
  }
}
