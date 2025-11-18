package com.a1;

import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Consumer thread that takes items from a blocking queue and adds them to a destination list.
 * Implements the consumer side of the producer-consumer pattern with thread synchronization.
 */
public class Consumer implements Runnable {
  private final MyBlockingQueue<Integer> queue;
  private final List<Integer> destination;
  private final String name;
  private final int itemsToConsume;
  private int itemsConsumed = 0;

  public Consumer(MyBlockingQueue<Integer> queue, List<Integer> destination,
                  String name, int itemsToConsume) {
    this.queue = queue;
    this.destination = destination;
    this.name = name;
    this.itemsToConsume = itemsToConsume;
  }

  /**
   * Consumes items from the queue and adds them to the destination list.
   */
  @Override
  public void run() {
    try {
      for (int i = 0; i < itemsToConsume; i++) {
        Integer item = queue.take();
        synchronized (destination) {
          destination.add(item);
        }
        LocalDateTime timestamp = LocalDateTime.now();
        itemsConsumed++;
        System.out.println("[" + timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")) + "] Consumer " + name + " consumed: " + item);
        Thread.sleep(75);
      }
      System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")) + "] Consumer " + name + " finished. Total consumed: " + itemsConsumed);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      System.out.println("Consumer " + name + " interrupted");
    }
  }

  public int getItemsConsumed() {
    return itemsConsumed;
  }
}
