package net.sansa_stack.template.spark.rdf
import net.sansa_stack.rdf.spark.model.JenaSparkRDDOps

class DataPartition(
                     outputPath: String,
                     symbol: Map[String, String],
                     ops: JenaSparkRDDOps,
                     nTriplesRDD: org.apache.spark.rdd.RDD[org.apache.jena.graph.Triple]
                   ) extends Serializable {
  def dataPartition(): Unit = {
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

    // save data to file (1 partitions)
    if(!partitionedData.partitions.isEmpty) {
      partitionedData.repartition(1).saveAsTextFile(this.outputPath)
    }
  }
}
