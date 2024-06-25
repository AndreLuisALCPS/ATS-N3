package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.json.JSONObject;

public class Experiment {
    private List<Capital> capitals;
    private WeatherDataCollector collector;

    public Experiment(List<Capital> capitals) {
        this.capitals = capitals;
        this.collector = new WeatherDataCollector();
    }

    public void run(int numThreads) {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<?>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (Capital capital : capitals) {
            futures.add(executor.submit(() -> {
                try {
                    JSONObject data = collector.collectData(capital.getLatitude(), capital.getLongitude());
                    collector.processData(data, capital.getName());
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Execution time with " + numThreads + " threads: " + (endTime - startTime) + " ms");

        executor.shutdown();
    }

    public static void main(String[] args) {
        List<Capital> capitals = readCapitals();
        Experiment experiment = new Experiment(capitals);

        experiment.run(1);  // 1 thread
        experiment.run(3);  // 3 threads
        experiment.run(9);  // 9 threads
        experiment.run(27); // 27 threads
    }

    private static List<Capital> readCapitals() {
        List<Capital> capitals = new ArrayList<>();
        capitals.add(new Capital("Aracaju", -10.9167, -37.05));
        capitals.add(new Capital("Belém", -1.4558, -48.5039));
        capitals.add(new Capital("Belo Horizonte", -19.9167, -43.9333));
        capitals.add(new Capital("Boa Vista", 2.81972, -60.67333));
        capitals.add(new Capital("Brasília", -15.7939, -47.8828));
        capitals.add(new Capital("Campo Grande", -20.44278, -54.64639));
        capitals.add(new Capital("Cuiabá", -15.5989, -56.0949));
        capitals.add(new Capital("Curitiba", -25.4297, -49.2711));
        capitals.add(new Capital("Florianópolis", -27.5935, -48.55854));
        capitals.add(new Capital("Fortaleza", -3.7275, -38.5275));
        capitals.add(new Capital("Goiânia", -16.6667, -49.25));
        capitals.add(new Capital("João Pessoa", -7.12, -34.88));
        capitals.add(new Capital("Macapá", 0.033, -51.05));
        capitals.add(new Capital("Maceió", -9.66583, -35.73528));
        capitals.add(new Capital("Manaus", -3.1189, -60.0217));
        capitals.add(new Capital("Natal", -5.7833, -35.2));
        capitals.add(new Capital("Palmas", -10.16745, -48.32766));
        capitals.add(new Capital("Porto Alegre", -30.0331, -51.23));
        capitals.add(new Capital("Porto Velho", -8.76194, -63.90389));
        capitals.add(new Capital("Recife", -8.05, -34.9));
        capitals.add(new Capital("Rio Branco", -9.97472, -67.81));
        capitals.add(new Capital("Rio de Janeiro", -22.9111, -43.2056));
        capitals.add(new Capital("Salvador", -12.9747, -38.4767));
        capitals.add(new Capital("São Luís", -2.5283, -44.3044));
        capitals.add(new Capital("São Paulo", -23.55, -46.6333));
        capitals.add(new Capital("Teresina", -5.08917, -42.80194));
        capitals.add(new Capital("Vitória", -20.2889, -40.3083));
        return capitals;
    }
}
