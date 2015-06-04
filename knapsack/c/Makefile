CC=gcc
CCFLAGS=-O2 -Wall -std=c99

ifeq ($(OS),Windows_NT)
    OUTPUT_FILE=knapsack.exe
    CCFLAGS += -D WIN32
    ifeq ($(PROCESSOR_ARCHITECTURE),AMD64)
        CCFLAGS += -D AMD64
    endif
    ifeq ($(PROCESSOR_ARCHITECTURE),x86)
        CCFLAGS += -D IA32
    endif
else
    OUTPUT_FILE=knapsack
    UNAME_S := $(shell uname -s)
    ifeq ($(UNAME_S),Linux)
        CCFLAGS += -D LINUX
    endif
    ifeq ($(UNAME_S),Darwin)
        CCFLAGS += -D OSX
    endif
    UNAME_P := $(shell uname -p)
    ifeq ($(UNAME_P),x86_64)
        CCFLAGS += -D AMD64
    endif
    ifneq ($(filter %86,$(UNAME_P)),)
        CCFLAGS += -D IA32
    endif
    ifneq ($(filter arm%,$(UNAME_P)),)
        CCFLAGS += -D ARM
    endif
endif

SRCS = knapsack.c dynprog.c
OBJS = $(SRCS:.c=.o)

all: $(OUTPUT_FILE)

$(OUTPUT_FILE): knapsack.o dynprog.o
	$(CC) $(CCFLAGS) knapsack.o dynprog.o -o $(OUTPUT_FILE)

.c.o: $(SRCS)
	$(CC) $(CCFLAGS) -c $<

.PHONY: clean
clean:
	rm -rf *.o $(OUTPUT_FILE)
