# Broadcast (read-only)
Broadcast variables allow to give every node a copy of a large input dataset in an efficient manner.

**Scenario:** Assume you have 100 executors running a task that needs some huge dictionary lookup. Without broadcast variables, you would load this data in every executor. With broadcast variables, you just have to load it once and all the executors will refer to the same dictionary. Hence you save a lot of space.

```
// initialize data
val file = sc.textFile("path")
val commonWords = scala.collection.mutable.ArrayBuffer(
    "a", "an", "the", "of", "at", "is", 
    "am", "are", "this", "that", "at", "in", 
    "or", "and", "or", "not", "be", "for", 
    "to", "it"
)
val commonWordsMap = collection.mutable.Map[String, Int]()
for(word <- commonWords) commonWordsMap(word) = 1 // Map(am -> 1, is -> 1)

// broadcast: commonWordsMap
val commonWordsBC = sc.broadcast(commonWordsMap)

// extract words that are not contained in commonWords
def toWords(data:String): ArrayBuffer[String] = {
    val words = data.split(" ")
    val output = ArrayBuffer[String]()
    for(word <- words) {
        if(!commonWordsBC.value.contains(word.toLowerCase.trim.replaceAll("[^a-z]","")))
            output.append(word)
    }

    output
}

// flatMap
val uncommonWordsRDD = file.flatMap(toWords)

// output
uncommonWordsRDD.take(100).foreach(println)
```
