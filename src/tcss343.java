import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class tcss343 {

    private static final Random RANDOM_GENERATOR = new Random(System.currentTimeMillis());

    private static final int MIN_BOUND = 1;
    private static final int MAX_BOUND = 1000;

    private static int generateInteger() {
        return RANDOM_GENERATOR.nextInt((MAX_BOUND - MIN_BOUND) + 1) + MIN_BOUND;
    }

    /*
    An algorithm to find the cheapest sequence of posts to rent and return from using
    the dynamic programming paradigm.
     */
    private static List<Integer> findCheapestRentalSequenceDynamic(Integer[][] cost) {
        // Initialize minCost and path arrays with length equal to the number of posts (e.g. cost.length).
        int[] minCost = new int[cost.length];
        int[] path = new int[cost.length];
        // Fill minCost with max value.
        Arrays.fill(minCost, Integer.MAX_VALUE);
        // Initialize the base case (-1 for the end with a cost of 0).
        path[0] = -1;
        minCost[0] = 0;
        // Iterate over posts 1 to n.
        for (int j = 1; j < cost.length; j++) {
            // Iterate posts 0 to j - 1.
            for (int i = 0; i < j; i++) {
                /*
                 If the minCost to post i plus the costs from post i to j is less than the
                 minimum cost to point j replace the min cost to j with the new minimum cost
                 and set the path at index j to i.
                  */
                if (minCost[i] + cost[i][j] < minCost[j]) {
                    minCost[j] = minCost[i] + cost[i][j];
                    path[j] = i;
                }
            }
        }

        // Initialize our sequence list.
        List<Integer> sequence = new ArrayList<>();
        // Initialize index equal to the number of posts minus 1.
        int i = cost.length - 1;
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

        return sequence;
    }

    public static Integer[][] generateCostTable(int n, GenerationMode mode) {
        Random random = new Random(System.currentTimeMillis());
        Integer[][] table = new Integer[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                mode.generateNumber(table, i, j);
            }
        }

        return table;
    }

    public static void testDynamic(Integer[][] costChart) {
        List<Integer> sequence = findCheapestRentalSequenceDynamic(costChart);

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < sequence.size(); i++) {
            if (builder.length() > 0) {
                builder.append("->");
            }

            builder.append(sequence.get(i));
        }

        System.out.println(String.format("Cheapest sequence: [%s]", builder.toString()));
    }

    public static void main(String... args) {
        testDynamic(generateCostTable(800, GenerationMode.RANDOM));
        testDynamic(generateCostTable(800, GenerationMode.DEPENDENT));
    }

    public enum GenerationMode {
        RANDOM {
            public void fill(Integer[][] table, int i, int j) {
                table[i][j] = generateInteger();
            }
        },
        DEPENDENT {
            public void fill(Integer[][] table, int i, int j) {
                table[i][j] = generateInteger();
                if (table[i][j - 1] != null) {
                    table[i][j] += table[i][j - 1];
                }
            }
        };

        public abstract void fill(Integer[][] table, int i, int j);

        public void generateNumber(Integer[][] table, int i, int j) {
            if (i == j) {
                table[i][j] = 0;
            } else {
                while (table[i][j] == null || table[i][j] <= 0) {
                    fill(table, i, j);
                }
            }
        }
    }

}
