package com.a2;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
/**
 * Tests CSV loading functionality for car price data.
 */
public class CarCsvLoaderTest {

  /**
   * Verifies that a single CSV record is correctly parsed into a CarPricePOJO object.
   */
  @Test
  void testLoadSingleRecord() throws IOException {
    Path tempCsv = Files.createTempFile("car_prices_test", ".csv");
    String header = String.join(",",
            "year", "make", "model", "trim", "body", "transmission",
            "vin", "state", "condition", "odometer", "color", "interior",
            "seller", "mmr", "sellingprice", "saledate"
    );
    String row = String.join(",",
            "2014",
            "Toyota",
            "Camry",
            "LE",
            "Sedan",
            "Automatic",
            "1NXBR32E54Z",
            "CA",
            "4",
            "60000",
            "Blue",
            "Cloth",
            "DealerA",
            "8000",
            "8500",
            "Tue Dec 16 2014 12:30:00 GMT-0800 (PST)"
    );

    Files.write(tempCsv, (header + System.lineSeparator() + row).getBytes());

    CarCsvLoader loader = new CarCsvLoader();
    var records = loader.load(tempCsv);

    assertEquals(1, records.size());
    CarPricePOJO r = records.get(0);

    assertEquals(2014, r.getYear());
    assertEquals("Toyota", r.getMake());
    assertEquals("Camry", r.getModel());
    assertEquals("LE", r.getTrim());
    assertEquals("Sedan", r.getBody());
    assertEquals("Automatic", r.getTransmission());
    assertEquals("1NXBR32E54Z", r.getVin());
    assertEquals("CA", r.getState());
    assertEquals(4, r.getCondition());
    assertEquals(60000, r.getOdometer());
    assertEquals("Blue", r.getColor());
    assertEquals("Cloth", r.getInterior());
    assertEquals("DealerA", r.getSeller());
    assertEquals(8000, r.getMmr());
    assertEquals(8500, r.getSellingPrice());

    LocalDateTime saleDate = r.getSaleDateTime();
    assertEquals(2014, saleDate.getYear());
    assertEquals(12, saleDate.getMonthValue());
    assertEquals(16, saleDate.getDayOfMonth());
    assertEquals(12, saleDate.getHour());
    assertEquals(30, saleDate.getMinute());
  }
}