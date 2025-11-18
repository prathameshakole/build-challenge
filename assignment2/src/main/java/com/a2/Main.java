package com.a2;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Month;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;

public class Main {

  public static void main(String[] args) {
    Path csvPath = Path.of("car_prices.csv");
    CarCsvLoader loader = new CarCsvLoader();

    try {
      List<CarPricePOJO> cars = loader.load(csvPath);
      System.out.println("Total records loaded: " + cars.size());
      System.out.println();
      runAnalytics(cars);

    } catch (IOException e) {
      System.err.println("Failed to read CSV file from " + csvPath.toAbsolutePath());
      e.printStackTrace();
    } catch (RuntimeException e) {
      System.err.println("Error while parsing or analyzing data:");
      e.printStackTrace();
    }
  }

  private static void runAnalytics(List<CarPricePOJO> cars) {
    System.out.println("1. OVERALL AVERAGE SELLING PRICE");
    double avgPrice = CarAnalytics.averageSellingPrice(cars);
    System.out.printf("   Average: %s%n%n", CarAnalytics.formatPrice(avgPrice));

    System.out.println("2. AVERAGE SELLING PRICE BY MAKE");
    CarAnalytics.sortByDoubleValueDesc(CarAnalytics.averageSellingPriceByMake(cars))
            .forEach((make, price) ->
                    System.out.printf("   %-15s -> %s%n", make, CarAnalytics.formatPrice(price)));
    System.out.println();

    System.out.println("3. AVERAGE SELLING PRICE BY MAKE & MODEL (Top 10)");
    CarAnalytics.averageSellingPriceByMakeModel(cars).entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(10)
            .forEach(entry ->
                    System.out.printf("   %-30s -> %s%n", entry.getKey(),
                            CarAnalytics.formatPrice(entry.getValue())));
    System.out.println();

    System.out.println("4. SALES VOLUME BY STATE");
    CarAnalytics.sortByLongValueDesc(CarAnalytics.salesCountByState(cars))
            .forEach((state, count) ->
                    System.out.printf("   %-5s -> %,d vehicles%n", state, count));
    System.out.println();

    System.out.println("5. AVERAGE ODOMETER READING BY MAKE (Top 10)");
    CarAnalytics.averageOdometerByMake(cars).entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(10)
            .forEach(entry ->
                    System.out.printf("   %-15s -> %,.0f miles%n", entry.getKey(), entry.getValue()));
    System.out.println();

    System.out.println("6. PRICE DELTA STATISTICS BY MAKE (Selling Price - MMR)");
    Map<String, DoubleSummaryStatistics> deltaStatsByMake =
            CarAnalytics.priceDeltaStatsByMake(cars);
    deltaStatsByMake.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach((entry) -> {
              String make = entry.getKey();
              DoubleSummaryStatistics stats = entry.getValue();
              System.out.printf("   %s: Count=%,d, Avg=%s, Min=%s, Max=%s%n",
                      make, stats.getCount(),
                      CarAnalytics.formatPrice(stats.getAverage()),
                      CarAnalytics.formatPrice(stats.getMin()),
                      CarAnalytics.formatPrice(stats.getMax()));
            });
    System.out.println();

    System.out.println("7. SALES VOLUME BY VEHICLE YEAR");
    CarAnalytics.salesCountByYear(cars).entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry ->
                    System.out.printf("   %d -> %,d vehicles%n", entry.getKey(), entry.getValue()));
    System.out.println();

