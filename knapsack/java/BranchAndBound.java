import java.util.Arrays;

class Item implements Comparable<Item> {
  public final double specificValue;
  public final int originalIndex;

  public Item(final double specificValue, final int originalIndex) {
    this.specificValue = specificValue;
    this.originalIndex = originalIndex;
  }

  @Override
  public int compareTo(final Item o) {
    // Reverse order sort.
    if (this.specificValue > o.specificValue) {
      return -1;
    } else if (this.specificValue < o.specificValue) {
      return 1;
    } else {
      return 0;
    }
  }
}

public class BranchAndBound {
  public static int[] optimize(final int n, final int K,
                               final int[] values, final int[] weights) {
    // Linear relaxation.
    final Item[] specificValues = new Item[n];
    for (int i = 0; i < n; i++) {
      specificValues[i] = new Item((double)values[i] / weights[i], i);
    }
    Arrays.sort(specificValues);

    int estimate = 0;
    int capacityReminder = K;
    for (int i = 0; i < n && capacityReminder > 0; i++) {
      final Item item = specificValues[i];
      final int weight = weights[item.originalIndex];
      final int value = values[item.originalIndex];
      if (capacityReminder >= weight) {
        capacityReminder -= weight;
        estimate += value;
      } else {
        final double fraction = (double)capacityReminder / weight;
        final int valueFraction = (int)Math.ceil(fraction * value);
        capacityReminder = 0;
        estimate += valueFraction;
      }
    }

    estimate = 0;
    for (int i = 0; i < n; i++) {
      estimate += values[i];
    }

//    estimate = 128;

    return bnb(n, K, values, weights, estimate);
  }

  private static int[] bnb(final int n, final int K,
                           final int[] values, final int[] weights,
                           final int estimate) {
    // Depth-first search.
    int maxValue = -1;
    byte[] maxValueMask = new byte[n];

    final byte[] mask = new byte[n];
    Arrays.fill(mask, (byte)2);

    int i = 0;
    int currentEstimate = estimate;
    int currentValue = 0;
    int currentRoom = K;

    while (i > - 1) {
      if (i == n) {
        if (currentValue > maxValue) {
          // Reached a leaf - consider the solution.
          maxValue = currentValue;
          maxValueMask = Arrays.copyOf(mask, n);
        }
        i -= 1;
      }

      if (mask[i] == 2) {  // Not explored at all
        currentRoom -= weights[i];
        currentValue += values[i];

        mask[i] = 1;

        // Only consider feasible solutions and worth exploring
        if (currentRoom >= 0 && currentEstimate > maxValue) {
          i += 1;
        }
      } else if (mask[i] == 1) { // Taken
        currentRoom += weights[i];
        currentValue -= values[i];

        currentEstimate -= values[i];

        mask[i] = 0;

        // Try to not take and go deeper if it worth doing so
        if (currentEstimate > maxValue) {
          i += 1;
        }
      } else { // Not taken
        // Go up
        currentEstimate += values[i];
        mask[i] = 2;
        i -= 1;
      }

    }

    int[] result = new int[n];
    int r = 0;
    for (int j = n - 1; j >= 0; j--) {
      if (maxValueMask[j] == 1) {
        result[r] = j;
        r++;
      }
    }
    result = Arrays.copyOf(result, r);
    return result;
  }
}
