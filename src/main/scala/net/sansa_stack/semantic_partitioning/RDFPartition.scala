// package
package net.sansa_stack.semantic_partitioning

// imports
import java.util.concurrent.TimeUnit
import org.apache.jena.graph.Triple
import org.apache.spark.rdd._
import org.apache.log4j.Logger

/*
 * RdfPartition - semantic partition of and RDF graph
 *
 * @symbol - list of symbols.
 * @nTriplesRDD - a RDD of n-triples.
 * @partitionedDataPath - path for partition data.
 * @numOfFilesPartition - total number of files to save the partition data.
 *
 * @return - semantic partition data.
 */
class RDFPartition(
                      symbol: Map[String, String],
                      nTriplesRDD: RDD[Triple],
                      partitionedDataPath: String,
                      numOfFilesPartition: Int
                  ) extends Serializable {
    @transient lazy val consoleLog: Logger = Logger.getLogger(getClass.getName)
    var _partitionData: RDD[String] = _

    def run(): Unit = {
        // start process time
        val startTime = System.nanoTime()

        // partition graph
        val partitionedData = this.partitionGraph()

        // end process time
        this.partitionTime(System.nanoTime() - startTime)

        // save data to output file
        if (partitionedData.partitions.nonEmpty) {
            partitionedData.repartition(this.numOfFilesPartition).saveAsTextFile(this.partitionedDataPath)
        }
    }

    // partition graph
    def partitionGraph(): RDD[String] = {
        // partition the data
        _partitionData =
            nTriplesRDD
                .distinct
                .filter(line => line.getSubject.getURI.nonEmpty) // ignore SUBJECT with empty URI
                .map(line => {
                    // SUBJECT, PREDICATE and OBJECT
                    val getSubject = line.getSubject
                    val getPredicate = line.getPredicate
                    val getObject = line.getObject

                    var filteredPredicate: Any = getPredicate
                    var filteredObject: Any = getObject

                    // set: PREDICATE
                    if (getPredicate.isURI && getPredicate.getURI.contains(this.symbol("hash"))) {
                        filteredPredicate = getPredicate.getURI.split(this.symbol("hash"))(1)

                        // set: OBJECT where PREDICATE is a "type"
                        if (filteredPredicate.equals("type") && getObject.isURI && getObject.getURI.contains(this.symbol("hash")))
                            filteredObject = this.symbol("colon") + getObject.getURI.split(this.symbol("hash"))(1)
                        else if (getObject.isURI)
                            filteredObject = this.symbol("less-than") + getObject + this.symbol("greater-than")
                        else
                            filteredObject = getObject

                        // add colon at the start
                        filteredPredicate = this.symbol("colon") + filteredPredicate
                    } else {
                        // PREDICATE
                        if (getPredicate.isURI)
                            filteredPredicate = this.symbol("less-than") + getPredicate + this.symbol("greater-than")

                        // OBJECT
                        if (getObject.isURI)
                            filteredObject = this.symbol("less-than") + getObject + this.symbol("greater-than")
                    }

                    // (K,V) pair
                    (
                        this.symbol("less-than") + getSubject + this.symbol("greater-than"),
                        filteredPredicate + this.symbol("space") + filteredObject + this.symbol("space")
                    )
                })
                .reduceByKey(_ + _) // group based on key
                // .sortBy(x => x._1) // sort by key
                .map(x => x._1 + this.symbol("space") + x._2) // output format

        _partitionData
    }

    // partition time
    def partitionTime(processedTime: Long): Unit = {
        val milliseconds = TimeUnit.MILLISECONDS.convert(processedTime, TimeUnit.NANOSECONDS)
        val seconds = Math.floor(milliseconds/1000d + .5d).toInt
        val minutes = TimeUnit.MINUTES.convert(processedTime, TimeUnit.NANOSECONDS)

        if (milliseconds >= 0) {
            consoleLog.info(s"Processed Time (MILLISECONDS): $milliseconds")

            if (seconds > 0) {
                consoleLog.info(s"Processed Time (SECONDS): $seconds approx.")

                if (minutes > 0) {
                    consoleLog.info(s"Processed Time (MINUTES): $minutes")
                }
            }
        }
    }
}
