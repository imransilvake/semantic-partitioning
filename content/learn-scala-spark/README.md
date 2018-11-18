# Learn-Scala-Spark
Learning content related to Scala and Spark

## Contents
- Scala
- Spark
  - Data Representation
    - RDD (Resilient Distributed Database)
    - Dataset
    - DataFrame
  - Shared Variables
    - Broadcast
    - Accumulators

## Scala
Scala combines object-oriented and functional programming in one concise, high-level language. Scala's static types help avoid bugs in complex applications, and its JVM and JavaScript runtimes let you build high-performance systems with easy access to huge ecosystems of libraries.

### Key Points
- Type-safe language
- Concise
- Object-oriented and functional programming language
  - Based on mathematical functions
  - Uses immutable data
  - Support parallel processing
  - Functions have no side-effects
- Can run java code
- Data Structures
  - Arrays (immutable): collection of similar elements
  - Lists (immutable):  items all have the same type
  - Sets (immutable): allows only distinct elements and eliminate the duplicates.
  - Tuple
  - Maps
  - Option

## Spark
Spark applications may run as independent sets of parallel processes distributed across numerous nodes of computers.  Numerous nodes collaborating together is commonly known as a cluster.  Spark processes are coordinated by a SparkContext object.  The SparkContext can connect to several types of cluster managers like YARN or Spark’s own internal cluster manager called “Standalone”. Once connected to the cluster manager, Spark acquires executors on nodes within the cluster.

<p align="center"><img src="spark-workflow.png?raw=true" alt="Spark Workflow"/></p>

Spark uses a Master/Slave architecture. It has one central coordinator **(driver)** that communicates with many distributed workers **(executors)**. The driver and each of the executors run in their own Java processes.

**Driver**
The driver is the process where the main method runs. First it converts the user program into tasks and after that it schedules the tasks on the executors.

**Executors:** Executors are worker nodes processes in charge of running individual tasks in a given Spark job. They are launched at the beginning of a Spark application and typically run for the entire lifetime of an application. Once they have run the task they send the results to the driver. They also provide in-memory storage for RDDs that are cached by user programs through Block Manager.

### Application Execution Flow
With this in mind, when you submit an application to the cluster with spark-submit this is what happens internally:

- A standalone application starts and instantiates a SparkContext instance (and it is only then when you can call the application a driver).
- The driver program ask for resources to the cluster manager to launch executors.
- The cluster manager launches executors.
- The driver process runs through the user application. Depending on the actions and transformations over RDDs task are sent to executors.
- Executors run the tasks and save the results.
- If any worker crashes, its tasks will be sent to different executors to be processed again.
  - Spark automatically deals with failed or slow machines by re-executing failed or slow tasks. For example, if the node running a partition of a `map()` operation crashes, Spark will rerun it on another node; and even if the node does not crash but is simply much slower than other nodes, Spark can preemptively launch a “speculative” copy of the task on another node, and take its result if that finishes.
- With `SparkContext.stop()` from the driver or if the main method exits/crashes all the executors will be terminated and the cluster resources will be released by the cluster manager.

### Data Representation
- **RDD (Resilient Distributed Database):** [read here](spark/data-representation/rdd.md)
- **DataFrame:** In Spark, a DataFrame is a distributed collection of data organized into named columns. It is conceptually equivalent to a table in a relational database or a data frame. It is mostly used for structured data processing. In Scala, a DataFrame is represented by a Dataset of Rows. A DataFrame can be constructed by wide range of arrays for example, existing RDDs, Hive tables, database tables.
- **Dataset:** It is also a distributed collection of data. A Dataset can be constructed from JVM objects and then manipulated using functional transformations (map, flatMap, filter, etc.).

### Shared Variables
Normally, when a function passed to a Spark operation (such as map or reduce) is executed on a remote cluster node, it works on separate copies of all the variables used in the function. These variables are copied to each machine, and no updates to the variables on the remote machine are propagated back to the driver program. Spark does provide two limited types of shared variables:

- **Broadcast:** [read here](spark/shared-variables/broadcast.md)
- **Accumulators:** [read here](spark/shared-variables/accumulators.md)
