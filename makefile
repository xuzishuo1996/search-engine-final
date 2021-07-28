_dummy := $(shell mkdir -p target)

# define a variable for java compiler
JC = javac

SRC_DIR = src/main/java
OUT_DIR = target

# define a variable for java compiler flags. -g:generating all debugging info.
JFLAGS = -d $(OUT_DIR) -sourcepath $(SRC_DIR) -classpath $(OUT_DIR)

index_src = $(SRC_DIR)/index/IndexEngine.java
query_src = $(SRC_DIR)/query/BooleanAND.java
retriever_src = $(SRC_DIR)/retriever/GetDoc.java
evaluation_src = $(SRC_DIR)/evaluation/EvaluationEngine.java
ranking_src = $(SRC_DIR)/ranking/RankingEngine.java

all: index_engine boolean_and get_doc evaluation_engine ranking_engine scripts

index_engine: $(index_src)
	$(JC) $(JFLAGS) $(index_src)

boolean_and: $(query_src)
	$(JC) $(JFLAGS) $(query_src)

get_doc: $(retriever_src)
	$(JC) $(JFLAGS) $(retriever_src)

evaluation_engine: $(evaluation_src)
	$(JC) $(JFLAGS) $(evaluation_src)

ranking_engine: $(ranking_src)
	$(JC) $(JFLAGS) $(ranking_src)

scripts:
	chmod +x IndexEngine
	chmod +x BooleanAND
	chmod +x GetDoc
	chmod +x EvaluationEngine
	chmod +x RankingEngine

# .PHONY tells makefile to treat clean as a command
.PHONY: clean
clean:
	rm -rf $(OUT_DIR)
