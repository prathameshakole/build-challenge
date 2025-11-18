package com.a2;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for analytics method in CarAnalytics
 */
public class CarAnalyticsTest {

  private List<CarPricePOJO> sampleCars() {
    CarPricePOJO c1 = new CarPricePOJO(
            2010, "Toyota", "Camry", "LE", "Sedan", "Automatic",
            "VIN1", "CA", 4, 60000, "Blue", "Cloth", "DealerA",
            8000, 8500,
            LocalDateTime.of(2014, 1, 10, 10, 0)
    );
    CarPricePOJO c2 = new CarPricePOJO(
            2012, "Toyota", "Corolla", "S", "Sedan", "Automatic",
            "VIN2", "CA", 3, 40000, "Red", "Cloth", "DealerB",
            7000, 9000,
            LocalDateTime.of(2014, 2, 5, 15, 30)
    );
    CarPricePOJO c3 = new CarPricePOJO(
            2014, "Honda", "Civic", "LX", "Sedan", "Manual",
            "VIN3", "TX", 5, 30000, "Black", "Leather", "DealerC",
            10000, 12000,
            LocalDateTime.of(2014, 2, 20, 9, 0)
    );
    CarPricePOJO c4 = new CarPricePOJO(
            2015, "Honda", "Accord", "EX", "Sedan", "Automatic",
            "VIN4", "TX", 4, 0, "White", "Cloth", "DealerD",
            11000, 13000,
            LocalDateTime.of(2014, 3, 1, 11, 15)
    );
    return List.of(c1, c2, c3, c4);
  }

  private List<CarPricePOJO> extendedSampleCars() {
    List<CarPricePOJO> base = sampleCars();
    CarPricePOJO c5 = new CarPricePOJO(
            2013, "Ford", "F-150", "XLT", "Truck", "Automatic",
            "VIN5", "FL", 3, 75000, "Silver", "Cloth", "DealerE",
            15000, 14000,
            LocalDateTime.of(2014, 6, 15, 14, 0)
    );
    CarPricePOJO c6 = new CarPricePOJO(
            2011, "Honda", "Civic", "EX", "Sedan", "Manual",
            "VIN6", "NY", 4, 85000, "Blue", "Leather", "DealerF",
            6000, 6500,
            LocalDateTime.of(2014, 9, 22, 10, 30)
    );
    return List.of(base.get(0), base.get(1), base.get(2), base.get(3), c5, c6);
  }

  /**
   * Verifies average selling price calculation across all cars.
   */
  @Test
  void testAverageSellingPrice() {
    List<CarPricePOJO> cars = sampleCars();
    double avg = CarAnalytics.averageSellingPrice(cars);
    double expected = (8500 + 9000 + 12000 + 13000) / 4.0;
    assertEquals(expected, avg, 1e-6);
  }

  /**
   * Verifies average selling price grouped by make.
   */
  @Test
  void testAverageSellingPriceByMake() {
    List<CarPricePOJO> cars = sampleCars();
    Map<String, Double> byMake = CarAnalytics.averageSellingPriceByMake(cars);
    assertEquals(8750.0, byMake.get("Toyota"), 1e-6);
    assertEquals(12500.0, byMake.get("Honda"), 1e-6);
    assertEquals(2, byMake.size());
  }

  /**
   * Verifies average selling price grouped by make and model combination.
   */
  @Test
  void testAverageSellingPriceByMakeModel() {
    List<CarPricePOJO> cars = extendedSampleCars();
    Map<String, Double> byMakeModel = CarAnalytics.averageSellingPriceByMakeModel(cars);
    assertEquals(8500.0, byMakeModel.get("Toyota Camry"), 1e-6);
    assertEquals(9000.0, byMakeModel.get("Toyota Corolla"), 1e-6);
    assertEquals((12000 + 6500) / 2.0, byMakeModel.get("Honda Civic"), 1e-6);
  }

  /**
   * Verifies sales count grouped by state.
   */
  @Test
  void testSalesCountByState() {
    List<CarPricePOJO> cars = sampleCars();
    Map<String, Long> countByState = CarAnalytics.salesCountByState(cars);
    assertEquals(2L, countByState.get("CA"));
    assertEquals(2L, countByState.get("TX"));
    assertEquals(2, countByState.size());
  }

  /**
   * Verifies average odometer reading by make excluding zero values.
   */
  @Test
  void testAverageOdometerByMake() {
    List<CarPricePOJO> cars = sampleCars();
    Map<String, Double> avgOdoByMake = CarAnalytics.averageOdometerByMake(cars);
    assertEquals(50000.0, avgOdoByMake.get("Toyota"), 1e-6);
    assertEquals(30000.0, avgOdoByMake.get("Honda"), 1e-6);
  }

