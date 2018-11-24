# Deploy Application on Cluster
An example of how to run the app on the cluster.

## Local `master("local[*]")`
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

## Standalone Cluster `master("spark://172.18.160.16:3077")`
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
