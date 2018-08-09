import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * TCSS 343 Assignment 4.
 *
 * @author Evan Lindsay, Mary Fitzgerald
 */
public class tcss343 {

    /**
     * Random generator that uses the time at application start as the seed.
     */
    private static final Random RANDOM_GENERATOR = new Random(System.currentTimeMillis());

    /**
     * Minimum value we to be returned by the generator.
     */
    private static final int MIN_BOUND = 1;

    /**
     * Maximum value to be returned by the generator.
     */
    private static final int MAX_BOUND = 1000;

    /**
     * Generate a random integer from MIN_BOUND to MAX_BOUND (inclusive).
     *
     * @return a random integer
     */
    private static int generateInteger() {
        return RANDOM_GENERATOR.nextInt((MAX_BOUND - MIN_BOUND) + 1) + MIN_BOUND;
    }

    private static Result findCheapestRentalSequenceDivideConquer(Integer[][] costTable) {
        return findCheapestRentalSequenceDivideConquer(costTable, 0, 0);
    }

    private static Result findCheapestRentalSequenceDivideConquer(Integer[][] costTable, int currentIndex, int currentCost) {
        if (costTable.length - 1 == currentIndex) {
            return new Result(new ArrayList<>(Arrays.asList(currentIndex)), currentCost);
        } else {
            Result cheapestResult = null;

            for (int i = currentIndex + 1; i < costTable.length; i++) {
                Result result = findCheapestRentalSequenceDivideConquer(costTable, i, currentCost + costTable[currentIndex][i]);
                if (cheapestResult == null || result.totalCost < cheapestResult.totalCost) {
                    cheapestResult = result;
                }
            }

            cheapestResult.sequence.add(0, currentIndex);
            return cheapestResult;
        }
    }

    /**
     * An algorithm to find the cheapest sequence of posts to rent and return from using
     * the dynamic programming paradigm.
     *
     * @param costChart the cost chart
     *
     * @return the cheapest sequence of rentals
     */
    private static Result findCheapestRentalSequenceDynamic(Integer[][] costChart) {
        // Initialize minCost and path arrays with length equal to the number of posts (e.g. cost.length).
        int[] minCost = new int[costChart.length];
        int[] path = new int[costChart.length];
        // Fill minCost with max value.
        Arrays.fill(minCost, Integer.MAX_VALUE);
        // Initialize the base case (-1 for the end with a cost of 0).
        path[0] = -1;
        minCost[0] = 0;
        // Iterate over posts 1 to n.
        for (int j = 1; j < costChart.length; j++) {
            // Iterate posts 0 to j - 1.
            for (int i = 0; i < j; i++) {
                /*
                 If the minCost to post i plus the costs from post i to j is less than the
                 minimum cost to point j replace the min cost to j with the new minimum cost
                 and set the path at index j to i.
                  */
                if (minCost[i] + costChart[i][j] < minCost[j]) {
                    minCost[j] = minCost[i] + costChart[i][j];
                    path[j] = i;
                }
            }
        }

        // Initialize our sequence list.
        List<Integer> sequence = new ArrayList<>();
        // Initialize index equal to the number of posts minus 1.
        int i = costChart.length - 1;
        /*
        Check if the value at index i of the path array is our sentinel value. If not add the index to
        our sequence list and update our i to the value.
         */
        while (path[i] != -1) {
            sequence.add(i);
            i = path[i];
        }
        // Add the starting post to our sequence list.
        sequence.add(i);
        // Reverse the sequence list so that it is in order from start to finish.
        Collections.reverse(sequence);

        return new Result(sequence, minCost[costChart.length - 1]);
    }

    /**
     * Creates a cost table of size n by n and populates the table using
     * the given generation mode.
     *
     * @param n    the dimension of the table
     * @param mode the generation mode
     *
     * @return
     */
    public static Integer[][] generateCostTable(int n, GenerationMode mode) {
        // Create a table of size n by n
        Integer[][] table = new Integer[n][n];

        // Iterate from row 0 to n
        for (int i = 0; i < n; i++) {
            // Iterate from column i to n for the current row
            for (int j = i; j < n; j++) {
                // Fill the current index with a generated value
                mode.fill(table, i, j);
            }
        }

        return table;
    }

