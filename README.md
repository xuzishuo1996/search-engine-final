# MSCI 720 Search Engines - HW5

Zishuo Xu

20900288

### 1 Introduction

This is part 5 of the series of homework to craft a simple search engine. We do not have a crawler. The compressed XML format data collection is provided.

HW4 implements the interactive interface of the search engine. When the user types in a query, the search engine will return the top-10 results using BM25 ranking method and generate corresponding snippets. Then the user could choose to see the complete content of a returned result or type in new query.

### 2 Build

Under the root of the project folder, execute:

```
$ make clean
$ make
```

### 3 Run

1) **IndexEngine**: seperate docs from the gz file and store both raw docs and metadata.

Under the root of the project folder, execute:

```
$ ./IndexEngine {input gzip file path} {path to store metadata and raw docs}
```

For example:

```
$ ./IndexEngine /home/smucker/latimes.gz /home/smucker/latimes-index
```

2) **RankingEngine**: rank the docs by BM25 algorithm and return 1000 (or less) related docs for each query.

Under the root of the project folder, execute:

```
$ ./IndexEngine {base dir of index} {query file path} {file path to output ranking result}
```

For example:

```
$ ./RankingEngine /home/smucker/latimes-index /home/smucker/queries.txt /home/smucker/hw4-bm25-stem-smucker.txt
```

3) **EvaluationEngine**: process the docs, calculate and output the evaluation metrics to the console. 

**Note**: Put the ranking results into {results dir path}, rename the result file as student1.results.

Under the root of the project folder, execute:

```
$ ./EvaluationEngine {qrels file path} {results dir path} {index dir path}
```

For example:

```
$ ./EvaluationEngine /home/smucker/Desktop/msci720-hw/hw3-files/qrels/LA-only.trec8-401.450.minus416-423-437-444-447.txt /home/smucker/Desktop/msci720-hw/hw3-files/results-files/ /home/smucker/Desktop/msci720-hw/latime_index/
```

Or you can import the project into IDEA and run the code. You can set the command line arguments in the Run Configuration.

### 4 Test Environment

OS name: Ubuntu 20.04.2 LTS

Linux kernel version: 5.8.0-43-generic

make version: GNU Make 4.2.1

java version: openjdk version "1.8.0_292"

