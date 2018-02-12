# SANSA Semantic Partitioning
RDF Data (N-Triples) Partition and SPARQL Query Layer for [SANSA-Stack](https://github.com/SANSA-Stack) using Scala and Spark.


##  Benchmarks (N-Triples Datasets): 

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


## Program Options Setting

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


## SPARQL Operators
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


## SPARQL Queries
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


## Run on Spark Cluster
An example of how to run the app on the cluster.

#### Local
```
spark-submit \
--class net.sansa_stack.semantic_partitioning.Semantic \
--master local[*] \
--driver-java-options "-DLogFilePath=/Users/imransilvake/Downloads/test/run/" \
/SANSA-Semantic-Partitioning/target/SANSA-Semantic-Partitioning-0.3.1-SNAPSHOT.jar \
--input /SANSA-Semantic-Partitioning/src/main/resources/input/lubm/sample.nt \
--queries /SANSA-Semantic-Partitioning/src/main/resources/queries/lubm/query-01.txt \
--partitions /SANSA-Semantic-Partitioning/src/main/resources/output/partitioned-data/ \
--output /SANSA-Semantic-Partitioning/src/main/resources/output/query-result/
```

## Useful Commands

#### General
- Check running processes: `jps`
- Kill process: `kill -9 PID`
- Copy from local to remote: `scp -r /path/to/file UserName@server.com:/path/to/destination`

#### Hadoop
- Check Hadoop hostname and port: `hdfs getconf -confKey fs.default.name`
- Put files on HDFS: `Hadoop fs -put file /user/imransilvake`
- Check files on HDFS: `Hadoop fs -ls`
- Remove Files on HDFS: `hadoop fs -rm -R path_of_file_or_dir`
- Default address of namenode web UI: `http://localhost:50070/`

```
The default Hadoop ports are as follows: (HTTP ports, they have WEB UI):
-----------------------  ------------ ----------------------------------
Daemon                   Default Port  Configuration Parameter
-----------------------  ------------ ----------------------------------
Namenode                 50070        dfs.http.address
Datanodes                50075        dfs.datanode.http.address
Secondarynamenode        50090        dfs.secondary.http.address
Backup/Checkpoint node?  50105        dfs.backup.http.address
Jobracker                50030        mapred.job.tracker.http.address
Tasktrackers             50060        mapred.task.tracker.http.address
```

```
Internally, Hadoop mostly uses Hadoop IPC, which stands for Inter Process Communicator, 
to communicate amongst servers. The following table presents the ports and protocols that 
Hadoop uses. This table does not include the HTTP ports mentioned above.

------------------------------------------------------------
Daemon      Default Port        Configuration Parameter     
------------------------------------------------------------
Namenode    8020                fs.default.name         
Datanode    50010               dfs.datanode.address        
Datanode    50020               dfs.datanode.ipc.address                                    
Backupnode  50100               dfs.backup.address
```

#### Spark
- Run Spark Shell: `spark-shell`


## Future Work
 - Implement Prefix for SPARQL queries
 - Add more operators
 - Add support in FILTER: 
    - **Math:** ```+, -, *, /```
    - **SPARQL Tests:** ```bound```
    - **SPARQL Accessors:** ```str```
    - **Other:** ```sameTerm, langMatches, regex```
 - Show predicate in the final result (for flexibility)
