package sample


object Main extends LogHelper {
  def main(args: Array[String]): Unit = {
    val log4jConfigFileOpt = if (args.contains("--reconfigure")) Some("log4j2-console-json.properties") else None
    val addConsoleAppenderProgrammatically = args.contains("--add-appender")

    configureLog4jProperties(log4jConfigFileOpt, addConsoleAppenderProgrammatically)
    LogHelper.printAppenders()

    val bar = 42
    logger.info(s"Foo: $bar")
    println("Done.")
  }
}