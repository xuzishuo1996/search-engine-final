# Search Engine

### 1 Introduction

This project crafts a simple search engine for a static text collection in compressed XML format. It consists of text processing, indexing, ranking, evaluation, snippet generation sub-engines and the interactive user-interface.

When the user types in a query, the search engine will return the top-10 results using BM25 ranking method and generate corresponding snippets. Then the user could choose to see the complete content of a returned result or type in new query. 

The evaluation engine evaluates the effectiveness measure of the search engines, including precision, mean average precision (mAP) and normalized discounted cumulative gain (NDCG).

Refer to the demos:

https://drive.google.com/file/d/1IuyeDczkxBPLYwH2sYq5AhCTjDhDqbiO/view?usp=sharing

https://drive.google.com/file/d/1SwgmvVxF6rRsrWbVE8dFs-7GiHh8TG5J/view?usp=sharing 

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

2) **InteractiveEngine**: the interactive interface. Users could type in queries. 

Under the root of the project folder, execute:

```
$ ./InteractiveEngine {path that stores metadata and raw docs}
```

For example:

```
$ ./InteractiveEngine /home/smucker/latimes.gz /home/smucker/latimes-index
```

Or you can import the project into IDEA and run the code. You can set the command line arguments in the Run Configuration.

### 4 Test Environment

OS name: Ubuntu 20.04.2 LTS

Linux kernel version: 5.8.0-43-generic

make version: GNU Make 4.2.1

java version: openjdk version "1.8.0_292"