  /**
   * Verifies price delta statistics grouped by make.
   */
  @Test
  void testPriceDeltaStatsByMake() {
    List<CarPricePOJO> cars = sampleCars();
    Map<String, DoubleSummaryStatistics> statsByMake = CarAnalytics.priceDeltaStatsByMake(cars);

    DoubleSummaryStatistics toyotaStats = statsByMake.get("Toyota");
    DoubleSummaryStatistics hondaStats = statsByMake.get("Honda");

    assertNotNull(toyotaStats);
    assertEquals(2L, toyotaStats.getCount());
    assertEquals(500.0, toyotaStats.getMin(), 1e-6);
    assertEquals(2000.0, toyotaStats.getMax(), 1e-6);

    assertNotNull(hondaStats);
    assertEquals(2L, hondaStats.getCount());
    assertEquals(2000.0, hondaStats.getMin(), 1e-6);
    assertEquals(2000.0, hondaStats.getMax(), 1e-6);
  }

  /**
   * Verifies sales count grouped by vehicle year.
   */
  @Test
  void testSalesCountByYear() {
    List<CarPricePOJO> cars = sampleCars();
    Map<Integer, Long> countByYear = CarAnalytics.salesCountByYear(cars);
    assertEquals(1L, countByYear.get(2010));
    assertEquals(1L, countByYear.get(2012));
    assertEquals(1L, countByYear.get(2014));
    assertEquals(1L, countByYear.get(2015));
  }

  /**
   * Verifies average selling price grouped by year-month of sale.
   */
  @Test
  void testAveragePriceByYearMonth() {
    List<CarPricePOJO> cars = sampleCars();
    Map<YearMonth, Double> avgByMonth = CarAnalytics.averagePriceByYearMonth(cars);
    assertEquals(8500.0, avgByMonth.get(YearMonth.of(2014, 1)), 1e-6);
    assertEquals((9000 + 12000) / 2.0, avgByMonth.get(YearMonth.of(2014, 2)), 1e-6);
    assertEquals(13000.0, avgByMonth.get(YearMonth.of(2014, 3)), 1e-6);
  }

  /**
   * Verifies top N make-model combinations by sales volume.
   */
  @Test
  void testTopMakeModelsByVolume() {
    List<CarPricePOJO> cars = sampleCars();
    var top = CarAnalytics.topMakeModelsByVolume(cars, 2);
    assertEquals(2, top.size());
    long allCountsAreOne = top.stream()
            .filter(e -> e.getValue() == 1L)
            .count();
    assertEquals(2L, allCountsAreOne);
  }

  /**
   * Verifies count of cars grouped by body style.
   */
  @Test
  void testCountByBody() {
    List<CarPricePOJO> cars = sampleCars();
    Map<String, Long> countByBody = CarAnalytics.countByBody(cars);
    assertEquals(1, countByBody.size());
    assertEquals(4L, countByBody.get("Sedan"));
  }

  /**
   * Verifies sorting of map by double values in descending order.
   */
  @Test
  void testSortByDoubleValueDesc() {
    Map<String, Double> input = Map.of("A", 10.0, "B", 30.0, "C", 20.0);
    Map<String, Double> sorted = CarAnalytics.sortByDoubleValueDesc(input);
    List<String> keys = List.copyOf(sorted.keySet());
    assertEquals("B", keys.get(0));
    assertEquals("C", keys.get(1));
    assertEquals("A", keys.get(2));
  }

  /**
   * Verifies sorting of map by long values in descending order.
   */
  @Test
  void testSortByLongValueDesc() {
    Map<String, Long> input = Map.of("X", 5L, "Y", 15L, "Z", 10L);
    Map<String, Long> sorted = CarAnalytics.sortByLongValueDesc(input);
    List<String> keys = List.copyOf(sorted.keySet());
    assertEquals("Y", keys.get(0));
    assertEquals("Z", keys.get(1));
    assertEquals("X", keys.get(2));
  }

  /**
   * Verifies identification of best deals based on discount percentage.
   */
  @Test
  void testFindBestDeals() {
    List<CarPricePOJO> cars = extendedSampleCars();
    List<CarPricePOJO> deals = CarAnalytics.findBestDeals(cars, 5.0);
    assertTrue(deals.size() > 0);
    for (CarPricePOJO car : deals) {
      double discount = ((double)(car.getMmr() - car.getSellingPrice()) / car.getMmr()) * 100;
      assertTrue(discount >= 5.0);
    }
  }

  /**
   * Verifies depreciation rate calculation by make.
   */
  @Test
  void testDepreciationRateByMake() {
    List<CarPricePOJO> cars = sampleCars();
    Map<String, Double> depRate = CarAnalytics.depreciationRateByMake(cars, 2015);
    assertNotNull(depRate.get("Toyota"));
    assertNotNull(depRate.get("Honda"));
    assertTrue(depRate.get("Toyota") > 0);
  }

