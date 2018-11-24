# SPARQL Queries
It is recommended to follow the patterns of SPARQL queries strictly (in order to avoid errors).

## Examples
Examples shown below are related to LUBM benchmark.

### Simple Query with LIMIT
```
SELECT ?author ?publication
WHERE {
	?publication :publicationAuthor ?author .
}
LIMIT 20
```

### UNION of two Queries
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

### Query with FILTER
```
SELECT ?X
WHERE {
    ?X :type :GraduateStudent .
    ?X :takesCourse <http://www.Department0.University0.edu/GraduateCourse0> .
    FILTER (?X != <http://www.Department0.University0.edu/GraduateStudent101>)
}
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
