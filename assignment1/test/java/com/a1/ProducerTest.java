package com.a1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for the Producer class verifying blocking queue production behavior.
 */
public class ProducerTest {

  private MyBlockingQueue<Integer> queue;
  private List<Integer> source;

  @BeforeEach
  void setUp() {
    queue = new MyBlockingQueue<>(10);
    source = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
  }

  /**
   * Verifies that a producer correctly produces all items from the source list to the queue.
   */
  @Test
  void testProducerProducesAllItems() throws InterruptedException {
    Producer producer = new Producer(queue, source, "TestProducer");
    Thread thread = new Thread(producer);
    thread.start();
    thread.join();

    assertEquals(5, producer.getItemsProduced());
    assertEquals(5, queue.size());
  }


  /**
   * Verifies that a producer blocks when the queue is full and resumes when space becomes available.
   */
  @Test
  void testProducerBlocksWhenQueueFull() throws InterruptedException {
    MyBlockingQueue<Integer> smallQueue = new MyBlockingQueue<>(2);
    Producer producer = new Producer(smallQueue, source, "TestProducer");
    Thread thread = new Thread(producer);
    thread.start();


    Thread.sleep(300);

    assertEquals(2, smallQueue.size(), "Queue should be full");

    int producedWhileBlocked = producer.getItemsProduced();
    assertTrue(producedWhileBlocked >= 2, "Producer should have filled the queue");

    Thread.sleep(300);
    assertEquals(producedWhileBlocked, producer.getItemsProduced(),
            "Producer should be blocked - item count shouldn't increase");

    smallQueue.take();
    Thread.sleep(200);

    assertTrue(producer.getItemsProduced() > producedWhileBlocked,
            "Producer should have resumed after space became available");

    while (thread.isAlive() || !smallQueue.isEmpty()) {
      if (!smallQueue.isEmpty()) {
        smallQueue.take();
      }
      Thread.sleep(10);
    }

    assertEquals(5, producer.getItemsProduced(), "All items should eventually be produced");
  }
}