package main;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

public class ExperimentWithThreads implements Experiment {
    private List<Capital> capitals;
    private int numberOfThreads;

    public ExperimentWithThreads(List<Capital> capitals, int numberOfThreads) {
        this.capitals = capitals;
        this.numberOfThreads = numberOfThreads;
    }

    public void runExperimentOnce() {
        long startTime = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        for (Capital capital : capitals) {
            executor.submit(() -> {
                try {
                    JSONObject weatherData = WeatherRequester.getWeather(capital.getLatitude(), capital.getLongitude());
                    double[] temps = WeatherRequester.processWeatherData(weatherData);
                    System.out.println("Capital: " + capital.getName() + " Média: " + temps[0] + " Mínima: " + temps[1] + " Máxima: " + temps[2]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Tempo de execução: " + (endTime - startTime) + "ms");
    }
}