    /**
     * Divide and conquer algorithm test that finds the cheapest sequence
     * of rentals and prints it to the console.
     *
     * @param costChart the cost chart
     */
    public static void testDivideConquer(Integer[][] costChart) {
        // Find the cheapest sequence for the provided cost chart
        Result result = findCheapestRentalSequenceDivideConquer(costChart);

        // Print the sequence
        StringBuilder builder = new StringBuilder().append("Divide and Conquer Algorithm:\n")
                .append("Size: %s\n")
                .append("Total Cost: %s\n")
                .append("Sequence: %s");
        System.out.println(String.format(builder.toString(), costChart.length, result.totalCost, serializeResultSequence(result)));
    }

    /**
     * Dynamic programming algorithm test that finds the cheapest sequence
     * of rentals and prints it to the console.
     *
     * @param costChart the cost chart
     */
    public static void testDynamic(Integer[][] costChart) {
        // Find the cheapest sequence for the provided cost chart
        Result result = findCheapestRentalSequenceDynamic(costChart);

        // Print the sequence
        StringBuilder builder = new StringBuilder().append("Dynamic Programming Algorithm:\n")
                .append("Size: %s\n")
                .append("Total Cost: %s\n")
                .append("Sequence: %s");
        System.out.println(String.format(builder.toString(), costChart.length, result.totalCost, serializeResultSequence(result)));
    }

    public static String serializeResultSequence(Result result) {
        // Generate string representation of the sequence
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < result.sequence.size(); i++) {
            if (builder.length() > 0) {
                builder.append("->");
            }

            builder.append(result.sequence.get(i));
        }

        return builder.toString();
    }

    /**
     * Entry point for this program.
     *
     * @param args program arguments
     */
    public static void main(String... args) {
        Integer[][] tableSizeFourRandom = generateCostTable(4, GenerationMode.RANDOM);
        Integer[][] tableSizeFourDependent = generateCostTable(4, GenerationMode.DEPENDENT);

        System.out.println("+================= RANDOM =================+");

        testDivideConquer(tableSizeFourRandom);
        testDynamic(tableSizeFourRandom);

        System.out.println("+================ DEPENDENT ===============+");

        testDivideConquer(tableSizeFourDependent);
        testDynamic(tableSizeFourDependent);

        System.out.println("+==========================================+");
    }

    public enum GenerationMode {
        /**
         * Generates a random number.
         */
        RANDOM {
            public int nextInt(Integer[][] table, int row, int column) {
                return generateInteger();
            }
        },
        /**
         * Generates a random number and adds the previous value
         * of the corresponding row to that number.
         */
        DEPENDENT {
            public int nextInt(Integer[][] table, int row, int column) {
                int val = generateInteger();
                if (table[row][column - 1] != null) {
                    val += table[row][column - 1];
                }
                return val;
            }
        };

        /**
         * Generates an integer from the given table, row, and column.
         *
         * @param table  the table
         * @param row    the row index
         * @param column the column index
         */
        public abstract int nextInt(Integer[][] table, int row, int column);

        /**
         * Generates a number and populates the table at the corresponding
         * indices with the generated number.
         *
         * @param table  the table
         * @param row    the row index
         * @param column the column index
         */
        public void fill(Integer[][] table, int row, int column) {
            if (row == column) {
                table[row][column] = 0;
            } else {
                while (table[row][column] == null || table[row][column] <= 0) {
                    table[row][column] = nextInt(table, row, column);
                }
            }
        }
    }

    public static class Result {

        private List<Integer> sequence;
        private int totalCost;

        public Result(List<Integer> sequence, int totalCost) {
            this.sequence = sequence;
            this.totalCost = totalCost;
        }

    }
}
