// packages
package net.sansa_stack.template.spark.rdf

// imports
import java.nio.file.{FileVisitResult, Files, Paths, Path, SimpleFileVisitor}
import java.nio.file.attribute.BasicFileAttributes
import java.io.IOException
import net.sansa_stack.rdf.spark.model.{JenaSparkRDDOps, TripleRDD}
import java.net.{URI => JavaURI}
import net.sansa_stack.rdf.spark.io.NTripleReader
import org.apache.spark.sql.SparkSession
import scala.collection.mutable.ArrayBuffer

object main {
  def main(args: Array[String]): Unit = {
    // check input arguments
    if (args.length < 1) {
      System.err.println("No input file found.")
      System.exit(1)
    }

    println("==================================")
    println("|       RDF Data Partition       |")
    println("=================================")

    // setup
    val inputPath: String = args(0)
    val outputPath: String = "src/main/resources/output/partitioned-data/"
    val partitionedDataPath: String = "src/main/resources/output/partitioned-data/"
    val queryPath: String = args(1)
    val numOfFilesPartition: Int = 2
    val symbol = Map(
      "space" -> " " * 3,
      "blank" -> " ",
      "tabs"  -> "\t",
      "colon" -> ":",
      "hash"  -> "#",
      "slash" -> "/",
      "dots"  -> "..."
    )
    
    // clear paths
    removePath(Paths.get(outputPath))

    // spark session
    val sparkSession = SparkSession.builder
      .master("local[*]")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .appName("RDF Data Partition")
      .getOrCreate()

    val ops = JenaSparkRDDOps(sparkSession.sparkContext)

    // N-Triples reader
    val nTriplesRDD = NTripleReader.load(sparkSession, JavaURI.create(inputPath))

    // output N-Triples log
    nTriplesLog(nTriplesRDD: TripleRDD, ops)

    println("\n")
    println("-----------------------")
    println("Phase 1: Data Partition")
    println("-----------------------")

    val dp = new DataPartition(
      outputPath,
      symbol,
      ops,
      nTriplesRDD,
      partitionedDataPath,
      numOfFilesPartition
    )
    dp.executePartition
    val partitionData = dp.getPartitionData

    // output number of N-Triples
    numOfNTriples(partitionData)

    println("\n")
    println("---------------------")
    println("Phase 2: Query System")
    println("---------------------")

    // get partition data
    val qs = new QuerySystem(queryPath, symbol)
    qs.queriesParser
  }

  // delete path
  def removePath(root: Path): Unit = {
    if(Files.exists(root)) {
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

  // N-Triples log
  def nTriplesLog(graph: TripleRDD, ops: JenaSparkRDDOps): Unit = {
    import ops._

    println("Number of N-Triples: "   + graph.find(ANY, ANY, ANY).distinct.count())
    println("Number of subjects: "    + graph.getSubjects.distinct.count())
    println("Number of predicates: "  + graph.getPredicates.distinct.count())
    println("Number of objects: "     + graph.getObjects.distinct.count())
  }

  // number of N-Triples
  def numOfNTriples(partitionData: ArrayBuffer[String]): Unit = {
    println("\nNumber of N-Triples: " + partitionData.length)
  }
}
