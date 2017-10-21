// packages
package net.sansa_stack.template.spark.rdf

// imports
import java.net.{URI => JavaURI}
import net.sansa_stack.rdf.spark.io.NTripleReader
import net.sansa_stack.rdf.spark.model.{JenaSparkRDDOps, TripleRDD}
import org.apache.spark.sql.SparkSession

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

    // initialize
    val outputResultsPath = "src/main/resources/output/results/"
    val inputPath = args(0)

    // spark session
    val sparkSession = SparkSession.builder
      .master("local[*]")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("spark.hadoop.validateOutputSpecs", "false")
      .appName("Data Partition: (" + inputPath + ")")
      .getOrCreate()

    val ops = JenaSparkRDDOps(sparkSession.sparkContext)
    import ops._

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
        val filteredPredicate = ":" + getPredicate.getURI.split("#")(1)

        // filter out object
        val filteredObject = if(getObject.isURI) getObject.getURI.split(".owl#")(1) else getObject

        // map format
        (getSubject, filteredPredicate + " " + filteredObject)
      }
    ).groupByKey()

    // save data to file
    partitionedData.repartition(1).saveAsTextFile(outputResultsPath)

    partitionedData.foreach(println)

    sparkSession.stop
  }

  // N-Triples log
  def nTriplesLog(graph: TripleRDD, ops: JenaSparkRDDOps): Unit = {
    import ops._

    println("Number of N-Triples: " + graph.find(ANY, ANY, ANY).distinct.count())
    println("Number of subjects: " + graph.getSubjects.distinct.count())
    println("Number of predicates: " + graph.getPredicates.distinct.count())
    println("Number of objects: " + graph.getObjects.distinct.count())
  }
}
