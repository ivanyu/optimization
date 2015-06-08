import java.util.Arrays;

public class BranchAndBound {
  private class Item implements Comparable<Item> {
    public final int originalIndex;
    public final int value;
    public final int weight;
    public final double specificValue;

    public Item(final int value, final int weight, final int originalIndex) {
      this.originalIndex = originalIndex;
      this.value = value;
      this.weight = weight;
      this.specificValue = (double)value / weight;
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

  private class IterationResult {
    public final int maxValue;
    public final byte[] mask;

    public IterationResult(final int maxValue, final byte[] mask) {
      this.maxValue = maxValue;
      this.mask = mask;
    }
  }


  private final int n;
  private final int K;

  private final Item[] items;

  public BranchAndBound(int n, int K, int[] values, int[] weights) {
    this.n = n;
    this.K = K;

    items = new Item[n];
    for (int i = 0; i < n; i++) {
      items[i] = new Item(values[i], weights[i], i);
    }
    Arrays.sort(items);
  }

  public int[] optimize() {
    // Linear relaxation.

    final byte[] solutionMask = bnb();
//    final byte[] solutionMask = bnbRec();
    final int[] result = new int[n];
    int resultCurr = 0;

    for (int i = 0; i < n; i++) {
      if (solutionMask[i] == 1) {
        result[resultCurr] = items[i].originalIndex;
        resultCurr += 1;
      }
    }

    int[] result1 = Arrays.copyOf(result, resultCurr);
    Arrays.sort(result1);
    return result1;
  }

  public static int[] optimize(final int n, final int K,
                               final int[] values, final int[] weights) {
    final BranchAndBound optimizer = new BranchAndBound(n, K, values, weights);
    return optimizer.optimize();
  }

  /**
   * Iterative solution.
   */
  private byte[] bnb() {
    // Depth-first search.
    int maxValue = -1;
    byte[] maxValueMask = new byte[n];

    final byte[] mask = new byte[n];
    Arrays.fill(mask, (byte)2);

    int i = 0;
    int currentValue = 0;
    int currentRoom = K;

    while (i > - 1) {
      if (i == n) {
        // Reached a leaf - examine the solution.
        if (currentValue > maxValue) {
          maxValue = currentValue;
          maxValueMask = Arrays.copyOf(mask, n);
        }
        i -= 1;
      }

      if (mask[i] == 2) {  // Node is not explored
        // Try to move to "taken" state.
        mask[i] = 1;
        currentRoom -= items[i].weight;
        currentValue += items[i].value;

        // Consider solutions that are feasible and worth exploring.
        final double currentEstimate = estimate(items, mask, currentRoom);
        final double fullEstimate = currentValue + currentEstimate;
        if (currentRoom >= 0 && fullEstimate > maxValue) {
          i += 1;
        }
      } else if (mask[i] == 1) { // Node is in "taken" state
        // Try to move to "not taken" state.
        mask[i] = 0;
        currentRoom += items[i].weight;
        currentValue -= items[i].value;

        double currentEstimate = estimate(items, mask, currentRoom);
        // Try to not take and go deeper if it worth doing so
        if (currentValue + currentEstimate > maxValue) {
          i += 1;
        }
      } else { // Node is in "not taken" state
        // Go up
        mask[i] = 2;
        i -= 1;
      }
    }

    return maxValueMask;
  }

  /**
   * Recursive solution.
   * Works fine but got StackOverflowException on huge item counts.
   */
  private byte[] bnbRec() {
    final byte[] initialMask = new byte[n];
    Arrays.fill(initialMask, (byte)2);
    final byte[] solutionMask = bnbRec0(0, initialMask, 0, K, -1, null).mask;
    return solutionMask;
  }

  private IterationResult bnbRec0(final int i, final byte[] mask,
                                  final int currentValue, final int currentRoom,
                                  final int maxValue, final byte[] maxValueMask) {
    if (i == n) {
      if (currentValue > maxValue)
        return new IterationResult(currentValue, mask);
      else
        return new IterationResult(maxValue, maxValueMask);
    }


    // Trying to take current item and go deeper.
    int maxValueIfTake = maxValue;
    byte[] maxValueMaskIfTake = maxValueMask;

    {
      final byte[] maskIfTake = Arrays.copyOf(mask, n);
      maskIfTake[i] = 1;

      final double estimateIfTake = estimate(items, maskIfTake, currentRoom);
      final double fullEstimate = estimateIfTake + currentValue + items[i].value;
      if (currentRoom >= items[i].weight && fullEstimate > maxValue) {
        final IterationResult r = bnbRec0(i + 1, maskIfTake,
            currentValue + items[i].value, currentRoom - items[i].weight, maxValue, maxValueMask);
        maxValueIfTake = r.maxValue;
        maxValueMaskIfTake = r.mask;
      }
    }

    // Trying not to take current item and go deeper.
    int maxValueIfNotTake = maxValueIfTake;
    byte[] maxValueMaskIfNotTake = maxValueMaskIfTake;

    {
      final byte[] maskIfNotTake = Arrays.copyOf(mask, n);
      maskIfNotTake[i] = 0;

      final double estimateIfNotTake = estimate(items, maskIfNotTake, currentRoom);
      final double fullEstimate = estimateIfNotTake + currentValue;
      if (fullEstimate > maxValueIfTake) {
        final IterationResult r = bnbRec0(i + 1, maskIfNotTake,
            currentValue, currentRoom, maxValueIfTake, maxValueMaskIfTake);
        maxValueIfNotTake = r.maxValue;
        maxValueMaskIfNotTake = r.mask;
      }
    }

    return new IterationResult(maxValueIfNotTake, maxValueMaskIfNotTake);
  }

  private static double estimate(final Item[] items, final byte[] mask, final int room) {
    int room0 = room;
    double estimateValue = 0;

    for (int i = 0; i < items.length && room0 > 0; i++) {
      if (mask[i] != 2)
        continue;

      final int weight = items[i].weight;
      final int value = items[i].value;
      if (room0 >= weight) {
        room0 -= weight;
        estimateValue += value;
      } else {
        final double fraction = (double)room0 / weight;
        final double valueFraction = fraction * value;
        room0 = 0;
        estimateValue += valueFraction;
      }
    }

    return estimateValue;
  }
}
