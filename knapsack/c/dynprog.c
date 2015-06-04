#include <stdlib.h>
#include "dynprog.h"

#ifndef max
#define max( a, b ) ( ((a) > (b)) ? (a) : (b) )
#endif

size_t dynamic_programming(const size_t n, const size_t K,
                           const int* const values,
                           const int* const weights,
                           int** result) {

    int** table = (int**)malloc(sizeof(int *) * (K + 1));
    for (size_t curr_K = 0; curr_K <= K; ++curr_K)
        table[curr_K] = (int*)malloc(sizeof(int) * n);

    for (size_t i = 0; i <= n; ++i) {
        for (size_t curr_K = 0; curr_K <= K; ++curr_K) {
            if (i == 0 || curr_K == 0) {
                table[curr_K][i] = 0;
                continue;
            }

            int not_taking = table[curr_K][i - 1];
            if (weights[i - 1] > curr_K) {
                table[curr_K][i] = not_taking;
            } else {
                int taking = values[i - 1]
                    + table[curr_K - weights[i - 1]][i - 1];
                table[curr_K][i] = max(not_taking, taking);
            }
        }
    }

    *result = (int*)malloc(sizeof(int) * n);
    size_t result_curr = 0;

    size_t i = n;
    size_t curr_K = K;
    while (i > 0 && curr_K > 0) {
        if (table[curr_K][i] != table[curr_K][i - 1]) {
            curr_K -= weights[i - 1];
            (*result)[result_curr] = i - 1;
            result_curr += 1;
        }
        i -= 1;
    }

    // for (size_t curr_K = 0; curr_K <= K; ++curr_K) {
    //     printf("%zd\t", curr_K);
    //     for (size_t i = 0; i <= n; ++i) {
    //         printf("%d ", table[curr_K][i]);
    //     }
    //     printf("\n");
    // }

    for (size_t curr_K = 0; curr_K <= K; ++curr_K)
        free(table[curr_K]);
    free(table);

    return result_curr;
}
