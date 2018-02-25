// package
package net.sansa_stack.semantic_partitioning

// imports
import java.io.IOException
import java.net.{URI => JavaURI}
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file._
import net.sansa_stack.rdf.spark.io.NTripleReader
import org.apache.spark.sql.SparkSession
import scopt.OptionParser
import org.apache.log4j.Logger

/*
 * Run SPARQL queries over Spark using Semantic partitioning approach.
 *
 * @author Imran Khan
 */
object Semantic {
    @transient lazy val consoleLog: Logger = Logger.getLogger(getClass.getName)

    def main(args: Array[String]) {
        parser.parse(args, Config()) match {
            case Some(config) =>
                run(config.in, config.queries, config.partitions, config.out)
            case None =>
                consoleLog.warn(parser.usage)
        }
    }

    def run(input: String, queries: String, partitions: String, output: String): Unit = {
        // remove path files
        removePathFiles(Paths.get(partitions))
        removePathFiles(Paths.get(output))

        consoleLog.info("=================================")
        consoleLog.info("| SANSA - Semantic Partitioning |")
        consoleLog.info("=================================")

        // variables initialization
        val numOfFilesPartition: Int = 1
        val symbol = Map(
            "space" -> " " * 5,
            "blank" -> " ",
            "tabs" -> "\t",
            "newline" -> "\n",
            "colon" -> ":",
            "comma" -> ",",
            "hash" -> "#",
            "slash" -> "/",
            "question-mark" -> "?",
            "exclamation-mark" -> "!",
            "curly-bracket-left" -> "{",
            "curly-bracket-right" -> "}",
            "round-bracket-left" -> "(",
            "round-bracket-right" -> ")",
            "less-than" -> "<",
            "greater-than" -> ">",
            "at" -> "@",
            "dot" -> ".",
            "dots" -> "...",
            "asterisk" -> "*",
            "up-arrows" -> "^^"
        )

        // spark session
        val spark = SparkSession.builder
            .master("local[*]")
            .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
            .appName("SANSA - Semantic Partitioning")
            .getOrCreate()

        // N-Triples reader
        val nTriplesRDD = NTripleReader.load(spark, JavaURI.create(input))

        consoleLog.info("----------------------")
        consoleLog.info("Phase 1: RDF Partition")
        consoleLog.info("----------------------")

        // class instance: Class RDFPartition and set the partition data
        val ps = new RDFPartition(
            symbol,
            nTriplesRDD,
            partitions,
            numOfFilesPartition
        )
        ps.run()
        val partitionData = ps._partitionData

        // count total number of N-Triples
        // consoleLog.info(s"Number of N-Triples before partition: ${nTriplesRDD.count}")
        // consoleLog.info(s"Number of N-Triples after partition: ${partitionData.count}")

        consoleLog.info("----------------------")
        consoleLog.info("Phase 2: SPARQL System")
        consoleLog.info("----------------------")

        // class instance: Class QuerySystem
        val qs = new QuerySystem(
            symbol,
            partitionData,
            queries,
            output,
            numOfFilesPartition
        )
        qs.run()

        spark.close()
    }

    // remove path files
    def removePathFiles(root: Path): Unit = {
        if (Files.exists(root)) {
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

    // show help options in terminal
    case class Config(in: String = "", queries: String = "", partitions: String = "", out: String = "")

    val parser: OptionParser[Config] = new scopt.OptionParser[Config]("SANSA - Semantic Partitioning") {
        head("Semantic Partitioning: Partition and Query System")

        opt[String]('i', "input").required().valueName("<path>").
            action((x, c) => c.copy(in = x)).
            text("path to file that contains RDF data (in N-Triples format)")

        opt[String]('q', "queries").required().valueName("<path>").
            action((x, c) => c.copy(queries = x)).
            text("path to file that contains SPARQL query list")

        opt[String]('o', "output").required().valueName("<directory>").
            action((x, c) => c.copy(out = x)).
            text("the output directory")

        opt[String]('p', "partitions").required().valueName("<directory>").
            action((x, c) => c.copy(partitions = x)).
            text("the partitions directory")

        help("help").text("prints this usage text")
    }
}
