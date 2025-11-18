package com.a1;

import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Producer thread that takes items from a source list and puts them into a blocking queue.
 * Implements the producer side of the producer-consumer pattern with thread synchronization.
 */
public class Producer implements Runnable {
  private final MyBlockingQueue<Integer> queue;
  private final List<Integer> source;
  private final String name;
  private int itemsProduced = 0;

  public Producer(MyBlockingQueue<Integer> queue, List<Integer> source, String name) {
    this.queue = queue;
    this.source = source;
    this.name = name;
  }

  /**
   * Takes items from the source list and puts them into the queue.
   */
  @Override
  public void run() {
    try {
      for (Integer item : source) {
        queue.put(item);
        LocalDateTime timestamp = LocalDateTime.now();  // Capture timestamp immediately after put
        itemsProduced++;
        System.out.println("[" + timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")) + "] Producer " + name + " produced: " + item);
        Thread.sleep(50); // Simulate work
      }
      System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")) + "] Producer " + name + " finished. Total produced: " + itemsProduced);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      System.out.println("Producer " + name + " interrupted");
    }
  }

  public int getItemsProduced() {
    return itemsProduced;
  }
}

