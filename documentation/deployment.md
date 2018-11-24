# Deploy Application on Cluster
Once a user application is bundled, it can be launched using the bin/spark-submit script. This script takes care of setting up the classpath with Spark and its dependencies, and can support different cluster managers and deploy modes that Spark supports.

## Examples

### Local
master: `local[*]`
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

### Standalone Cluster
master `spark://172.18.160.16:3077`
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

## Useful Commands

### General
- Check running processes: `jps`
- Kill process: `kill -9 PID`
- Copy from local to remote: `scp -r /path/to/file UserName@server.com:/path/to/destination`

### Hadoop
- Check Hadoop hostname and port: `hdfs getconf -confKey fs.default.name`
- Put files on HDFS: `Hadoop fs -put file path`
- Check files on HDFS: `Hadoop fs -ls /`
- Remove Files on HDFS: `hadoop fs -rm -R path`
- Make a directory on HDFS: `hadoop fs -mkdir path`
- Move location on HDFS: `hadoop fs -mv source destination`

### Spark
- Run Spark Shell: `spark-shell`
