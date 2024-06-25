package main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherDataFetcher {

    private static final String BASE_URL = "https://api.open-meteo.com/v1/forecast";
    private static final HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) throws IOException, InterruptedException {
        Map<String, double[]> cities = readCities("capitais.txt");
        String startDate = "2024-01-01";
        String endDate = "2024-01-31";

        // Executar experimentos
        runExperiments(cities, startDate, endDate);
    }

    private static Map<String, double[]> readCities(String filePath) throws IOException {
        Map<String, double[]> cities = new HashMap<>();
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        for (String line : lines) {
            String[] parts = line.split(",");
            String city = parts[0];
            double latitude = Double.parseDouble(parts[1]);
            double longitude = Double.parseDouble(parts[2]);
            cities.put(city, new double[]{latitude, longitude});
        }

        return cities;
    }

    private static Map<String, Map<String, double[]>> fetchWeatherData(String city, double latitude, double longitude, String startDate, String endDate) throws IOException, InterruptedException {
        String url = String.format("%s?latitude=%f&longitude=%f&start_date=%s&end_date=%s&hourly=temperature_2m&timezone=America/Sao_Paulo", BASE_URL, latitude, longitude, startDate, endDate);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject data = new JSONObject(response.body());
        return processData(data);
    }

    private static Map<String, Map<String, double[]>> processData(JSONObject data) {
        JSONArray hourlyTemps = data.getJSONObject("hourly").getJSONArray("temperature_2m");
        JSONArray hourlyTime = data.getJSONObject("hourly").getJSONArray("time");

        Map<String, Map<String, double[]>> result = new HashMap<>();
        Map<LocalDate, double[]> dailyTemps = new HashMap<>();

        for (int i = 0; i < hourlyTemps.length(); i++) {
            double temp = hourlyTemps.getDouble(i);
            String timeStr = hourlyTime.getString(i);
            LocalDate date = LocalDate.parse(timeStr.split("T")[0]);

            dailyTemps.putIfAbsent(date, new double[]{Double.MAX_VALUE, Double.MIN_VALUE, 0, 0});
            double[] temps = dailyTemps.get(date);
            
            temps[0] = Math.min(temps[0], temp); // min
            temps[1] = Math.max(temps[1], temp); // max
            temps[2] += temp; // sum
            temps[3] += 1; // count
        }

        for (Map.Entry<LocalDate, double[]> entry : dailyTemps.entrySet()) {
            LocalDate date = entry.getKey();
            double[] temps = entry.getValue();
            double avg = temps[2] / temps[3]; // average
            result.put(date.toString(), Map.of(
                    "min", new double[]{temps[0]},
                    "max", new double[]{temps[1]},
                    "avg", new double[]{avg}
            ));
        }

        return result;
    }

    private static void runExperiments(Map<String, double[]> cities, String startDate, String endDate) throws InterruptedException {
        List<Runnable> versions = List.of(
                () -> runVersionNoThreads(cities, startDate, endDate),
                () -> runVersionWithThreads(cities, startDate, endDate, 3),
                () -> runVersionWithThreads(cities, startDate, endDate, 9),
                () -> runVersionWithThreads(cities, startDate, endDate, 27)
        );

        for (Runnable version : versions) {
            double totalTime = 0;
            for (int i = 0; i < 10; i++) {
                long startTime = System.currentTimeMillis();
                version.run();
                long endTime = System.currentTimeMillis();
                totalTime += (endTime - startTime);
            }
            double avgTime = totalTime / 10;
            System.out.println("Average Time: " + avgTime + " ms");
        }
    }

    private static void runVersionNoThreads(Map<String, double[]> cities, String startDate, String endDate) {
        cities.forEach((city, coords) -> {
            try {
                Map<String, Map<String, double[]>> result = fetchWeatherData(city, coords[0], coords[1], startDate, endDate);
                System.out.println("Processed data for " + city);
                result.forEach((date, temps) -> {
                    System.out.printf("Date: %s, Min: %.2f, Max: %.2f, Avg: %.2f%n", date, temps.get("min")[0], temps.get("max")[0], temps.get("avg")[0]);
                });
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private static void runVersionWithThreads(Map<String, double[]> cities, String startDate, String endDate, int numThreads) {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Callable<Void>> tasks = new ArrayList<>();

        cities.forEach((city, coords) -> tasks.add(() -> {
            try {
                Map<String, Map<String, double[]>> result = fetchWeatherData(city, coords[0], coords[1], startDate, endDate);
                System.out.println("Processed data for " + city);
                result.forEach((date, temps) -> {
                    System.out.printf("Date: %s, Min: %.2f, Max: %.2f, Avg: %.2f%n", date, temps.get("min")[0], temps.get("max")[0], temps.get("avg")[0]);
                });
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }));

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }

        try {
            if (!executor.awaitTermination(1, TimeUnit.HOURS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
