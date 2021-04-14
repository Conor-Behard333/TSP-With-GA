import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * The type Population.
 */
public class Population {
    private Individual[] individuals;
    private final int POPULATION_SIZE;
    private final int CHROMOSOME_LENGTH;
    private final String SELECTION_METHOD;
    private final int MUTATION_PROB;

    /**
     * Instantiates a new Population.
     *
     * @param populationSize   the population size
     * @param chromosomeLength the chromosome length
     * @param selectionMethod  the selection method
     * @param mutationProb     the mutation prob
     */
    public Population(int populationSize, int chromosomeLength, String selectionMethod, int mutationProb) {
        CHROMOSOME_LENGTH = chromosomeLength;
        POPULATION_SIZE = populationSize;
        SELECTION_METHOD = selectionMethod;
        MUTATION_PROB = mutationProb;

        individuals = new Individual[populationSize];
        for (int i = 0; i < populationSize; i++) {
            individuals[i] = new Individual(chromosomeLength);
        }
    }

    /**
     * Evaluate fitness.
     */
    public void evaluateFitness() {
        FitnessCalculator fc = new FitnessCalculator();
        for (Individual individual : individuals) {
            int score = fc.evaluateFitness(individual.getChromosome());
            individual.setFitnessScore(score);
        }
    }

    private void killTheWeak(double averageFitness) {
        for (int i = 0; i < POPULATION_SIZE; i++) {
            if (individuals[i].getFitnessScore() > averageFitness && getLivingPopulation() > 2) {
                individuals[i] = null;
            }
        }
    }

    private double getAverageFitness() {
        //get the average fitness
        double averageFitness = 0;
        for (Individual individual : individuals) {
            averageFitness += individual.getFitnessScore();
        }
        averageFitness = Math.round(averageFitness / POPULATION_SIZE);
        return averageFitness;
    }

    /**
     * Select fittest individuals.
     */
    public void selectFittestIndividuals() {
        //Using basic average selection

        double averageFitness = getAverageFitness();

        //sort the arrays from largest to smallest
        Arrays.sort(individuals);

        //remove any individual whose fitness is less than the average
        killTheWeak(averageFitness);
    }

    /**
     * Reproduce.
     * Uses one-point crossover
     */
    public void reproduce() {
        Random rand = new Random();
        int[][] offspringsGenes = new int[getDeadPopulation()][CHROMOSOME_LENGTH];

        for (int i = 0; i < offspringsGenes.length; i++) {

            Individual[] parents = getParents(rand);

            //creates the offsprings genes using the parents genes
            createOffspringGenes(rand, offspringsGenes, i, parents);

            //chance to mute offsprings genes
            mutateOffspring(rand, offspringsGenes, i);
        }

        addOffspringsToPopulation(offspringsGenes);
    }

    private void mutateOffspring(Random rand, int[][] offspringsGenes, int offspringIndex) {
        if (rand.nextInt(99) + 1 < MUTATION_PROB) {
            //Phase 5
            Chromosome.shuffleGenes(1, offspringsGenes[offspringIndex]);
        }
    }

    private void createOffspringGenes(Random rand, int[][] offspringsGenes, int offspringIndex, Individual[] parents) {
        switch (SELECTION_METHOD) {
            case "one-point":
                onePointCrossover(offspringsGenes, offspringIndex, rand, parents[0], parents[1]);
                break;
            case "two-point":
                twoPointCrossover(offspringsGenes, offspringIndex, rand, parents[0], parents[1]);
                break;
            default:
                cycleCrossoverV2(offspringsGenes, offspringIndex, rand, parents[0], parents[1]);
//                cycleCrossover(offspringsGenes, offspringIndex, rand, parents[0], parents[1]);
        }
    }

    private void addOffspringsToPopulation(int[][] offspringsGenes) {
        int offspringIndex = 0;
        for (int i = 0; i < individuals.length; i++) {
            if (individuals[i] == null) {
                individuals[i] = new Individual(new Chromosome(offspringsGenes[offspringIndex]));
                offspringIndex++;
            }
        }
    }

    //https://www.hindawi.com/journals/cin/2017/7430125/ ---- implement proposed crossover technique
    private void cycleCrossover(int[][] offspringsGenes, int offspringIndex, Random rand, Individual parent1, Individual parent2) {
        int[] parent1Genes = parent1.getChromosome().getGenes();
        int[] parent2Genes = parent2.getChromosome().getGenes();

        int index = 1;
        while (parent1Genes[index] != offspringsGenes[offspringIndex][index]) {
            offspringsGenes[offspringIndex][index] = parent1Genes[index];
            int numPartner = parent2Genes[index];
            index = Chromosome.getIndexOfNumber(parent1Genes, numPartner);
        }

        for (int i = 1; i < offspringsGenes[offspringIndex].length - 1; i++) {
            if (offspringsGenes[offspringIndex][i] == 0) {
                offspringsGenes[offspringIndex][i] = parent2Genes[i];
            }
        }
    }

