_dummy := $(shell mkdir -p target)

# define a variable for java compiler
JC = javac

SRC_DIR = src/main/java
OUT_DIR = target

# define a variable for java compiler flags. -g:generating all debugging info.
JFLAGS = -d $(OUT_DIR) -sourcepath $(SRC_DIR) -classpath $(OUT_DIR)

index_src = $(SRC_DIR)/index/IndexEngine.java
interactive_src = $(SRC_DIR)/interactive/InteractiveEngine.java

all: index_engine interactive_engine scripts

index_engine: $(index_src)
	$(JC) $(JFLAGS) $(index_src)

interactive_engine: $(interactive_src)
	$(JC) $(JFLAGS) $(interactive_src)

scripts:
	chmod +x IndexEngine
	chmod +x InteractiveEngine

# .PHONY tells makefile to treat clean as a command
.PHONY: clean
clean:
	rm -rf $(OUT_DIR)
