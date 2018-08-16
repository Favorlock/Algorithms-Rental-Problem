import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * TCSS 343 Assignment 4.
 *
 * Suggested program arguments: -g:25,50,100,200,400,800 -tg -bfl:25 -dcl:25
 * Will generate tables of sizes 25, 50, 100, 200, 400, and 800 and save them to file. It will then test
 * each generated table. The brute force and divide and conquer algorithms will only test the first table
 * to avoid extremely long test periods.
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
     * Separator used in print messages.
     */
    private static final String SEPARATOR = "============================================================";

    /**
     * Max dimension the brute force algorithm should test.
     */
    private static int bruteForceLimit = Integer.MAX_VALUE;

    /**
     * Max dimension the divide and conquer algorithm should test.
     */
    private static int divideConquerLimit = Integer.MAX_VALUE;

    /**
     * Max dimension the dynamic programming algorithm should test.
     */
    private static int dynamicLimit = Integer.MAX_VALUE;

    /**
     * Whether or not to perform test on after generating a table.
     */
    private static boolean testGenerated = false;

    /**
     * Generate a random integer from MIN_BOUND to MAX_BOUND (inclusive).
     *
     * @return a random integer
     */
    private static int generateInteger() {
        return RANDOM_GENERATOR.nextInt((MAX_BOUND - MIN_BOUND) + 1) + MIN_BOUND;
    }

    /**
     * Reads a cost table from file.
     *
     * @param target the target file to read from
     *
     * @return a cost table
     *
     * @throws IOException
     */
    private static Integer[][] readCostTableFromFile(String target) throws IOException {
        File file = new File(target);
        Integer[][] costTable = null;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int row = 0;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (costTable == null) {
                    costTable = new Integer[parts.length][parts.length];
                }

                for (int column = row; column < parts.length; column++) {
                    costTable[row][column] = Integer.parseInt(parts[column]);
                }

                row += 1;
            }
        }

        return costTable;
    }

    private static Integer[][] generateAndSaveCostTable(int size, GenerationMode mode) {
        Integer[][] costTable = generateCostTable(size, mode);
        File file = new File(String.format("./%sCostTable%s.txt", mode.name(), size));

        try {
            if (file.exists()) {
                file.delete();
            }

            file.createNewFile();

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                for (int i = 0; i < costTable.length; i++) {
                    for (int j = 0; j < costTable.length; j++) {
                        if (costTable[i][j] == null) {
                            bw.append("NA");
                        } else {
                            bw.append(String.valueOf(costTable[i][j]));
                        }

                        if (j < costTable.length - 1) {
                            bw.append('\t');
                        }
                    }

                    if (i < costTable.length - 1) {
                        bw.append("\n");
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("An error occurred while writing a table to file: " + file.toString());
        }

        return costTable;
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

        // Check if counter is even, if so increment by one.
        if (!counter.testBit(0)) {
            counter = counter.add(BigInteger.ONE);
        }

        // Iterate over all odd numbers from 2^(costTable.length - 1) to 2^costTable.length
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
     * @param costTable    the cost chart
     * @param currentIndex the current post
     * @param currentCost  the total cost up to this post
     *
     * @return the cheapest sequence of rentals starting from the current post to the end
     */
    private static Result findCheapestRentalSequenceDivideConquer(Integer[][] costTable, int currentIndex, int currentCost) {
        if (costTable.length - 1 == currentIndex) {
            // The end has been reached.
            List list = new LinkedList();
            list.add(currentIndex);
            return new Result(list, currentCost);
        } else {
            Result cheapestResult = null;

            // Check all possible destinations from the current index.
            for (int i = currentIndex + 1; i < costTable.length; i++) {
                Result result = findCheapestRentalSequenceDivideConquer(costTable, i, currentCost + costTable[currentIndex][i]);
                // Check if the result cost less than the current cheapest result.
                if (cheapestResult == null || result.totalCost < cheapestResult.totalCost) {
                    cheapestResult = result;
                }
            }

            // Add the current index to the beginning of cheapest result's sequence.
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
        List<Integer> sequence = new LinkedList<>();
        // Initialize index equal to the number of posts minus 1.
        int i = costTable.length - 1;
        /*
        Check if the value at index i of the path array is our sentinel value. If not add the index to
        our sequence list and update our i to the value.
         */
        while (path[i] != -1) {
            sequence.add(0, i);
            i = path[i];
        }
        // Add the starting post to our sequence list.
        sequence.add(0, i);

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
        if (costTable.length > bruteForceLimit) {
            return;
        }

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
        if (costTable.length > divideConquerLimit) {
            return;
        }

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
        if (costTable.length > dynamicLimit) {
            return;
        }

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
     * Runs tests for a provided cost table.
     *
     * @param costTable the table to test
     * @param testBruteForce whether or not to test the brute force algorithm
     * @param testDivideConquer whether or not to test the divide and conquer algorithm
     * @param testDynamic whether or not to test the dynamic programming algorithm
     */
    private static void testCostTable(Integer[][] costTable, boolean testBruteForce, boolean testDivideConquer, boolean testDynamic) {
        if (testBruteForce) {
            testBruteForce(costTable);
        }

        if (testDivideConquer) {
            testDivideConquer(costTable);
        }

        if (testDynamic) {
            testDynamic(costTable);
        }
    }

    /**
     * Runs tests for a table loaded from a file.
     *
     * @param target
     */
    private static void testFromFile(String target) {
        try {
            Integer[][] costTable = readCostTableFromFile(target);

            System.out.println(SEPARATOR);
            System.out.println(String.format("Testing File: %s", target));
            System.out.println(String.format("Table Dimension: %s", costTable.length));
            System.out.println(SEPARATOR);

            testCostTable(costTable, true, true, true);
        } catch (Exception ex) {
            System.err.println("An error occurred while testing from a file: " + ex.getMessage());
        }
    }

    /**
     * Entry point for this program.
     *
     * @param args program arguments
     */
    public static void main(String... args) {
        String[] files = null;
        Integer[] sizesToGenerate = null;
        for (String arg : args) {
            arg = arg.toLowerCase();

            if (arg.startsWith("-f:")) {
                files = arg.substring(3).split(",");
            }

            if (arg.startsWith("-g:")) {
                String[] parts = arg.substring(3).split(",");
                sizesToGenerate = new Integer[parts.length];
                for (int i = 0; i < sizesToGenerate.length; i++) {
                    try {
                        sizesToGenerate[i] = Integer.parseInt(parts[i]);
                    } catch (NumberFormatException ex) {
                        System.err.println("Invalid generations size specified: " + parts[i]);
                    }
                }
            }

            if (arg.startsWith("-bfl:")) {
                bruteForceLimit = Integer.parseInt(arg.substring(5));
            }

            if (arg.startsWith("-dcl:")) {
                divideConquerLimit = Integer.parseInt(arg.substring(5));
            }

            if (arg.startsWith("-dpl:")) {
                dynamicLimit = Integer.parseInt(arg.substring(5));
            }

            if (arg.equals("-tg")) {
                testGenerated = true;
            }
        }

        if (files != null) {
            for (String file : files) {
                testFromFile(file);
            }
        }

        if (sizesToGenerate != null) {
            List<Integer[][]> costTables = new ArrayList<>();

            for (Integer size : sizesToGenerate) {
                if (size == null || size < 2) {
                    continue;
                }

                costTables.add(generateAndSaveCostTable(size, GenerationMode.RANDOM));
                costTables.add(generateAndSaveCostTable(size, GenerationMode.DEPENDENT));
            }

            if (testGenerated) {
                costTables.forEach(table -> {
                    System.out.println(SEPARATOR);
                    System.out.println(String.format("Table Dimension: %s", table.length));
                    System.out.println(SEPARATOR);
                    testCostTable(table, true, true, true);
                });
            }
        }

        if (args.length == 0) {
            StringBuilder builder = new StringBuilder();
            builder.append("optional arguments:\n")
                    .append("  -f:./file.txt,./file2.txt\t\tRuns test for specified files\n")
                    .append("  -g:5,10,20,40\t\tGenerates and saves tables of specified sizes to files\n")
                    .append("  -tg\t\tEnables tests on generated tables\n")
                    .append("  -bfl:50\t\tSets the max dimension for brute force testing\n")
                    .append("  -dcl:50\t\tSets the max dimension for divide and conquer testing\n")
                    .append("  -dpl:50\t\tSets the max dimension for dynamic programming testing");
            System.out.println(builder.toString());
        }
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
         * @param costTable the table
         * @param row       the row index
         * @param column    the column index
         */
        public abstract int nextInt(Integer[][] costTable, int row, int column);

        /**
         * Generates a number and populates the table at the corresponding
         * indices with the generated number.
         *
         * @param costTable the table
         * @param row       the row index
         * @param column    the column index
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
         * @param sequence  the sequence
         * @param totalCost the cost
         */
        public Result(List<Integer> sequence, int totalCost) {
            this.sequence = sequence;
            this.totalCost = totalCost;
        }

    }
}
