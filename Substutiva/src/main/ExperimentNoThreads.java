package main;

import java.util.List;

import org.json.JSONObject;

public class ExperimentNoThreads implements Experiment {
    private List<Capital> capitals;

    public ExperimentNoThreads(List<Capital> capitals) {
        this.capitals = capitals;
    }

    public void runExperimentOnce() {
        long startTime = System.currentTimeMillis();
        for (Capital capital : capitals) {
            try {
                JSONObject weatherData = WeatherRequester.getWeather(capital.getLatitude(), capital.getLongitude());
                double[] temps = WeatherRequester.processWeatherData(weatherData);
                System.out.println("Capital: " + capital.getName() + " Média: " + temps[0] + " Mínima: " + temps[1] + " Máxima: " + temps[2]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Tempo de execução: " + (endTime - startTime) + "ms");
    }
}
