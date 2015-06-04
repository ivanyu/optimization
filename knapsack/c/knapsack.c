#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#include "dynprog.h"

int main(const int argc, char const * const * const argv) {
    if (argc != 2) {
        printf("Usage: knapsack filename\n");
        return 0;
    }
    
    const char* const filename = argv[1];
    FILE* pFile = fopen(filename, "r");
    if (!pFile) {
        fprintf(stderr, "Can't open input file\n");
        exit(1);
    }

    int n;
    int K;
    fscanf(pFile, "%d %d", &n, &K);

    int* values = (int*)malloc(sizeof(int) * n); 
    if (!values) {
        fclose(pFile);

        fprintf(stderr, "Error allocating memory\n");
        exit(1);
    }

    int* weights = (int*)malloc(sizeof(int) * n); 
    if (!weights) {
        fclose(pFile);

        fprintf(stderr, "Error allocating memory\n");
        exit(1);
    }

    for (int i = 0; i < n; i++) {
        fscanf(pFile, "%d %d", &values[i], &weights[i]);
    }

    fclose(pFile);

    int* result = NULL;

    clock_t start = clock();

    int result_size = dynamic_programming(n, K, values, weights, &result);

    clock_t end = clock();
    float seconds = (float)(end - start) / CLOCKS_PER_SEC;

    printf("Total items to take:%d\nItems:\n", result_size);
    for (int i = result_size - 1; i >= 0; --i)
        printf("%d\n", result[i]);
    free(result);

    printf("Completed in %f seconds.\n", seconds);

    free(values);
    free(weights);

    return 0;
}
