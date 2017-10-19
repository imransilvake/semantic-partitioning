// packages
package net.sansa_stack.template.spark.rdf

// imports
import java.net.{URI => JavaURI}
import net.sansa_stack.rdf.spark.io.NTripleReader
import net.sansa_stack.rdf.spark.model.{JenaSparkRDDOps, TripleRDD}
import org.apache.spark.sql.SparkSession

object DataPartitionMain {
  def main(args: Array[String]) = {
    // check input arguments
    if (args.length < 1) {
      System.err.println("No input file found.")
      System.exit(1)
    }

    println("================================")
    println("|       Data Partitioner       |")
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
    val triplesRDD = NTripleReader.load(sparkSession, JavaURI.create(inputPath))

    // partition the data
    val data = triplesRDD
      .filter(
        line => {
          // ignore subjects having empty URI
          !line.getSubject.getURI.isEmpty
        }
      )
      .map( line => {
        val getSubject    = line.getSubject
        val getPredicate  = line.getPredicate
        val getObject     = line.getObject

        (getSubject, getPredicate + " " + getObject)
      }
    ).groupByKey()

    // save data to file
    data.repartition(1).saveAsTextFile(outputResultsPath)

    // triples
    // val graph: TripleRDD = triplesRDD

    // additional information.
    // println("Number of triples: " + graph.find(ANY, ANY, ANY).distinct.count())
    // println("Number of subjects: " + graph.getSubjects.distinct.count())
    // println("Number of predicates: " + graph.getPredicates.distinct.count())
    // println("Number of objects: " + graph.getObjects.distinct.count())

    sparkSession.stop
  }
}
