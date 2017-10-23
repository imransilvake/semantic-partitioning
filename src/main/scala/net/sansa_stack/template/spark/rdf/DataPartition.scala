package net.sansa_stack.template.spark.rdf

import java.nio.file.{Files, Paths}
import net.sansa_stack.rdf.spark.model.JenaSparkRDDOps
import java.util.concurrent.TimeUnit
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.collection.JavaConversions._

class DataPartition(
                     outputPath: String,
                     symbol: Map[String, String],
                     ops: JenaSparkRDDOps,
                     nTriplesRDD: org.apache.spark.rdd.RDD[org.apache.jena.graph.Triple],
                     partitionedDataPath: String,
                     numOfFilesPartition: Int
                   ) extends Serializable {
  def executePartition: Unit = {
    // start process time
    val startTime = System.nanoTime()

    // partition the data
    val partitionedData = nTriplesRDD
      .filter(
        line => {
          // ignore subjects having empty URI
          !line.getSubject.getURI.isEmpty
        }
      )
      .map(line => {
        // subject, predicate and object
        val getSubject    = line.getSubject
        val getPredicate  = line.getPredicate
        val getObject     = line.getObject

        // filter out predicate
        var filteredPredicate: Any = ()
        if(getPredicate.isURI && getPredicate.getURI.contains(this.symbol("hash"))) {
          filteredPredicate = getPredicate.getURI.split(this.symbol("hash"))(1)
        } else {
          filteredPredicate = getPredicate
        }

        // filter out object
        var filteredObject: Any = ()
        if(getPredicate.isURI && getPredicate.getURI.contains("type")) {
          filteredObject = getObject.getURI.split(this.symbol("hash"))(1)
        } else if (getObject.isURI) {
          filteredObject = getObject.getURI.substring(getObject.getURI.lastIndexOf('/') + 1)
        } else {
          filteredObject = getObject
        }

        // (K,V) pair
        (getSubject, this.symbol("colon") + filteredPredicate + this.symbol("space") + filteredObject + this.symbol("space"))
      })
      .reduceByKey(_ + _) // group based on key
      .map(x => x._1 + this.symbol("space") + x._2) // output format

    // output some result on console
    partitionedData.collect().take(5).foreach(println)
    println(this.symbol("dots"))

    // end process time
    this.partitionTime(System.nanoTime() - startTime)

    // save data to file (8 partition)
    if(!partitionedData.partitions.isEmpty) {
      partitionedData.repartition(this.numOfFilesPartition).saveAsTextFile(this.outputPath)
    }
  }

  // total partition time
  def partitionTime(processedTime: Long): Unit = {
    println("\nProcessed Time (MILLISECONDS): " + TimeUnit.MILLISECONDS.convert(processedTime, TimeUnit.NANOSECONDS))
    println("Processed Time (SECONDS): " + TimeUnit.SECONDS.convert(processedTime, TimeUnit.NANOSECONDS))
    println("Processed Time (MINUTES): " + TimeUnit.MINUTES.convert(processedTime, TimeUnit.NANOSECONDS))
  }

  // get partition data
  def getPartitionData: ArrayBuffer[String] = {
    var lines: ArrayBuffer[String] = ArrayBuffer()

    Files
      .newDirectoryStream(Paths.get(this.partitionedDataPath))
      .filter(
        file => {
          // ignore files extension .crc
          file.toString.contains("part-") && !file.toString.contains(".crc")
        }
      )
      .foreach(file => {
        for (line <- Source.fromFile(file.toString).getLines) {
          lines += line
        }
      })

    lines
  }
}
