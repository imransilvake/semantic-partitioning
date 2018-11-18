# RDD (Resilient Distributed Database)
It is a collection of elements, that can be divided across multiple nodes in a cluster for parallel processing. It is also fault tolerant collection of elements, which means it can automatically recover from failures. RDD is immutable, we can create RDD once but can’t change it.

## Parallelized Collections
Parallelized collections are created by calling SparkContext’s parallelize method on an existing collection in your driver program (a Scala Seq). The elements of the collection are copied to form a distributed dataset that can be operated on in parallel.

```
val data = Array(1, 2, 3, 4, 5)
val distData = sc.parallelize(data)
```

## External Datasets
Spark can create distributed datasets from any storage source (e.g: Local, HDFS, Amazon S3 etc). Text file RDDs can be created using SparkContext’s textFile method (takes URI for the file) and reads it as a collection of lines.

`val distFile = sc.textFile("data.txt")`

Spark creates one partition for each block of the file (blocks being 128MB by default in HDFS), but you can also ask for a higher number of partitions by passing a larger value. Note that you cannot have fewer partitions than blocks.

## RDD Operations
RDDs support two types of operations: 
  - **Transformations**: which create a new dataset from an existing one. For example, `map` is a transformation that passes each dataset element through a function and returns a new RDD representing the results.
  - **Actions**: which return a value to the driver program after running a computation on the dataset. For example, `reduce` is an action that aggregates all the elements of the RDD using some function and returns the final result to the driver program.
  
All transformations in Spark are lazy, in that they do not compute their results right away. Instead, they just remember the transformations applied to some base dataset (e.g. a file). The transformations are only computed when an action requires a result to be returned to the driver program.

By default, each transformed RDD may be recomputed each time you run an action on it. However, you may also persist an RDD in memory using the persist (or cache) method, in which case Spark will keep the elements around on the cluster for much faster access the next time you query it. There is also support for persisting RDDs on disk, or replicated across multiple nodes.

**Example**
```
val lines = sc.textFile("data.txt") // RDD from an external file
val lineLengths = lines.map(s => s.length) // gives lines length as the result of a map transformation
val totalLength = lineLengths.reduce((a, b) => a + b) // action: reduce
```
Reduce is an action. At this stage (on reduce action) Spark breaks the computation into tasks to run on separate machines, and each machine runs both its part of the map and a local reduction, returning only its answer to the driver program.

In order to use lineLengths later on, use: `lineLengths.persist()` before the reduce action, which would cause lineLengths to be saved in memory after the first time it is computed.

## Transformations and Actions on RDD

#### map Transformation
A map transformation is useful when we need to transform a RDD by applying a function to each element.
```
val Length = lines.map(s => s.length)
Length.collect()
```

#### count Action
Return the number of elements in the dataset.
```
lines.count()
```

#### reduce Action
Aggregate the elements of the dataset using a function func (which takes two arguments and returns one).
```
val totalLength = Length.reduce((a, b) => a + b)
```

#### flatMap Transformation and reduceByKey Action
Let’s calculate frequency of each word from the dataset.
```
val counts = lines
                .flatMap(line => line.split(" "))
                .map(word => (word, 1))
                .reduceByKey(_ + _)
counts.collect()
```

#### filter Transformation
Let’s filter out the words from the dataset whose length is more than 5.
```
val output = lines
            .flatMap(line => line.split(" "))
            .filter(_.length > 5)
```