    System.out.println("8. AVERAGE SELLING PRICE BY SALE MONTH");
    Map<YearMonth, Double> avgByMonth = CarAnalytics.averagePriceByYearMonth(cars);
    avgByMonth.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry ->
                    System.out.printf("   %s -> %s%n", entry.getKey(),
                            CarAnalytics.formatPrice(entry.getValue())));
    System.out.println();

    System.out.println("9. TOP SELLING MODELS BY VOLUME (Top 10)");
    CarAnalytics.topMakeModelsByVolume(cars, 10)
            .forEach(entry ->
                    System.out.printf("   %-30s -> %,d units%n", entry.getKey(), entry.getValue()));
    System.out.println();

    System.out.println("10. SALES VOLUME BY BODY STYLE");
    CarAnalytics.sortByLongValueDesc(CarAnalytics.countByBody(cars))
            .forEach((body, count) ->
                    System.out.printf("   %-15s -> %,d vehicles%n", body, count));
    System.out.println();

    System.out.println("11. BEST DEALS - VEHICLES WITH 10%+ DISCOUNT (Top 10)");
    List<CarPricePOJO> bestDeals = CarAnalytics.findBestDeals(cars, 10.0);
    bestDeals.stream()
            .limit(10)
            .forEach(car -> {
              double savings = car.getMmr() - car.getSellingPrice();
              double percent = (savings / car.getMmr()) * 100;
              System.out.printf("   %d %s %s - Save %s (%.1f%% off)%n",
                      car.getYear(), car.getMake(), car.getModel(),
                      CarAnalytics.formatPrice(savings), percent);
            });
    System.out.println();

    System.out.println("12. VALUE RETENTION BY MAKE (Price per year of age, Top 10)");
    Map<String, Double> depreciation = CarAnalytics.depreciationRateByMake(cars, 2025);
    CarAnalytics.sortByDoubleValueDesc(depreciation).entrySet().stream()
            .limit(10)
            .forEach(e -> System.out.printf("   %-15s -> %s per year%n",
                    e.getKey(), CarAnalytics.formatPrice(e.getValue())));
    System.out.println();

    System.out.println("13. SEASONAL PRICING TRENDS (Best months to buy)");
    Map<Month, Double> seasonal = CarAnalytics.averagePriceByMonth(cars);
    seasonal.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .limit(3)
            .forEach(e -> System.out.printf("   %-10s -> %s%n",
                    e.getKey(), CarAnalytics.formatPrice(e.getValue())));
    System.out.println();

    System.out.println("14. PRICE BY MILEAGE BRACKET");
    Map<String, Double> mileageImpact = CarAnalytics.priceByMileageBracket(cars);
    mileageImpact.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .forEach(e -> System.out.printf("   %-20s -> %s%n",
                    e.getKey(), CarAnalytics.formatPrice(e.getValue())));
    System.out.println();

    System.out.println("15. PRICE BY CONDITION RATING");
    Map<Integer, DoubleSummaryStatistics> conditionStats =
            CarAnalytics.priceStatsByCondition(cars);
    conditionStats.forEach((condition, stats) ->
            System.out.printf("   Condition %d: %s%n", condition,
                    CarAnalytics.formatStats(stats)));
    System.out.println();

    System.out.println("16. REGIONAL PRICE VARIATIONS (Top 3 models, Top 5 states each)");
    List<String> topModels = CarAnalytics.topMakeModelsByVolume(cars, 3).stream()
            .map(Map.Entry::getKey)
            .toList();
    Map<String, Map<String, Double>> regional =
            CarAnalytics.regionalPriceByModel(cars, topModels);
    regional.forEach((model, stateMap) -> {
      System.out.println("   " + model + ":");
      stateMap.entrySet().stream()
              .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
              .limit(5)
              .forEach(e -> System.out.printf("      %-5s -> %s%n",
                      e.getKey(), CarAnalytics.formatPrice(e.getValue())));
    });
    System.out.println();


    System.out.println("17. SELLER MARKUP PERCENTAGE (vs MMR, Top 10)");
    Map<String, Double> sellerMarkup = CarAnalytics.sellerMarkupPercentage(cars);
    CarAnalytics.sortByDoubleValueDesc(sellerMarkup).entrySet().stream()
            .limit(10)
            .forEach(e -> System.out.printf("   %-20s -> %+.2f%%%n", e.getKey(), e.getValue()));
    System.out.println();


    System.out.println("18. INVENTORY PROFITABILITY RANKING (Top 15)");
    List<CarAnalytics.InventoryMetrics> profitable =
            CarAnalytics.inventoryProfitabilityRanking(cars, 15);
    int rank = 1;
    for (CarAnalytics.InventoryMetrics metric : profitable) {
      System.out.printf("   %2d. %s%n", rank++, metric);
    }
    System.out.println();

    System.out.println("19. MARKET SUMMARY");
    System.out.printf("   Overall average: %s%n",
            CarAnalytics.formatPrice(CarAnalytics.averageSellingPrice(cars)));
    System.out.println("   Top 5 makes by volume:");
    Map<String, Long> makeVolume = cars.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                    CarPricePOJO::getMake,
                    java.util.stream.Collectors.counting()
            ));
    CarAnalytics.sortByLongValueDesc(makeVolume).entrySet().stream()
            .limit(5)
            .forEach(e -> System.out.printf("      %-15s -> %,d units%n", e.getKey(), e.getValue()));
  }
}