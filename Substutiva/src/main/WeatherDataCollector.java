package main;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherDataCollector {

    private static final String BASE_URL = "https://api.open-meteo.com/v1/forecast";
    private static final HttpClient client = HttpClient.newHttpClient();

    public JSONObject collectData(double latitude, double longitude) throws IOException, InterruptedException {
        String url = String.format("%s?latitude=%.6f&longitude=%.6f&start_date=2024-01-01&end_date=2024-01-31&hourly=temperature_2m&timezone=America/Sao_Paulo", BASE_URL, latitude, longitude);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Response for coordinates (" + latitude + ", " + longitude + "): " + response.body());
        return new JSONObject(response.body());
    }

    public void processData(JSONObject data, String cityName) {
        if (!data.has("hourly")) {
            System.err.println("No 'hourly' data found for city: " + cityName);
            return;
        }

        JSONObject hourlyData = data.getJSONObject("hourly");
        if (!hourlyData.has("temperature_2m") || !hourlyData.has("time")) {
            System.err.println("Incomplete 'hourly' data for city: " + cityName);
            return;
        }

        JSONArray hourlyTemps = hourlyData.getJSONArray("temperature_2m");
        JSONArray hourlyTime = hourlyData.getJSONArray("time");

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

        System.out.println("Processed data for " + cityName);
        for (Map.Entry<LocalDate, double[]> entry : dailyTemps.entrySet()) {
            LocalDate date = entry.getKey();
            double[] temps = entry.getValue();
            double avg = temps[2] / temps[3]; // average
            System.out.printf("Date: %s, Min: %.2f, Max: %.2f, Avg: %.2f%n", date, temps[0], temps[1], avg);
        }
    }
}
