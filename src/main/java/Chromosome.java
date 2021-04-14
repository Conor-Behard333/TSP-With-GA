import java.util.Arrays;
import java.util.Random;

public class Chromosome {
    int[] genes;

    public Chromosome(int chromosomeLength) {
        genes = new int[chromosomeLength];//Len: 14
        genes[0] = 0;
        genes[chromosomeLength - 1] = 0;
        for (int i = 1; i < chromosomeLength - 1; i++) {
            genes[i] = i;
        }

        shuffleGenes(40, genes);
    }

    public static void shuffleGenes(int numOfSwaps, int[] genes) {
        Random rand = new Random();
        for (int i = 0; i < numOfSwaps; i++) {
            int index = rand.nextInt(genes.length - 3) + 1;
            int newIndex = rand.nextInt(genes.length - 3) + 1;
            swap(genes, index, newIndex);
        }
    }

    private static void swap(int[] genes, int index, int newIndex) {
        int tmp = genes[index];
        genes[index] = genes[newIndex];
        genes[newIndex] = tmp;
    }

    public static int getIndexOfNumber(int[] genes, int numLookingFor) {
        for (int i = 0; i < genes.length; i++) {
            if (genes[i] == numLookingFor) {
                return i;
            }
        }
        return -1;
    }

    public Chromosome(int[] genes) {
        this.genes = genes;
    }

    public int[] getGenes() {
        return genes;
    }

    @Override
    public String toString() {
        return "Chromosome:" + Arrays.toString(genes);
    }
}
