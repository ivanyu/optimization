#!/usr/bin/python
# -*- coding: utf-8 -*-
import sys
import numpy as np
import time
from dynprog import dynamic_programming


def main(argv):
    if len(argv) != 2:
        print("Usage: knapsack filename")
        return

    filename = argv[1]
    with open(filename) as f:
        line = f.readline()
        n, K = line.split(' ')
        n = int(n)
        K = int(K)

        values = np.zeros((n,), dtype=np.int)
        weights = np.zeros((n,), dtype=np.int)

        for i in range(n):
            line = f.readline()
            v, w = line.split(' ')
            values[i] = int(v)
            weights[i] = int(w)

    start = time.time()
    result = dynamic_programming(n, K, values, weights)
    end = time.time()
    print("Total items to take:{}\nItems:", len(result))
    for i in result:
        print(i)
    print("Completed in {} seconds.".format(end - start))

if __name__ == "__main__":
    main(sys.argv)
