# -*- coding: utf-8 -*-
import numpy as np


def dynamic_programming(n, K, values, weights):
    table = np.zeros((K + 1, n + 1), dtype=np.int)

    for i in range(0, n + 1):
        for curr_K in range(0, K + 1):
            if i == 0 or curr_K == 0:
                table[curr_K, i] = 0
                continue

            not_taking = table[curr_K, i - 1]
            if weights[i - 1] > curr_K:
                table[curr_K, i] = not_taking
            else:
                taking_capacity = curr_K - weights[i - 1]
                taking = values[i - 1] + table[taking_capacity, i - 1]
                table[curr_K, i] = max(not_taking, taking)

    result = np.zeros((n,), dtype=np.int)
    result_curr = 0
    i = n
    curr_K = K
    while i > 0 and curr_K > 0:
        if table[curr_K, i] != table[curr_K, i - 1]:
            curr_K -= weights[i - 1]
            result[result_curr] = i - 1
            result_curr += 1
        i -= 1

    result.resize((result_curr, ), refcheck=False)

    # for curr_K in range(0, K + 1):
    #     print("{}".format(curr_K), end='\t')
    #     for i in range(0, n + 1):
    #         print('{}'.format(table[curr_K, i]), end=' ')
    #     print()

    return result
