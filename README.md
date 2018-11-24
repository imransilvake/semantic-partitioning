# Semantic Partitioning
SANSA Semantic Partitioning is a scalable and highly efficient application that first perform in-memory RDF Data (N-Triples) Partition and then pass the partitioning data to the SPARQL Query Engine layer to get efficient results. It is built on top of [SANSA-Stack](https://github.com/SANSA-Stack) using the Scala and Spark technologies.

![Alt text](preview.png?raw=true "Semantic Partitioning")

Read here: [Scala & Spark](content/learn-scala-spark/README.md)


##  Benchmarks
The datasets should be in N-Triples format.

### [LUBM](https://github.com/rvesse/lubm-uba) 
`./generate.sh --quiet --timing -u 1 --format NTRIPLES  --consolidate Maximal --threads 8`
### [BSBM](https://sourceforge.net/projects/bsbmtools/files/bsbmtools/bsbmtools-0.2/bsbmtools-v0.2.zip/download) 
`./generate -fc -s nt -fn dataset_10MB -pc 100`
### [DBpedia](http://benchmark.dbpedia.org/)
`direct download`


## Application Settings

### VM Options
```
-DLogFilePath=/SANSA-Semantic-Partitioning/src/main/resources/log/console.log
```

### Program Arguments
```
--input /SANSA-Semantic-Partitioning/src/main/resources/input/lubm/sample.nt
--queries /SANSA-Semantic-Partitioning/src/main/resources/queries/lubm/query-01.txt
--partitions /SANSA-Semantic-Partitioning/src/main/resources/output/partitioned-data/
--output /SANSA-Semantic-Partitioning/src/main/resources/output/query-result/
```


## SPARQL Operators
Read here: [SPARQL Operators](documentation/operators.md)


## SPARQL Queries
Read here: [SPARQL Queries](documentation/queries.md)


## Deploy App on Cluster
Read here: [App Deploy](documentation/deployment.md)


## Future Work
 - Implement Prefix for SPARQL queries
 - Add more operators
 - Add support in FILTER: 
    - **Math:** ```+, -, *, /```
    - **SPARQL Tests:** ```bound```
    - **SPARQL Accessors:** ```str```
    - **Other:** ```sameTerm, langMatches, regex```
 - Show predicate in the final result (for flexibility)
