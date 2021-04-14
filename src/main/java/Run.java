import java.util.Random;

public class Run {
    public static void main(String[] args) {
        //AIM TO GET 7293
        int chromosomeLength = 14;//13 states + the same state at the end
        for (int iter = 0; iter < 100; iter++) {
            //Phase 1:
            int populationSize = 150;
            //Possible selections methods: one-point, two-point, uniform (default is uniform)
            Population population = new Population(populationSize, chromosomeLength, "uniform", 100);

            for (int generation = 0; generation < 100; generation++) {
                //Phase 2
                population.evaluateFitness();

                //Phase 3:
                population.selectFittestIndividuals();

                //Phase 4 & 5:
                population.reproduce();

            }
            System.out.println("Best fitness score for iteration " + (iter + 1) + ": " + population.getFittestIndividual().getFitnessScore());
            System.out.println(population.getFittestIndividual());
            System.out.println();
        }
    }
}
