// packages
package net.sansa_stack.template.spark.rdf

// imports
import java.net.{URI => JavaURI}
import net.sansa_stack.rdf.spark.io.NTripleReader
import net.sansa_stack.rdf.spark.model.{JenaSparkRDDOps, TripleRDD}
import org.apache.spark.sql.SparkSession
import java.nio.file.{Files, Paths, Path, SimpleFileVisitor, FileVisitResult}
import java.nio.file.attribute.BasicFileAttributes
import java.io.IOException

object Main {
  def main(args: Array[String]) = {
    // check input arguments
    if (args.length < 1) {
      System.err.println("No input file found.")
      System.exit(1)
    }

    println("==================================")
    println("|       RDF Data Partition       |")
    println("=================================")

    // setup
    val inputPath       = args(0)
    val outputPath      = "src/main/resources/output/"
    val symbol   = Map(
      "space" -> " ",
      "tabs"  -> "\t",
      "colon" -> ":",
      "hash"  -> "#",
      "dots"  -> "..."
    )
    
    // clear paths
    removePath(Paths.get(outputPath))
    // removePath(Paths.get(logsPath))

    // spark session
    val sparkSession = SparkSession.builder
      .master("local[*]")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("spark.hadoop.validateOutputSpecs", "false")
      .appName("RDF Data Partition")
      .getOrCreate()

    val ops = JenaSparkRDDOps(sparkSession.sparkContext)

    // N-Triples reader
    val nTriplesRDD = NTripleReader.load(sparkSession, JavaURI.create(inputPath))

    // N-Triples log
    nTriplesLog(nTriplesRDD: TripleRDD, ops)

    println("\n")
    println("-----------------------")
    println("Phase 1: Data Partition")
    println("-----------------------")

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
        val filteredPredicate = getPredicate.getURI.split(symbol("hash"))(1)
        var filteredObject: Any = ()

        // filter out object
        if(getPredicate.getURI.contains("type")) {
          filteredObject = getObject.getURI.split(symbol("hash"))(1)
        } else {
          filteredObject = getObject
        }

        // (K,V) pair
        (getSubject, symbol("colon") + filteredPredicate + symbol("space") + filteredObject + symbol("space"))
      }
    )
    .reduceByKey(_ + _) // group based on key
    .map(x => x._1 + symbol("space") + x._2) // output format

    // output some result on console
    partitionedData.collect().take(5).foreach(println)
    println(symbol("dots"))

    // save data to file (1 partitions)
    if(!partitionedData.partitions.isEmpty) {
      partitionedData.repartition(1).saveAsTextFile(outputPath)
    }

    // stop spark
    sparkSession.stop
  }

  // N-Triples log
  def nTriplesLog(graph: TripleRDD, ops: JenaSparkRDDOps): Unit = {
    import ops._

    println("Number of N-Triples: "   + graph.find(ANY, ANY, ANY).distinct.count())
    println("Number of subjects: "    + graph.getSubjects.distinct.count())
    println("Number of predicates: "  + graph.getPredicates.distinct.count())
    println("Number of objects: "     + graph.getObjects.distinct.count())
  }

  // delete folder
  def removePath(root: Path): Unit = {
    Files.walkFileTree(root, new SimpleFileVisitor[Path] {
      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        Files.delete(file)
        FileVisitResult.CONTINUE
      }

      override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = {
        Files.delete(dir)
        FileVisitResult.CONTINUE
      }
    })
  }
}
