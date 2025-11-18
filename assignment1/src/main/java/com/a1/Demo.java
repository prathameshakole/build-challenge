package com.a1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;


/**
 * Interactive demonstration of the producer-consumer pattern with customizable configurations.
 */
public class Demo {
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    boolean running = true;

    System.out.println("=== Producer-Consumer Pattern Demo ===\n");

    while (running) {
      System.out.println("\n--- Main Menu ---");
      System.out.println("1. Run sample task with predefined values");
      System.out.println("2. Enter custom inputs");
      System.out.println("3. Exit");
      System.out.print("\nEnter your choice (1, 2, or 3): ");

      int choice = getValidIntInput(scanner, 1, 3);

      try {
        switch (choice) {
          case 1:
            runSampleTask();
            break;
          case 2:
            runCustomTask(scanner);
            break;
          case 3:
            System.out.println("Exited Successfully!");
            running = false;
            break;
        }

        if (running && choice != 3) {
          System.out.println("\nEnter your choice (1, 2, or 3) to continue...");
          scanner.nextLine();
        }

      } catch (InterruptedException e) {
        System.out.println("\nDemo was interrupted!");
        Thread.currentThread().interrupt();
      }
    }

    scanner.close();
  }

  private static void runSampleTask() throws InterruptedException {
    System.out.println("\n=== Running Sample Task ===\n");

    int queueCapacity = 5;
    int numProducers = 2;
    int numConsumers = 2;
    int totalItems = 10;

    System.out.println("Configuration:");
    System.out.println("  - Queue Capacity: " + queueCapacity);
    System.out.println("  - Number of Producers: " + numProducers);
    System.out.println("  - Number of Consumers: " + numConsumers);
    System.out.println("  - Total Items: " + totalItems);
    System.out.println();

    executeDemo(queueCapacity, numProducers, numConsumers, totalItems);
  }

  private static void runCustomTask(Scanner scanner) throws InterruptedException {
    System.out.println("\n=== Custom Configuration ===\n");

    System.out.print("Enter queue capacity (1-100): ");
    int queueCapacity = getValidIntInput(scanner, 1, 100);

    System.out.print("Enter number of producers (1-10): ");
    int numProducers = getValidIntInput(scanner, 1, 10);

    System.out.print("Enter number of consumers (1-10): ");
    int numConsumers = getValidIntInput(scanner, 1, 10);

    System.out.print("Enter total number of items to produce (1-1000): ");
    int totalItems = getValidIntInput(scanner, 1, 1000);

    System.out.println("\nConfiguration Summary:");
    System.out.println("  - Queue Capacity: " + queueCapacity);
    System.out.println("  - Number of Producers: " + numProducers);
    System.out.println("  - Number of Consumers: " + numConsumers);
    System.out.println("  - Total Items: " + totalItems);
    System.out.println();

    executeDemo(queueCapacity, numProducers, numConsumers, totalItems);
  }

  private static void executeDemo(int queueCapacity, int numProducers,
                                  int numConsumers, int totalItems) throws InterruptedException {

    MyBlockingQueue<Integer> queue = new MyBlockingQueue<>(queueCapacity);
    List<Integer> destination = Collections.synchronizedList(new ArrayList<>());


    List<Producer> producers = new ArrayList<>();
    int itemsPerProducer = totalItems / numProducers;
    int remainingItems = totalItems % numProducers;

    int currentItem = 1;
    for (int i = 0; i < numProducers; i++) {
      int itemsForThisProducer = itemsPerProducer + (i < remainingItems ? 1 : 0);
      List<Integer> source = IntStream.range(currentItem, currentItem + itemsForThisProducer)
              .boxed()
              .collect(java.util.stream.Collectors.toList());
      currentItem += itemsForThisProducer;

      Producer producer = new Producer(queue, source, "P" + (i + 1));
      producers.add(producer);
    }

    List<Consumer> consumers = new ArrayList<>();
    int itemsPerConsumer = totalItems / numConsumers;
    int remainingConsumerItems = totalItems % numConsumers;

    for (int i = 0; i < numConsumers; i++) {
      int itemsForThisConsumer = itemsPerConsumer + (i < remainingConsumerItems ? 1 : 0);
      Consumer consumer = new Consumer(queue, destination, "C" + (i + 1), itemsForThisConsumer);
      consumers.add(consumer);
    }

    ExecutorService executor = Executors.newFixedThreadPool(numProducers + numConsumers);

    System.out.println("Starting threads...\n");

    for (Producer producer : producers) {
      executor.submit(producer);
    }

    for (Consumer consumer : consumers) {
      executor.submit(consumer);
    }

    executor.shutdown();
    boolean finished = executor.awaitTermination(30, TimeUnit.SECONDS);

    if (!finished) {
      System.out.println("\nWarning: Execution timed out!");
      executor.shutdownNow();
    }

    printAnalysisResults(totalItems, destination, producers, consumers, queue);
  }

  private static void printAnalysisResults(int expectedItems, List<Integer> destination,
                                           List<Producer> producers, List<Consumer> consumers,
                                           MyBlockingQueue<Integer> queue) {
    System.out.println("\n=== Analysis Results ===");

    int totalProduced = producers.stream().mapToInt(Producer::getItemsProduced).sum();
    int totalConsumed = consumers.stream().mapToInt(Consumer::getItemsConsumed).sum();

    System.out.println("\n1. Production Summary:");
    for (int i = 0; i < producers.size(); i++) {
      System.out.println("   - Producer P" + (i + 1) + ": " +
              producers.get(i).getItemsProduced() + " items");
    }
    System.out.println("   - Total Produced: " + totalProduced);

    System.out.println("\n2. Consumption Summary:");
    for (int i = 0; i < consumers.size(); i++) {
      System.out.println("   - Consumer C" + (i + 1) + ": " +
              consumers.get(i).getItemsConsumed() + " items");
    }
    System.out.println("   - Total Consumed: " + totalConsumed);

    System.out.println("\n3. Destination Items:");
    if (destination.size() <= 20) {
      System.out.println("   " + destination);
    } else {
      System.out.println("   First 10: " + destination.subList(0, 10));
      System.out.println("   Last 10: " + destination.subList(destination.size() - 10, destination.size()));
      System.out.println("   (Total: " + destination.size() + " items)");
    }

    System.out.println("\n4. Verification:");
    System.out.println("   - Expected items: " + expectedItems);
    System.out.println("   - Destination size: " + destination.size());
    System.out.println("   - Queue empty: " + queue.isEmpty());
    System.out.println("   - All items transferred: " + (expectedItems == destination.size()));
    System.out.println("   - Production matches consumption: " + (totalProduced == totalConsumed));

    boolean success = (expectedItems == totalProduced) &&
            (expectedItems == totalConsumed) &&
            (expectedItems == destination.size()) &&
            queue.isEmpty();

    if (success) {
      System.out.println("\n✓ Thank you!");
    } else {
      System.out.println("\n✗ Warning: Some items may not have been transferred correctly!");
    }

    System.out.println("\n=== Demo Complete ===");
  }

  private static int getValidIntInput(Scanner scanner, int min, int max) {
    while (true) {
      try {
        String input = scanner.nextLine().trim();
        int value = Integer.parseInt(input);

        if (value >= min && value <= max) {
          return value;
        } else {
          System.out.print("Please enter a number between " + min + " and " + max + ": ");
        }
      } catch (NumberFormatException e) {
        System.out.print("Invalid input. Please enter a valid number: ");
      }
    }
  }
}
