package com.a2;

import java.time.LocalDateTime;

/**
 * POJO containing car attributes (year, make, model, condition) and sale data (price, MMR, date, location) along with other data.
 */
public class CarPricePOJO {

  private final int year;
  private final String make;
  private final String model;
  private final String trim;
  private final String body;
  private final String transmission;
  private final String vin;
  private final String state;
  private final int condition;
  private final int odometer;
  private final String color;
  private final String interior;
  private final String seller;
  private final int mmr;
  private final int sellingPrice;
  private final LocalDateTime saleDateTime;

  public CarPricePOJO (
          int year,
          String make,
          String model,
          String trim,
          String body,
          String transmission,
          String vin,
          String state,
          int condition,
          int odometer,
          String color,
          String interior,
          String seller,
          int mmr,
          int sellingPrice,
          LocalDateTime saleDateTime
  ) {
    this.year = year;
    this.make = make;
    this.model = model;
    this.trim = trim;
    this.body = body;
    this.transmission = transmission;
    this.vin = vin;
    this.state = state;
    this.condition = condition;
    this.odometer = odometer;
    this.color = color;
    this.interior = interior;
    this.seller = seller;
    this.mmr = mmr;
    this.sellingPrice = sellingPrice;
    this.saleDateTime = saleDateTime;
  }

  public int getYear() {
    return year;
  }

  public String getMake() {
    return make;
  }

  public String getModel() {
    return model;
  }

  public String getTrim() {
    return trim;
  }

  public String getBody() {
    return body;
  }

  public String getTransmission() {
    return transmission;
  }

  public String getVin() {
    return vin;
  }

  public String getState() {
    return state;
  }

  public int getCondition() {
    return condition;
  }

  public int getOdometer() {
    return odometer;
  }

  public String getColor() {
    return color;
  }

  public String getInterior() {
    return interior;
  }

  public String getSeller() {
    return seller;
  }

  public int getMmr() {
    return mmr;
  }

  public int getSellingPrice() {
    return sellingPrice;
  }

  public LocalDateTime getSaleDateTime() {
    return saleDateTime;
  }

  /**
   * Selling price minus MMR (market reference).
   * If mmr is 0, this returns 0 to avoid bogus large values or division by zero elsewhere.
   */
  public double getPriceDeltaFromMmr() {
    return mmr > 0 ? (sellingPrice - mmr) : 0.0;
  }

  /**
   * Combined key for (make, model).
   */
  public String getMakeModelKey() {
    return make + " " + model;
  }

  @Override
  public String toString() {
    return "CarPricePOJO{" +
            "year=" + year +
            ", make='" + make + '\'' +
            ", model='" + model + '\'' +
            ", trim='" + trim + '\'' +
            ", body='" + body + '\'' +
            ", transmission='" + transmission + '\'' +
            ", vin='" + vin + '\'' +
            ", state='" + state + '\'' +
            ", condition=" + condition +
            ", odometer=" + odometer +
            ", color='" + color + '\'' +
            ", interior='" + interior + '\'' +
            ", seller='" + seller + '\'' +
            ", mmr=" + mmr +
            ", sellingPrice=" + sellingPrice +
            ", saleDateTime=" + saleDateTime +
            '}';
  }
}