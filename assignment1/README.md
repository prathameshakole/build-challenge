# Assignment 1

## Producer- Consumer Pattern Implementation

### Overview

- In this assignment I implemented a classic producer consumer pattern demonstrating thread synchronization and communication using wait/Notify mechanism as instructed.


- The implementation includes a custom BlockingQueue called MyBlockingQueue (I avoided the Java's BlockingQueue) which uses synchronized blocks and wait/notify for thread-safe operations.

### Setup and Running the Demo Class

1. Clone repository
2. Check for prerequisites (Java 11 or higher) and (Maven 3.6 or higher)
3. Verify Java and Maven Installations 
4. Compile using `mvn clean install`
5. Run all the tests using `mvn test` command (I have tested around 20 scenarios, Go to `Tests Section` to know more) 
6. Go to `assignment1\src\main\java\com\a1>` 
7. Compile using `javac *.java`
8. Run the Demo class using `java Demo.java`

### Sample Output
```
=== Running Sample Task ===

Configuration:
- Queue Capacity: 5
- Number of Producers: 2
- Number of Consumers: 2
- Total Items: 10

Starting threads...

[2025-11-17 18:53:47.738] Consumer C1 consumed: 1
[2025-11-17 18:53:47.738] Producer P1 produced: 1
[2025-11-17 18:53:47.739] Producer P2 produced: 6
[2025-11-17 18:53:47.739] Consumer C2 consumed: 6
[2025-11-17 18:53:47.820] Producer P2 produced: 7
[2025-11-17 18:53:47.820] Producer P1 produced: 2
[2025-11-17 18:53:47.846] Consumer C1 consumed: 7
[2025-11-17 18:53:47.846] Consumer C2 consumed: 2
[2025-11-17 18:53:47.871] Producer P1 produced: 3
[2025-11-17 18:53:47.871] Producer P2 produced: 8
[2025-11-17 18:53:47.923] Consumer C1 consumed: 8
[2025-11-17 18:53:47.923] Producer P2 produced: 9
[2025-11-17 18:53:47.923] Consumer C2 consumed: 3
[2025-11-17 18:53:47.923] Producer P1 produced: 4
[2025-11-17 18:53:47.975] Producer P1 produced: 5
[2025-11-17 18:53:47.975] Producer P2 produced: 10
[2025-11-17 18:53:47.999] Consumer C1 consumed: 9
[2025-11-17 18:53:48.000] Consumer C2 consumed: 4
[2025-11-17 18:53:48.027] Producer P1 finished. Total produced: 5
[2025-11-17 18:53:48.027] Producer P2 finished. Total produced: 5
[2025-11-17 18:53:48.076] Consumer C1 consumed: 10
[2025-11-17 18:53:48.076] Consumer C2 consumed: 5
[2025-11-17 18:53:48.152] Consumer C1 finished. Total consumed: 5
[2025-11-17 18:53:48.152] Consumer C2 finished. Total consumed: 5

=== Analysis Results ===

1. Production Summary:
    - Producer P1: 5 items
    - Producer P2: 5 items
    - Total Produced: 10

2. Consumption Summary:
    - Consumer C1: 5 items
    - Consumer C2: 5 items
    - Total Consumed: 10

3. Destination Items:
   [1, 6, 7, 2, 8, 3, 9, 4, 5, 10]

4. Verification:
    - Expected items: 10
    - Destination size: 10
    - Queue empty: true
    - All items transferred: true
    - Production matches consumption: true

Thank you!

=== Demo Complete ===
```

### Tests

#### End-To-End Test Cases (EndToEndTest.java)

```
1. Single Producer, Single Consumer: Basic one-to-one scenario 
2. Multiple Producers, Single Consumer: Multiple producers feeding one consumer 
3. Single Producer, Multiple Consumers: One producer serving multiple consumers 
4. Multiple Producers, Multiple Consumers (Balanced): Equal producers and consumers 
5. More Producers Than Consumers: Stress test with producer overload 
6. More Consumers Than Producers: Stress test with consumer overload 
7. Small Queue Capacity: Stress test with minimal buffer size (capacity: 2)
8. Large Dataset: Performance test with 1000 items across 8 threads 
9. Producer Faster Than Consumer: Tests blocking when queue fills up 
10. Consumer Faster Than Producer: Tests blocking when queue empties.
11. All Items Transferred Correctly: Verifies data integrity with bidirectional checks 
12. Thread Safety Verification: Concurrent access validation with 5 producers and 3 consumers 
13. Verify Wait on Full Queue: Confirms producer blocking when queue reaches capacity 
14. Verify Wait on Empty Queue: Confirms consumer blocking when no items available 
15. Multiple Producers Blocked, All Notified: Verifies notifyAll() wakes all blocked producers 
16. Handle Thread Interruption: Tests graceful shutdown when thread is interrupted 
17. Multiple Consumers Waiting - Spurious Wakeup Protection: Tests protection against spurious wakeups 
18. Multiple Consumers with Single Item Production: Tests consumer competition with capacity: 1 
19. Multiple Producers Waiting on Full Queue: Verifies multiple producers unblock correctly
```