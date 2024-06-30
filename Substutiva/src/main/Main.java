package main;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Capital> capitals = Arrays.asList(
            new Capital("Aracaju", -10.9167, -37.05),
            new Capital("Belém", -1.4558, -48.5039),
            new Capital("Belo Horizonte", -19.9167, -43.9333),
            new Capital("Boa Vista", 2.81972, -60.67333),
            new Capital("Brasília", -15.7939, -47.8828),
            new Capital("Campo Grande", -20.44278, -54.64639),
            new Capital("Cuiabá", -15.5989, -56.0949),
            new Capital("Curitiba", -25.4297, -49.2711),
            new Capital("Florianópolis", -27.5935, -48.55854),
            new Capital("Fortaleza", -3.7275, -38.5275),
            new Capital("Goiânia", -16.6667, -49.25),
            new Capital("João Pessoa", -7.12, -34.88),
            new Capital("Macapá", 0.033, -51.05),
            new Capital("Maceió", -9.66583, -35.73528),
            new Capital("Manaus", -3.1189, -60.0217),
            new Capital("Natal", -5.7833, -35.2),
            new Capital("Palmas", -10.16745, -48.32766),
            new Capital("Porto Alegre", -30.0331, -51.23),
            new Capital("Porto Velho", -8.76194, -63.90389),
            new Capital("Recife", -8.05, -34.9),
            new Capital("Rio Branco", -9.97472, -67.81),
            new Capital("Rio de Janeiro", -22.9111, -43.2056),
            new Capital("Salvador", -12.9747, -38.4767),
            new Capital("São Luís", -2.5283, -44.3044),
            new Capital("São Paulo", -23.55, -46.6333),
            new Capital("Teresina", -5.08917, -42.80194),
            new Capital("Vitória", -20.2889, -40.3083)
        );

        System.out.println("Versão sem threads:");
        Experiment noThreads = new ExperimentNoThreads(capitals);
        noThreads.runExperimentOnce();

        System.out.println("Versão com 3 threads:");
        Experiment threeThreads = new ExperimentWithThreads(capitals, 3);
        threeThreads.runExperimentOnce();

        System.out.println("Versão com 9 threads:");
        Experiment nineThreads = new ExperimentWithThreads(capitals, 9);
        nineThreads.runExperimentOnce();

        System.out.println("Versão com 27 threads:");
        Experiment twentySevenThreads = new ExperimentWithThreads(capitals, 27);
        twentySevenThreads.runExperimentOnce();
    }
}
