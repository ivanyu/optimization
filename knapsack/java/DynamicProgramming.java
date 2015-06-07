import java.util.Arrays;

public class DynamicProgramming {
  public static int[] optimize(final int n, final int K,
                               final int[] values, final int[] weights) {
    final int[][] table = new int[K + 1][n + 1];

    for (int i = 0; i <= n; i++) {
      for (int curr_K = 0; curr_K <= K; curr_K++) {
        if (i == 0 || curr_K == 0) {
          table[curr_K][i] = 0;
          continue;
        }

        final int notTaking = table[curr_K][i - 1];
        if (weights[i - 1] > curr_K) {
          table[curr_K][i] = notTaking;
        } else {
          final int takingCapacity = curr_K - weights[i - 1];
          final int taking = values[i - 1] + table[takingCapacity][i - 1];
          table[curr_K][i] = Math.max(notTaking, taking);
        }
      }
    }

    final int[] result = new int[n];
    int result_curr = 0;
    int i = n;
    int curr_K = K;

    while (i > 0 && curr_K > 0) {
      if (table[curr_K][i] != table[curr_K][i - 1]) {
        curr_K -= weights[i - 1];
        result[result_curr] = i - 1;
        result_curr += 1;
      }
      i -= 1;
    }

    return Arrays.copyOf(result, result_curr);
  }
}
