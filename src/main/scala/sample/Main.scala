package sample

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession


object Main extends LogHelper {
  def main(args: Array[String]): Unit = {
    val log4jConfigFileOpt = if (args.contains("--reconfigure")) Some("log4j2-console-json.properties") else None
    val addConsoleAppenderProgrammatically = args.contains("--add-appender")

    implicit val spark = createSparkSession()

    configureLog4jProperties(log4jConfigFileOpt, addConsoleAppenderProgrammatically)
    LogHelper.printAppenders()

    val bar = 42
    logger.info(s"Foo: $bar")
    println("Done.")
  }

  def createSparkSession(): SparkSession = {
    val sparkConf = new SparkConf()

    SparkSession
      .builder()
      .config(sparkConf
        .setAppName("Bar")
        .set("spark.master", "local[*]")
        .set("spark.driver.extraClassPath", "jars/log4j-layout-template-json-2.20.0.jar"))
      .getOrCreate()
  }
}
