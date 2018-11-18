# Accumulators (write-only)
Accumulators are variables that are used for aggregating information across the executors.

```
// initialize data
val file = sc.textFile("path")
var numBlankLines = spark.sparkContext.accumulator(0)

def toWords(line:String): Array[String] = {
    // if no data in a file
    if(line.length == 0)
        numBlankLines += 1

    // split
    line.split(" ")
}

// count blank lines
val words = file.flatMap(toWords)

// output
words.foreach(println)
printf("Blank lines: %d", numBlankLines.value)
```
