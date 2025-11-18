package com.a2;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Loads data from CSV File (car_price.csv)
 */
public class CarCsvLoader {

  private static final DateTimeFormatter SALEDATE_FORMATTER =
          DateTimeFormatter.ofPattern("EEE MMM dd yyyy HH:mm:ss", Locale.ENGLISH);

  public List<CarPricePOJO> load(Path csvPath) throws IOException {
    try (Stream<String> lines = Files.lines(csvPath)) {
      return lines
              .skip(1)
              .filter(line -> !line.isBlank())
              .map(this::parseLine)
              .collect(Collectors.toList());
    }
  }


  private CarPricePOJO parseLine(String line) {
    String[] parts = line.split(",", -1);
    if (parts.length != 16) {
      throw new IllegalArgumentException("Invalid CSV line (expected 16 columns): " + line);
    }

    int index = 0;
    int year = parseIntSafe(parts[index++]);
    String make = parts[index++].trim();
    String model = parts[index++].trim();
    String trim = parts[index++].trim();
    String body = normalizeBodyStyle(parts[index++].trim());
    String transmission = parts[index++].trim();
    String vin = parts[index++].trim();
    String state = parts[index++].trim();
    int condition = parseIntSafe(parts[index++]);
    int odometer = parseIntSafe(parts[index++]);
    String color = parts[index++].trim();
    String interior = parts[index++].trim();
    String seller = parts[index++].trim();
    int mmr = parseIntSafe(parts[index++]);
    int sellingPrice = parseIntSafe(parts[index++]);
    String saledateRaw = parts[index].trim();

    LocalDateTime saleDateTime = parseSaleDateTimeSafe(saledateRaw);

    return new CarPricePOJO(
            year,
            make,
            model,
            trim,
            body,
            transmission,
            vin,
            state,
            condition,
            odometer,
            color,
            interior,
            seller,
            mmr,
            sellingPrice,
            saleDateTime
    );
  }

  private String normalizeBodyStyle(String body) {
    if (body == null || body.isEmpty()) {
      return "Unknown";
    }
    return toTitleCase(body);
  }


  private String toTitleCase(String text) {
    if (text == null || text.isEmpty()) {
      return text;
    }

    StringBuilder result = new StringBuilder();
    boolean capitalizeNext = true;

    for (char c : text.toCharArray()) {
      if (Character.isWhitespace(c) || c == '-') {
        result.append(c);
        capitalizeNext = true;
      } else if (capitalizeNext) {
        result.append(Character.toUpperCase(c));
        capitalizeNext = false;
      } else {
        result.append(Character.toLowerCase(c));
      }
    }

    return result.toString();
  }

  private int parseIntSafe(String s) {
    String trimmed = s.trim();
    return trimmed.isEmpty() || trimmed.equals("—")
            ? 0
            : Integer.parseInt(trimmed);
  }

  private LocalDateTime parseSaleDateTimeSafe(String s) {
    return s == null || s.length() < 24
            ? LocalDateTime.of(1970, 1, 1, 0, 0)
            : LocalDateTime.parse(s.substring(0, 24), SALEDATE_FORMATTER);
  }
}