    private static void cycleCrossoverV2(int[][] offspringsGenes, int offspringIndex, Random rand, Individual parent1, Individual parent2) {
        int[] parent1Genes = parent1.getChromosome().getGenes();
        int[] parent2Genes =parent2.getChromosome().getGenes();


        int index = 1;
        for (int i = 1; i < offspringsGenes[offspringIndex].length - 1; i++) {
            if (Chromosome.getIndexOfNumber(offspringsGenes[offspringIndex], parent2Genes[index]) != -1) {
                index = 0;
                parent1Genes = getNewArray(offspringsGenes[offspringIndex], parent1Genes);
                parent2Genes = getNewArray(offspringsGenes[offspringIndex], parent2Genes);
            }
            offspringsGenes[offspringIndex][i] = parent2Genes[index];
            int numPartner = parent2Genes[index];
            index = Chromosome.getIndexOfNumber(parent1Genes, numPartner);
            index = Chromosome.getIndexOfNumber(parent1Genes, parent2Genes[index]);
            index = Chromosome.getIndexOfNumber(parent1Genes, parent2Genes[index]);

        }

    }

    private static int[] getNewArray(int[] offspringsGenes, int[] parentGenes) {
        ArrayList<Integer> tmp = new ArrayList<>();
        for (int parent1Gene : parentGenes) {
            if (Chromosome.getIndexOfNumber(offspringsGenes, parent1Gene) == -1) {
                tmp.add(parent1Gene);
            }
        }

        int[] rtn = new int[tmp.size()];
        for (int i = 0; i < rtn.length; i++) {
            rtn[i] = tmp.get(i);
        }
        return rtn;
    }

    private Individual[] getParents(Random rand) {

        int livingPopulation = getLivingPopulation();
        Individual parent1 = individuals[(individuals.length - 1) - rand.nextInt(livingPopulation)];
        Individual parent2;
        do {
            parent2 = individuals[(individuals.length - 1) - rand.nextInt(livingPopulation)];
        } while (parent1.equals(parent2));

        return new Individual[]{parent1, parent2};
    }

    private void onePointCrossover(int[][] offspringsGenes, int offspringIndex, Random rand, Individual parent1, Individual parent2) {
        int crossoverPoint = rand.nextInt(CHROMOSOME_LENGTH - 1);
        //go from start to crossover Point 1
        for (int i = 0; i < crossoverPoint + 1; i++) {
            offspringsGenes[offspringIndex][i] = parent1.getChromosome().getGenes()[i];
        }
        //go from crossover Point 1 to end
        for (int i = crossoverPoint + 1; i < CHROMOSOME_LENGTH; i++) {
            offspringsGenes[offspringIndex][i] = parent2.getChromosome().getGenes()[i];
        }
    }

    private void twoPointCrossover(int[][] offspringsGenes, int offspringIndex, Random rand, Individual parent1, Individual parent2) {
        int crossoverPoint1;
        int crossoverPoint2;
        do {
            crossoverPoint1 = rand.nextInt(CHROMOSOME_LENGTH / 2);
            crossoverPoint2 = (rand.nextInt(CHROMOSOME_LENGTH / 2) + CHROMOSOME_LENGTH / 2) - 1;
        } while (crossoverPoint2 < crossoverPoint1);

        //go from start to crossover Point 1
        for (int i = 0; i < crossoverPoint1 + 1; i++) {
            offspringsGenes[offspringIndex][i] = parent1.getChromosome().getGenes()[i];
        }

        //go from crossover Point 1 to crossover Point 2
        for (int i = crossoverPoint1 + 1; i <= crossoverPoint2; i++) {
            offspringsGenes[offspringIndex][i] = parent2.getChromosome().getGenes()[i];
        }

        //go from crossover Point 2 to end
        for (int i = crossoverPoint2 + 1; i < CHROMOSOME_LENGTH; i++) {
            offspringsGenes[offspringIndex][i] = parent1.getChromosome().getGenes()[i];
        }
    }

    private void mutate(int[] offspringsGene, int mutationIndex) {
        if (offspringsGene[mutationIndex] == '1') {
            offspringsGene[mutationIndex] = '0';
        } else {
            offspringsGene[mutationIndex] = '1';
        }
    }

    private int getLivingPopulation() {
        int alive = 0;
        for (Individual individual : individuals) {
            if (individual != null) {
                alive++;
            }
        }
        return alive;
    }

    private int getDeadPopulation() {
        int killed = 0;
        for (Individual individual : individuals) {
            if (individual == null) {
                killed++;
            }
        }
        return killed;
    }

    /**
     * Gets fittest individual.
     *
     * @return the fittest individual
     */
    public Individual getFittestIndividual() {
        return individuals[individuals.length - 1];
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        int index = 0;
        for (Individual individual : individuals) {
            if (individual != null) {
                result.append("i: ").append(index).append(", Fitness: ").append(individual.getFitnessScore()).append("\n");
            } else {
                result.append("i: ").append(index).append(", Fitness: ").append("Null\n");
            }
            index++;
        }
        return result.toString();
    }
}
