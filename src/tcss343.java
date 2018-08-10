import java.math.BigInteger;
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
     * BigInteger for the value 2.
     */
    private static final BigInteger TWO = new BigInteger("2");

    /**
     * Generate a random integer from MIN_BOUND to MAX_BOUND (inclusive).
     *
     * @return a random integer
     */
    private static int generateInteger() {
        return RANDOM_GENERATOR.nextInt((MAX_BOUND - MIN_BOUND) + 1) + MIN_BOUND;
    }

    /**
     * An algorithm to find the cheapest sequence of posts to rent and return from using
     * the brute force paradigm.
     *
     * @param costTable the cost chart
     *
     * @return the cheapest sequence of rentals
     */
    private static Result findCheapestRentalSequenceBruteForce(Integer[][] costTable) {
        /*
         We first calculate the size of the power set. It is important to use
         BigInteger because the power set size very quickly exceeds Integer.MAX_VALUE.
          */
        BigInteger powerSetSize = TWO.pow(costTable.length);
        /*
         Create a counter as a BigInteger to support loops of sizes greater than Integer.MAX_VALUE.
         Because we need bit costTable.length - 1 to be set we know that our counter can start at
         2^(costTable.length - 1).
          */
        BigInteger counter = TWO.pow(costTable.length - 1);

        List<Integer> cheapestSequence = null;
        int cheapestCost = -1;

        if (!counter.testBit(0)) {
            counter = counter.add(BigInteger.ONE);
        }

        // Iterate from 0 to powerSetSize - 1;
        while (counter.compareTo(powerSetSize) < 0) {
            // Create our sequence list and add the start index as the base case.
            List<Integer> sequence = new ArrayList<>();
            sequence.add(0);

            // Keep track of the totalCost and the previously checked post.
            int totalCost = 0;
            int previous = 0;

            // Iterate over all posts between the start and end.
            for (int j = 1; j < costTable.length - 1; j++) {
                // Check if the bit in the counter is set for post j.
                if (counter.testBit(j)) {
                    // Add the cost from the previous post to post j to the total cost.
                    totalCost += costTable[previous][j];
                    // Set the previous post to j.
                    previous = j;
                    // Add post j to the sequence list.
                    sequence.add(j);
                }
            }

            // Add the cost from the previous post to the end post to the total cost.
            totalCost += costTable[previous][costTable.length - 1];
            // Add the end post to the sequence list.
            sequence.add(costTable.length - 1);

                /*
                 Check if the cheapestCost has yet to be set or if the total cost of the latest sequence
                 is cheaper than the cost of the cheapest sequence.
                  */
            if (cheapestCost == -1 || totalCost < cheapestCost) {
                // Set the new cheapest sequence and cheapest cost.
                cheapestSequence = sequence;
                cheapestCost = totalCost;
            }

            // Increment the counter.
            counter = counter.add(TWO);
        }

        return new Result(cheapestSequence, cheapestCost);
    }

    /**
     * An algorithm to find the cheapest sequence of posts to rent and return from using
     * the divide and conquer paradigm.
     *
     * @param costTable the cost chart
     *
     * @return the cheapest sequence of rentals
     */
    private static Result findCheapestRentalSequenceDivideConquer(Integer[][] costTable) {
        return findCheapestRentalSequenceDivideConquer(costTable, 0, 0);
    }

    /**
     * An algorithm to find the cheapest sequence of posts to rent and return from using
     * the divide and conquer paradigm.
     *
     * @param costTable the cost chart
     * @param currentIndex the current post
     * @param currentCost the total cost up to this post
     *
     * @return the cheapest sequence of rentals starting from the current post to the end
     */
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
     * @param costTable the cost chart
     *
     * @return the cheapest sequence of rentals
     */
    private static Result findCheapestRentalSequenceDynamic(Integer[][] costTable) {
        // Initialize minCost and path arrays with length equal to the number of posts (e.g. cost.length).
        int[] minCost = new int[costTable.length];
        int[] path = new int[costTable.length];
        // Fill minCost with max value.
        Arrays.fill(minCost, Integer.MAX_VALUE);
        // Initialize the base case (-1 for the end with a cost of 0).
        path[0] = -1;
        minCost[0] = 0;
        // Iterate over posts 1 to n.
        for (int j = 1; j < costTable.length; j++) {
            // Iterate posts 0 to j - 1.
            for (int i = 0; i < j; i++) {
                /*
                 If the minCost to post i plus the costs from post i to j is less than the
                 minimum cost to point j replace the min cost to j with the new minimum cost
                 and set the path at index j to i.
                  */
                if (minCost[i] + costTable[i][j] < minCost[j]) {
                    minCost[j] = minCost[i] + costTable[i][j];
                    path[j] = i;
                }
            }
        }

        // Initialize our sequence list.
        List<Integer> sequence = new ArrayList<>();
        // Initialize index equal to the number of posts minus 1.
        int i = costTable.length - 1;
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

        return new Result(sequence, minCost[costTable.length - 1]);
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
     * Brute force algorithm test that finds the cheapest sequence
     * of rentals and prints it to the console.
     *
     * @param costTable the cost chart
     */
    public static void testBruteForce(Integer[][] costTable) {
        // Find the cheapest sequence for the provided cost chart
        Result result = findCheapestRentalSequenceBruteForce(costTable);

        // Print the sequence
        StringBuilder builder = new StringBuilder().append("Brute Force Algorithm:\n")
                .append("Total Cost: %s\n")
                .append("Sequence: %s");
        System.out.println(String.format(builder.toString(), result.totalCost, serializeResultSequence(result)));
    }

    /**
     * Divide and conquer algorithm test that finds the cheapest sequence
     * of rentals and prints it to the console.
     *
     * @param costTable the cost chart
     */
    public static void testDivideConquer(Integer[][] costTable) {
        // Find the cheapest sequence for the provided cost chart
        Result result = findCheapestRentalSequenceDivideConquer(costTable);

        // Print the sequence
        StringBuilder builder = new StringBuilder().append("Divide and Conquer Algorithm:\n")
                .append("Total Cost: %s\n")
                .append("Sequence: %s");
        System.out.println(String.format(builder.toString(), result.totalCost, serializeResultSequence(result)));
    }

    /**
     * Dynamic programming algorithm test that finds the cheapest sequence
     * of rentals and prints it to the console.
     *
     * @param costTable the cost chart
     */
    public static void testDynamic(Integer[][] costTable) {
        // Find the cheapest sequence for the provided cost chart
        Result result = findCheapestRentalSequenceDynamic(costTable);

        // Print the sequence
        StringBuilder builder = new StringBuilder().append("Dynamic Programming Algorithm:\n")
                .append("Total Cost: %s\n")
                .append("Sequence: %s");
        System.out.println(String.format(builder.toString(), result.totalCost, serializeResultSequence(result)));
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
        int size = 25;
//        for (int i = 0; i <= 5; i++) {
            Integer[][] randomTable = generateCostTable(size, GenerationMode.RANDOM);
            Integer[][] dependentTable = generateCostTable(size, GenerationMode.DEPENDENT);

            System.out.println("+==========================================+");
            System.out.println(String.format("Table Size: %s", randomTable.length));
            System.out.println("+================= RANDOM =================+");

            testBruteForce(randomTable);
            testDivideConquer(randomTable);
            testDynamic(randomTable);

            System.out.println("+================ DEPENDENT ===============+");

            testBruteForce(dependentTable);
            testDivideConquer(dependentTable);
            testDynamic(dependentTable);

            System.out.println("+==========================================+");

//            size *= 2;
//        }
    }

    /**
     * Enum that allows for custom random int generation logic for each value.
     */
    public enum GenerationMode {
        /**
         * Generates a random number.
         */
        RANDOM {
            public int nextInt(Integer[][] costTable, int row, int column) {
                return generateInteger();
            }
        },
        /**
         * Generates a random number and adds the previous value
         * of the corresponding row to that number.
         */
        DEPENDENT {
            public int nextInt(Integer[][] costTable, int row, int column) {
                int val = generateInteger();
                if (costTable[row][column - 1] != null) {
                    val += costTable[row][column - 1];
                }
                return val;
            }
        };

        /**
         * Generates an integer from the given table, row, and column.
         *
         * @param costTable  the table
         * @param row    the row index
         * @param column the column index
         */
        public abstract int nextInt(Integer[][] costTable, int row, int column);

        /**
         * Generates a number and populates the table at the corresponding
         * indices with the generated number.
         *
         * @param costTable  the table
         * @param row    the row index
         * @param column the column index
         */
        public void fill(Integer[][] costTable, int row, int column) {
            if (row == column) {
                costTable[row][column] = 0;
            } else {
                while (costTable[row][column] == null || costTable[row][column] <= 0) {
                    costTable[row][column] = nextInt(costTable, row, column);
                }
            }
        }
    }

    /**
     * Wrapper class to store a sequence and its total cost.
     */
    public static class Result {

        private List<Integer> sequence;
        private int totalCost;

        /**
         * Constructor that takes a sequence list and total cost.
         *
         * @param sequence the sequence
         * @param totalCost the cost
         */
        public Result(List<Integer> sequence, int totalCost) {
            this.sequence = sequence;
            this.totalCost = totalCost;
        }

    }
}
