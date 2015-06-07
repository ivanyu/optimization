import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class App {
  public static void main(final String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: knapsack filename");
      return;
    }

    int n;
    int K;

    int[] values;
    int[] weights;

    final String filename = args[0];
    try (final FileReader fileReader = new FileReader(filename);
         final BufferedReader reader = new BufferedReader(fileReader)) {
      String[] splited = reader.readLine().split("\\s");
      n = Integer.parseInt(splited[0]);
      K = Integer.parseInt(splited[1]);

      values = new int[n];
      weights = new int[n];

      for (int i = 0; i < n; i++) {
        splited = reader.readLine().split("\\s");
        values[i] = Integer.parseInt(splited[0]);
        weights[i] = Integer.parseInt(splited[1]);
      }
    }

    final long start = System.currentTimeMillis();
//    final int[] result = DynamicProgramming.optimize(n, K, values, weights);
    final int[] result = BranchAndBound.optimize(n, K, values, weights);
    final long end = System.currentTimeMillis();

    System.out.printf("Total items to take: %d\nItems:\n", result.length);

    int solutionValue = 0;
    int solutionWeight = 0;
    for (int x : result) {
      System.out.println(x);
      solutionValue += values[x];
      solutionWeight += weights[x];
    }

    System.out.printf("Capacity: %d, weight of the solution: %d\nSolution value: %d\n",
        K, solutionWeight, solutionValue);

    System.out.printf("Completed in %f seconds.", (end - start) / 1000.0);
  }
}
