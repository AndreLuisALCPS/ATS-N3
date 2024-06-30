package main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherRequester {
    private static final String API_URL = "https://api.open-meteo.com/v1/forecast";

    public static JSONObject getWeather(double latitude, double longitude) throws Exception {
        String urlString = String.format(Locale.US, "%s?latitude=%.6f&longitude=%.6f&daily=temperature_2m_max,temperature_2m_min&timezone=America/Sao_Paulo", 
                                          API_URL, latitude, longitude);
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        connection.disconnect();

        return new JSONObject(content.toString());
    }

    public static double[] processWeatherData(JSONObject weatherData) {
        JSONArray maxTemps = weatherData.getJSONObject("daily").getJSONArray("temperature_2m_max");
        JSONArray minTemps = weatherData.getJSONObject("daily").getJSONArray("temperature_2m_min");

        double totalMax = 0, totalMin = 0, max = Double.MIN_VALUE, min = Double.MAX_VALUE;
        int count = maxTemps.length();

        for (int i = 0; i < count; i++) {
            double maxTemp = maxTemps.getDouble(i);
            double minTemp = minTemps.getDouble(i);
            totalMax += maxTemp;
            totalMin += minTemp;
            if (maxTemp > max) max = maxTemp;
            if (minTemp < min) min = minTemp;
        }

        double average = (totalMax + totalMin) / (2 * count);
        return new double[]{average, min, max};
    }
}
