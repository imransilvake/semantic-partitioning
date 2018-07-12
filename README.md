## Efficient and Scalable in-memory Semantic Partitioning for RDF Data
RDF Data (N-Triples) Partition and SPARQL Query Layer for [SANSA-Stack](https://github.com/SANSA-Stack) using Scala and Spark.

![Alt text](preview.png?raw=true "Semantic Partitioning")


###  Benchmarks (in N-Triples): 

1. LUBM
    1. Clone this project: [LUBM](https://github.com/rvesse/lubm-uba)
    2. Run Commands
    ```
    ./generate.sh --quiet --timing -u 1 --format NTRIPLES  --consolidate Maximal --threads 8
    ./generate.sh --quiet --timing -u 10 --format NTRIPLES  --consolidate Maximal --threads 8
    ./generate.sh --quiet --timing -u 100 --format NTRIPLES  --consolidate Maximal --threads 8
    ```

2. BSBM
    1. Download the BSBM tool: [BSBM](https://sourceforge.net/projects/bsbmtools/files/bsbmtools/bsbmtools-0.2/bsbmtools-v0.2.zip/download)
    2. Unzip and Go inside the folder
    3. Run Command
    ```
    ./generate -fc -s nt -fn dataset_10MB -pc 100
    ./generate -fc -s nt -fn dataset_100MB -pc 1000
    ./generate -fc -s nt -fn dataset_1GB -pc 10000
    ```
3. DBpedia - [Datasets](http://benchmark.dbpedia.org/)


### Application Settings

#### VM Options
```
-DLogFilePath=/SANSA-Semantic-Partitioning/src/main/resources/log/console.log
```

#### Program Arguments
```
--input /SANSA-Semantic-Partitioning/src/main/resources/input/lubm/sample.nt
--queries /SANSA-Semantic-Partitioning/src/main/resources/queries/lubm/query-01.txt
--partitions /SANSA-Semantic-Partitioning/src/main/resources/output/partitioned-data/
--output /SANSA-Semantic-Partitioning/src/main/resources/output/query-result/
```


### SPARQL Operators
###### Note: All operators are case insensitive
 - **Mandatory**: ```SELECT, WHERE```
 - **Optional**: ```LIMIT, UNION, FILTER```

#### LIMIT
 - Only accepts **Integer** value

#### FILTER
 - **Logical:** ```!, &&, ||```
 - **Comparison:** ```<, >, = or ==, >=, <=, !=```
 - **SPARQL Tests:** ```isURI, isBlank, isLiteral```
    1. isURI: ```FILTER (isURI(?author))```
    2. isBlank: ```FILTER (isBlank(?author))```
    3. isLiteral: ```FILTER (isLiteral(?author))```
 - **SPARQL Accessors:** ```lang, datatype```
    1. lang: ```FILTER (lang(?name) = "ES")```
    2. datatype: ```FILTER (datatype(?name) = string)```

###### Single Line
```    
FILTER (?X == <http://www.Department6.University0.edu/UndergraduateStudent103>)
```
    
###### Multi Line
```    
FILTER (
    ?X == <http://www.Department6.University0.edu/UndergraduateStudent103> &&
    ?Z = <http://www.Department6.University0.edu/Course1>
)
```


### SPARQL Queries
#### Important: Strictly follow the pattern of SPARQL queries in order to avoid errors.
###### Note: Examples shown below are related to LUBM benchmark


###### Simple Query
```
SELECT ?author ?publication
WHERE {
	?publication :publicationAuthor ?author .
}
```

###### Query with LIMIT
```
SELECT *
WHERE {
	?publication :publicationAuthor ?author .
}
LIMIT 20
```

###### UNION of two (or more) Queries
```
SELECT *
WHERE {
    { ?X :type :UndergraduateStudent . }
    UNION
    {
        ?X :type :GraduateStudent .
        ?X :takesCourse <http://www.Department0.University0.edu/GraduateCourse0> .
    }
}
```

###### Query with FILTER
```
SELECT ?X
WHERE {
    ?X :type :GraduateStudent .
    ?X :takesCourse <http://www.Department0.University0.edu/GraduateCourse0> .
    FILTER (?X != <http://www.Department0.University0.edu/GraduateStudent101>)
}
```


### Submitting Applications
An example of how to run the app on the cluster.

#### Local `master("local[*]")`
###### Without HDFS
```
spark-submit \
--class net.sansa_stack.semantic_partitioning.Semantic \
--master local[*] \
--driver-java-options "-Dlog4j.configuration=file:/Users/imransilvake/IdeaProjects/SANSA-Semantic-Partitioning/src/main/resources/log4j.properties -DLogFilePath=/Users/imransilvake/IdeaProjects/SANSA-Semantic-Partitioning/src/main/resources/log/console.log/src/main/resources/log/console.log" \
/SANSA-Semantic-Partitioning-0.3.1-SNAPSHOT.jar \
--input /sample.nt \
--queries /query-01.txt \
--partitions /output/partitioned-data/ \
--output /output/results-data/
```

###### With HDFS
```
spark-submit \
--class net.sansa_stack.semantic_partitioning.Semantic \
--master local[*] \
--driver-java-options "-Dlog4j.configuration=file:/Users/imransilvake/IdeaProjects/SANSA-Semantic-Partitioning/src/main/resources/log4j.properties -DLogFilePath=/Users/imransilvake/IdeaProjects/SANSA-Semantic-Partitioning/src/main/resources/log/console.log/src/main/resources/log/console.log" \
hdfs://localhost:9000/user/imransilvake/SANSA-Semantic-Partitioning-0.3.1-SNAPSHOT.jar \
--input hdfs://localhost:9000/user/imransilvake/sample.nt \
--queries hdfs://localhost:9000/user/imransilvake/query-01.txt \
--partitions /output/partitioned-data/ \
--output /output/results-data/
```

#### Standalone Cluster `master("spark://172.18.160.16:3077")`
```
spark-submit \
--class net.sansa_stack.semantic_partitioning.Semantic \
--master spark://172.18.160.16:3077 \
--driver-java-options "-Dlog4j.configuration=file:/data/home/ImranKhan/log4j.properties -DLogFilePath=/data/home/ImranKhan/console.log" \
hdfs://172.18.160.17:54310/ImranKhan/apps/semantic/app.jar \
--input hdfs://172.18.160.17:54310/ImranKhan/datasets/lubm/small.nt \
--queries hdfs://172.18.160.17:54310/ImranKhan/queries/lubm/query-01.txt \
--partitions /data/home/ImranKhan/partitioned-data/ \
--output /data/home/ImranKhan/results-data/
```


### Useful Commands

#### General
- Check running processes: `jps`
- Kill process: `kill -9 PID`
- Copy from local to remote: `scp -r /path/to/file UserName@server.com:/path/to/destination`

#### Hadoop
- Check Hadoop hostname and port: `hdfs getconf -confKey fs.default.name`
- Put files on HDFS: `Hadoop fs -put file path`
- Check files on HDFS: `Hadoop fs -ls /`
- Remove Files on HDFS: `hadoop fs -rm -R path`
- Make a directory on HDFS: `hadoop fs -mkdir path`
- Move location on HDFS: `hadoop fs -mv source destination`

#### Spark
- Run Spark Shell: `spark-shell`


### Future Work
 - Implement Prefix for SPARQL queries
 - Add more operators
 - Add support in FILTER: 
    - **Math:** ```+, -, *, /```
    - **SPARQL Tests:** ```bound```
    - **SPARQL Accessors:** ```str```
    - **Other:** ```sameTerm, langMatches, regex```
 - Show predicate in the final result (for flexibility)
