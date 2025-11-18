package com.a2;

import java.time.YearMonth;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.Month;
import java.util.*;


/**
 * Pure analytical functions on top of List<CarPricePOJO>, using Streams.
 */
public class CarAnalytics {

  /**
   * Average selling price across all cars.
   */
  public static double averageSellingPrice(List<CarPricePOJO> cars) {
    return cars.stream()
            .mapToInt(CarPricePOJO::getSellingPrice)
            .average()
            .orElse(0.0);
  }

  /**
   * Returns average selling price grouped by make.
   */
  public static Map<String, Double> averageSellingPriceByMake(List<CarPricePOJO> cars) {
    return cars.stream()
            .collect(Collectors.groupingBy(
                    CarPricePOJO::getMake,
                    Collectors.averagingInt(CarPricePOJO::getSellingPrice)
            ));
  }

  /**
   * Returns average selling price grouped by make and model.
   */
  public static Map<String, Double> averageSellingPriceByMakeModel(List<CarPricePOJO> cars) {
    return cars.stream()
            .collect(Collectors.groupingBy(
                    CarPricePOJO::getMakeModelKey,
                    Collectors.averagingInt(CarPricePOJO::getSellingPrice)
            ));
  }

  /**
   * Returns count of sales grouped by state.
   */
  public static Map<String, Long> salesCountByState(List<CarPricePOJO> cars) {
    return cars.stream()
            .collect(Collectors.groupingBy(
                    CarPricePOJO::getState,
                    Collectors.counting()
            ));
  }

  /**
   * Returns average odometer reading by make, excluding invalid readings.
   */
  public static Map<String, Double> averageOdometerByMake(List<CarPricePOJO> cars) {
    return cars.stream()
            .filter(car -> car.getOdometer() > 0)
            .collect(Collectors.groupingBy(
                    CarPricePOJO::getMake,
                    Collectors.averagingInt(CarPricePOJO::getOdometer)
            ));
  }

  /**
   * Price delta (sellingPrice - mmr) statistics by make.
   * Only cars with mmr > 0 are included.
   */
  public static Map<String, DoubleSummaryStatistics> priceDeltaStatsByMake(List<CarPricePOJO> cars) {
    return cars.stream()
            .filter(car -> car.getMmr() > 0)
            .collect(Collectors.groupingBy(
                    CarPricePOJO::getMake,
                    Collectors.summarizingDouble(CarPricePOJO::getPriceDeltaFromMmr)
            ));
  }

  /**
   * Returns count of sales grouped by vehicle year.
   */
  public static Map<Integer, Long> salesCountByYear(List<CarPricePOJO> cars) {
    return cars.stream()
            .collect(Collectors.groupingBy(
                    CarPricePOJO::getYear,
                    Collectors.counting()
            ));
  }

  /**
   * Returns average selling price grouped by year-month of sale.
   */
  public static Map<YearMonth, Double> averagePriceByYearMonth(List<CarPricePOJO> cars) {
    return cars.stream()
            .collect(Collectors.groupingBy(
                    car -> YearMonth.from(car.getSaleDateTime()),
                    Collectors.averagingInt(CarPricePOJO::getSellingPrice)
            ));
  }

  /**
   * Returns top N make-model combinations by sales volume.
   */
  public static List<Map.Entry<String, Long>> topMakeModelsByVolume(List<CarPricePOJO> cars, int n) {
    return cars.stream()
            .collect(Collectors.groupingBy(
                    CarPricePOJO::getMakeModelKey,
                    Collectors.counting()
            ))
            .entrySet()
            .stream()
            .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
            .limit(n)
            .collect(Collectors.toList());
  }

  /**
   * Returns count of cars grouped by body style (Sedan, SUV, etc).
   */
  public static Map<String, Long> countByBody(List<CarPricePOJO> cars) {
    return cars.stream()
            .collect(Collectors.groupingBy(
                    CarPricePOJO::getBody,
                    Collectors.counting()
            ));
  }