  /**
   * Verifies average selling price grouped by month for seasonal trends.
   */
  @Test
  void testAveragePriceByMonth() {
    List<CarPricePOJO> cars = sampleCars();
    Map<Month, Double> avgByMonth = CarAnalytics.averagePriceByMonth(cars);
    assertEquals(8500.0, avgByMonth.get(Month.JANUARY), 1e-6);
    assertEquals((9000 + 12000) / 2.0, avgByMonth.get(Month.FEBRUARY), 1e-6);
    assertEquals(13000.0, avgByMonth.get(Month.MARCH), 1e-6);
  }

  /**
   * Verifies average price grouped by mileage brackets.
   */
  @Test
  void testPriceByMileageBracket() {
    List<CarPricePOJO> cars = extendedSampleCars();
    Map<String, Double> priceByMileage = CarAnalytics.priceByMileageBracket(cars);
    assertTrue(priceByMileage.containsKey("30k-40k miles"));
    assertTrue(priceByMileage.containsKey("40k-50k miles"));
    assertTrue(priceByMileage.size() > 0);
  }

  /**
   * Verifies price statistics grouped by condition rating.
   */
  @Test
  void testPriceStatsByCondition() {
    List<CarPricePOJO> cars = sampleCars();
    Map<Integer, DoubleSummaryStatistics> statsByCondition = CarAnalytics.priceStatsByCondition(cars);
    assertTrue(statsByCondition.containsKey(3));
    assertTrue(statsByCondition.containsKey(4));
    assertTrue(statsByCondition.containsKey(5));
    DoubleSummaryStatistics cond4 = statsByCondition.get(4);
    assertEquals(2L, cond4.getCount());
  }

  /**
   * Verifies regional price comparison for specified models.
   */
  @Test
  void testRegionalPriceByModel() {
    List<CarPricePOJO> cars = extendedSampleCars();
    List<String> topModels = List.of("Honda Civic", "Toyota Camry");
    Map<String, Map<String, Double>> regionalPrices = CarAnalytics.regionalPriceByModel(cars, topModels);
    assertTrue(regionalPrices.containsKey("Honda Civic"));
    assertTrue(regionalPrices.containsKey("Toyota Camry"));
  }

  /**
   * Verifies color premium/discount percentage calculation.
   */
  @Test
  void testColorPremiumPercentage() {
    List<CarPricePOJO> cars = sampleCars();
    Map<String, Double> colorPremium = CarAnalytics.colorPremiumPercentage(cars);
    assertTrue(colorPremium.containsKey("Blue"));
    assertTrue(colorPremium.containsKey("Red"));
    assertTrue(colorPremium.containsKey("Black"));
    assertTrue(colorPremium.containsKey("White"));
  }

  /**
   * Verifies seller markup percentage calculation.
   */
  @Test
  void testSellerMarkupPercentage() {
    List<CarPricePOJO> cars = sampleCars();
    Map<String, Double> sellerMarkup = CarAnalytics.sellerMarkupPercentage(cars);
    assertTrue(sellerMarkup.containsKey("DealerA"));
    assertTrue(sellerMarkup.containsKey("DealerB"));
    assertTrue(sellerMarkup.containsKey("DealerC"));
    assertTrue(sellerMarkup.containsKey("DealerD"));
  }

  /**
   * Verifies transmission type comparison with price and count statistics.
   */
  @Test
  void testTransmissionComparison() {
    List<CarPricePOJO> cars = sampleCars();
    Map<String, CarAnalytics.TransmissionStats> transStats = CarAnalytics.transmissionComparison(cars);
    assertTrue(transStats.containsKey("Automatic"));
    assertTrue(transStats.containsKey("Manual"));

    CarAnalytics.TransmissionStats autoStats = transStats.get("Automatic");
    assertEquals(3L, autoStats.count);
    assertTrue(autoStats.avgPrice > 0);

    CarAnalytics.TransmissionStats manualStats = transStats.get("Manual");
    assertEquals(1L, manualStats.count);
  }

  /**
   * Verifies inventory profitability ranking by combined volume and profit metrics.
   */
  @Test
  void testInventoryProfitabilityRanking() {
    List<CarPricePOJO> cars = extendedSampleCars();
    List<CarAnalytics.InventoryMetrics> ranking = CarAnalytics.inventoryProfitabilityRanking(cars, 3);
    assertTrue(ranking.size() <= 3);

    for (CarAnalytics.InventoryMetrics metric : ranking) {
      assertNotNull(metric.makeModel);
      assertTrue(metric.salesCount > 0);
      assertTrue(metric.profitabilityScore >= 0);
    }

    // Verify descending order by profitability score
    for (int i = 0; i < ranking.size() - 1; i++) {
      assertTrue(ranking.get(i).profitabilityScore >= ranking.get(i + 1).profitabilityScore);
    }
  }

  /**
   * Verifies price formatting with commas.
   */
  @Test
  void testFormatPrice() {
    String formatted = CarAnalytics.formatPrice(12345.67);
    assertEquals("$12,346", formatted);
  }

}