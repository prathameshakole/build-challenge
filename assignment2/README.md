# Assignment 2

## Data analysis using appropriate API on CSV data

---
## Overview

- In this assignment I have used real world car sales dataset to perform data analysis.
- I have used Streams and Functional Programming Paradigms to write the methods for analytics.
- Multiple instances of data aggregation and grouping during the analysis.

---

## Setup and Running the Main Class

1. Clone repository
2. Check for prerequisites (Java 11 or higher) and (Maven 3.6 or higher)
3. Verify Java and Maven Installations
4. Compile using `mvn clean install`
5. Run all the tests using `mvn test` command (I have tested around 20 scenarios, Go to `Tests Section` to know more)
6. Go to `assignment2\src\main\java\com\a2>`
7. Compile using `javac *.java`
8. Run the Demo class using `java Main.java`

Alternatively if the code does not run using this command in your environment.

1. Go to Main.java and change line 15 to 
`Path csvPath = Path.of("src/java/com/a2/car_prices.csv")`
2. Go to `/assignment2` and run the command `mvn exec:java -D exec.mainClass="com.a2.Main"`

---

## Sample Output

```
7. SALES VOLUME BY VEHICLE YEAR
   1982 -> 2 vehicles
   1983 -> 1 vehicles
   1984 -> 5 vehicles
   1985 -> 10 vehicles
   1986 -> 11 vehicles
   1987 -> 8 vehicles
   1988 -> 11 vehicles
   1989 -> 20 vehicles
   1990 -> 49 vehicles
   1991 -> 67 vehicles
   1992 -> 132 vehicles
   1993 -> 205 vehicles
   1994 -> 392 vehicles
   1995 -> 711 vehicles
   1996 -> 851 vehicles
   1997 -> 1,546 vehicles
   1998 -> 2,149 vehicles
   1999 -> 3,363 vehicles
   2000 -> 5,227 vehicles
   2001 -> 6,468 vehicles
   2002 -> 9,715 vehicles
   2003 -> 13,281 vehicles
   2004 -> 17,342 vehicles
   2005 -> 21,394 vehicles
   2006 -> 26,913 vehicles
   2007 -> 30,845 vehicles
   2008 -> 31,502 vehicles
   2009 -> 20,594 vehicles
   2010 -> 26,485 vehicles
   2011 -> 48,548 vehicles
   2012 -> 102,315 vehicles
   2013 -> 98,168 vehicles
   2014 -> 81,070 vehicles
   2015 -> 9,437 vehicles

8. AVERAGE SELLING PRICE BY SALE MONTH
   1970-01 -> $9,350
   2014-01 -> $15,556
   2014-02 -> $10,500
   2014-12 -> $11,294
   2015-01 -> $13,288
   2015-02 -> $13,608
   2015-03 -> $13,443
   2015-04 -> $10,207
   2015-05 -> $14,342
   2015-06 -> $15,008
   2015-07 -> $16,977
   
```

---
## Decisions

### 1. Dataset choice

For this assignment I used a real world style vehicle sales dataset, `car_prices.csv`.
Each row represents a single used car sale with information about:

* Vehicle attributes
* Seller and location
* Market reference price
* Final selling price
* Timestamp of the sale

This dataset is a good fit for Streams and functional style because:

* It has a mix of numerical and categorical fields.
* It supports realistic aggregation and grouping queries.

It is large enough to make the analysis meaningful, but still structured and regular.

---

### 2. Schema and POJO model

#### 2.1 CSV columns

The code assumes the following column order in `car_prices.csv`:

1. `year`
2. `make`
3. `model`
4. `trim`
5. `body`
6. `transmission`
7. `vin`
8. `state`
9. `condition`
10. `odometer`
11. `color`
12. `interior`
13. `seller`
14. `mmr`
15. `sellingprice`
16. `saledate`

#### 2.2 POJO (`CarPricePOJO`)

Each row is mapped into a `CarPricePOJO` object with:

* `int year`
* `String make`
* `String model`
* `String trim`
* `String body`
* `String transmission`
* `String vin`
* `String state`
* `int condition`
* `int odometer`
* `String color`
* `String interior`
* `String seller`
* `int mmr`
* `int sellingPrice`
* `LocalDateTime saleDateTime`


#### 2.3 Why a POJO

* It gives type safety for numeric fields and dates.
* It keeps parsing and domain logic in one place.
* It keeps Stream pipelines clean and matches typical Java practice for domain modelling.
* The class is immutable from the outside and analytics functions do not mutate records, which aligns well with a functional style.

---

### 3. Assumptions:

* Time zone and offset are ignored, since for aggregation by `YearMonth` or date it is enough to know local date and time.
* If the `saledate` value is missing or too short to parse, a fallback value of `1970-01-01T00:00` is used.

---

## 4. Functional programming and Streams

All analytical logic is implemented in `CarAnalytics` as pure functions on `List<CarPricePOJO>` using Java Streams and collectors.

```
Examples of Stream operations used:

* `map`, `mapToInt`, `filter`
* `collect` with:

    * `groupingBy`
    * `counting`
    * `averagingInt`
    * `summarizingDouble`
* `sorted`, `limit` for top N style queries
```

### 4.2 Analytics implemented

1. averageSellingPrice - Overall average selling price across all vehicles 
2. averageSellingPriceByMake - Average prices grouped by manufacturer 
3. averageSellingPriceByMakeModel - Average prices for specific make-model combinations 
4. salesCountByState - Total sales volume by geographic state
5. averageOdometerByMake - Average mileage patterns by manufacturer 
6. priceDeltaStatsByMake - Variance between selling price and market value (MMR) by make
7. salesCountByYear - Total sales volume by year. 
8. averagePriceByYearMonth - Monthly price trends over time (year-month granularity)
9. topMakeModelsByVolume - Best-selling make-model combinations by sales volume 
10. countByBody - Distribution of body styles (Sedan, SUV, etc.)
11. findBestDeals - Identifies vehicles priced significantly below market value
12. depreciationRateByMake - Annual depreciation rates by manufacturer
13. averagePriceByMonth - Seasonal pricing patterns by calendar month
14. priceByMileageBracket - Price depreciation patterns across 10k mile brackets
15. priceStatsByCondition - Price statistics segmented by vehicle condition rating
16. regionalPriceByModel - Geographic price variations for popular models
17. sellerMarkupPercentage - Average markup above or below MMR by seller
18. inventoryProfitabilityRanking - Models ranked by combined sales volume and profit margins

---

## 5. Testing assumptions (JUnit)

Two JUnit test classes are provided:

* `CarAnalyticsTest`

    * Uses a small in memory list of `CarPricePOJO` objects.
    * Tests each analytics function with known expected values.
* `CarCsvLoaderTest`

    * Builds a temporary CSV file with one row.
    * Verifies that parsing matches the expected field values and that `saledate` parsing works.

Assumptions for tests:

* Tests do not depend on the full real dataset, to keep them deterministic and fast.
* Tests only validate functional behavior of analytics and parsing logic, not performance.

---
