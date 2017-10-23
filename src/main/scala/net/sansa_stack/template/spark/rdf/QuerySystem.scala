package net.sansa_stack.template.spark.rdf

import scala.collection.mutable.ArrayBuffer
import java.util.Scanner
import java.io.File

class QuerySystem(queryPath: String, symbol: Map[String, String]) {
  def queriesParser: Unit = {
    this.addQueries.foreach({ query =>
      this.queryParser(query)
    })
  }

  def addQueries: ArrayBuffer[ArrayBuffer[String]] = {
    val file = new File(queryPath)
    val fileScanner = new Scanner(file)
    var queryList: ArrayBuffer[ArrayBuffer[String]] = ArrayBuffer()

    while(fileScanner.hasNext) {
      var line = fileScanner.nextLine
      var isEnd = false

      // query should start with select
      if(line.startsWith("SELECT") || line.startsWith("select") || line.startsWith("Select")) {
        var singleQuery: ArrayBuffer[String] = ArrayBuffer()
        singleQuery += line

        // add elements until "}" found
        while(fileScanner.hasNext && !isEnd) {
          line = fileScanner.nextLine

          // reach end
          if(line.endsWith("}")) {
            isEnd = true
          }

          singleQuery += line
        }

        // all queries
        queryList += singleQuery
      }
    }

    queryList
  }

  def queryParser(query: ArrayBuffer[String]): Unit = {
    println(query)
  }
}