  /**
   * Sorts map by double value in descending order.
   */
  public static <K> Map<K, Double> sortByDoubleValueDesc(Map<K, Double> input) {
    return input.entrySet().stream()
            .sorted(Map.Entry.<K, Double>comparingByValue(Comparator.reverseOrder()))
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (a, b) -> a,
                    LinkedHashMap::new
            ));
  }

  /**
   * Sorts map by long value in descending order.
   */
  public static <K> Map<K, Long> sortByLongValueDesc(Map<K, Long> input) {
    return input.entrySet().stream()
            .sorted(Map.Entry.<K, Long>comparingByValue(Comparator.reverseOrder()))
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (a, b) -> a,
                    LinkedHashMap::new
            ));
  }

  /**
   * Find vehicles selling significantly below MMR (potential bargains)
   * Returns cars where selling price is at least 10% below MMR
   */
  public static List<CarPricePOJO> findBestDeals(List<CarPricePOJO> cars, double thresholdPercent) {
    return cars.stream()
            .filter(car -> car.getMmr() > 0)
            .filter(car -> {
              double discount = ((double)(car.getMmr() - car.getSellingPrice()) / car.getMmr()) * 100;
              return discount >= thresholdPercent;
            })
            .sorted(Comparator.comparingDouble(car ->
                    ((CarPricePOJO)car).getMmr() - ((CarPricePOJO) car).getSellingPrice()).reversed())
            .collect(Collectors.toList());
  }

  /**
   * Returns average price per year of age by make to indicate depreciation rate.
   * Higher value = better value retention
   */
  public static Map<String, Double> depreciationRateByMake(List<CarPricePOJO> cars, int currentYear) {
    return cars.stream()
            .filter(car -> car.getYear() < currentYear)
            .collect(Collectors.groupingBy(
                    CarPricePOJO::getMake,
                    Collectors.averagingDouble(car -> {
                      int age = currentYear - car.getYear();
                      return age > 0 ? (double) car.getSellingPrice() / age : 0;
                    })
            ));
  }

  /**
   * Returns average selling price grouped by month to identify seasonal trends.
   */
  public static Map<Month, Double> averagePriceByMonth(List<CarPricePOJO> cars) {
    return cars.stream()
            .collect(Collectors.groupingBy(
                    car -> car.getSaleDateTime().getMonth(),
                    Collectors.averagingInt(CarPricePOJO::getSellingPrice)
            ));
  }

  /**
   * Returns average price grouped by 10k mile brackets to show depreciation by mileage
   */
  public static Map<String, Double> priceByMileageBracket(List<CarPricePOJO> cars) {
    return cars.stream()
            .filter(car -> car.getOdometer() > 0)
            .collect(Collectors.groupingBy(
                    car -> {
                      int bracket = (car.getOdometer() / 10000) * 10;
                      return bracket + "k-" + (bracket + 10) + "k miles";
                    },
                    Collectors.averagingInt(CarPricePOJO::getSellingPrice)
            ));
  }

  /**
   * Returns price statistics grouped by condition rating to show savings for lower condition
   */
  public static Map<Integer, DoubleSummaryStatistics> priceStatsByCondition(List<CarPricePOJO> cars) {
    return cars.stream()
            .collect(Collectors.groupingBy(
                    CarPricePOJO::getCondition,
                    TreeMap::new,
                    Collectors.summarizingDouble(CarPricePOJO::getSellingPrice)
            ));
  }

  /**
   * Returns average prices by state for specified popular models.
   */
  public static Map<String, Map<String, Double>> regionalPriceByModel(
          List<CarPricePOJO> cars, List<String> topModels) {
    return cars.stream()
            .filter(car -> topModels.contains(car.getMakeModelKey()))
            .collect(Collectors.groupingBy(
                    CarPricePOJO::getMakeModelKey,
                    Collectors.groupingBy(
                            CarPricePOJO::getState,
                            Collectors.averagingInt(CarPricePOJO::getSellingPrice)
                    )
            ));
  }

  /**
   * Calculate price premium/discount for each color vs overall average
   */
  public static Map<String, Double> colorPremiumPercentage(List<CarPricePOJO> cars) {
    double overallAvg = cars.stream()
            .mapToInt(CarPricePOJO::getSellingPrice)
            .average()
            .orElse(0.0);

    Map<String, Double> avgByColor = cars.stream()
            .collect(Collectors.groupingBy(
                    CarPricePOJO::getColor,
                    Collectors.averagingInt(CarPricePOJO::getSellingPrice)
            ));

    return avgByColor.entrySet().stream()
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> ((entry.getValue() - overallAvg) / overallAvg) * 100
            ));
  }

  /**
   * Average markup percentage (above/below MMR) by seller
   */
  public static Map<String, Double> sellerMarkupPercentage(List<CarPricePOJO> cars) {
    return cars.stream()
            .filter(car -> car.getMmr() > 0)
            .collect(Collectors.groupingBy(
                    CarPricePOJO::getSeller,
                    Collectors.averagingDouble(car ->
                            ((double)(car.getSellingPrice() - car.getMmr()) / car.getMmr()) * 100
                    )
            ));
  }

  /**
   * Returns average price and count statistics by transmission type.
   */
  public static class TransmissionStats {
    public double avgPrice;
    public long count;

    public TransmissionStats(double avgPrice, long count) {
      this.avgPrice = avgPrice;
      this.count = count;
    }

    @Override
    public String toString() {
      return String.format("Avg: $%.0f, Count: %d", avgPrice, count);
    }
  }

  /**
   * Returns average price and count statistics by transmission type.
   */
  public static Map<String, TransmissionStats> transmissionComparison(List<CarPricePOJO> cars) {
    Map<String, Double> avgPrice = cars.stream()
            .collect(Collectors.groupingBy(
                    CarPricePOJO::getTransmission,
                    Collectors.averagingInt(CarPricePOJO::getSellingPrice)
            ));

    Map<String, Long> count = cars.stream()
            .collect(Collectors.groupingBy(
                    CarPricePOJO::getTransmission,
                    Collectors.counting()
            ));

    return avgPrice.keySet().stream()
            .collect(Collectors.toMap(
                    key -> key,
                    key -> new TransmissionStats(avgPrice.get(key), count.get(key))
            ));
  }

  /**
   * Returns top N make-models ranked by profitability score combining volume and profit margin.
   */
  public static class InventoryMetrics {
    public String makeModel;
    public long salesCount;
    public double avgProfit;
    public double profitabilityScore;

    public InventoryMetrics(String makeModel, long salesCount, double avgProfit) {
      this.makeModel = makeModel;
      this.salesCount = salesCount;
      this.avgProfit = avgProfit;
      this.profitabilityScore = salesCount * (avgProfit / 1000.0);
    }

    @Override
    public String toString() {
      return String.format("%s: %d sales, $%.0f avg profit, Score: %.1f",
              makeModel, salesCount, avgProfit, profitabilityScore);
    }
  }

  public static List<InventoryMetrics> inventoryProfitabilityRanking(List<CarPricePOJO> cars, int topN) {
    Map<String, Long> volumeMap = cars.stream()
            .filter(car -> car.getMmr() > 0)
            .collect(Collectors.groupingBy(
                    CarPricePOJO::getMakeModelKey,
                    Collectors.counting()
            ));

    Map<String, Double> profitMap = cars.stream()
            .filter(car -> car.getMmr() > 0)
            .collect(Collectors.groupingBy(
                    CarPricePOJO::getMakeModelKey,
                    Collectors.averagingDouble(CarPricePOJO::getPriceDeltaFromMmr)
            ));

    return volumeMap.keySet().stream()
            .map(makeModel -> new InventoryMetrics(
                    makeModel,
                    volumeMap.get(makeModel),
                    profitMap.getOrDefault(makeModel, 0.0)
            ))
            .sorted(Comparator.comparingDouble(m -> -m.profitabilityScore))
            .limit(topN)
            .collect(Collectors.toList());
  }


  /**
   * HELPER: Format price with commas
   */
  public static String formatPrice(double price) {
    return String.format("$%,.0f", price);
  }

  /**
   * HELPER: Print DoubleSummaryStatistics nicely
   */
  public static String formatStats(DoubleSummaryStatistics stats) {
    return String.format("Min: %s, Avg: %s, Max: %s, Count: %d",
            formatPrice(stats.getMin()),
            formatPrice(stats.getAverage()),
            formatPrice(stats.getMax()),
            stats.getCount());
  }
}