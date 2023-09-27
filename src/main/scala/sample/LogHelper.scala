package sample

import org.apache.logging.log4j.{Level, LogManager}
import org.apache.logging.log4j.core.appender.ConsoleAppender
import org.apache.logging.log4j.core.{Logger, LoggerContext}
import org.apache.logging.log4j.layout.template.json.JsonTemplateLayout
import org.apache.logging.log4j.layout.template.json.JsonTemplateLayout.EventTemplateAdditionalField

import java.net.URI
import scala.collection.JavaConverters.mapAsScalaMapConverter
import scala.util.Try

trait LogHelper {
  import LogHelper._

  @transient lazy implicit val logger: Logger = LogHelper.getSampleLogger

  def configureLog4jProperties(configFileNameOpt: Option[String], addConsoleAppenderProgrammatically: Boolean): Unit = {
    configFileNameOpt.foreach { configFileName =>
      reconfigureLog4j(configFileName)
    }

    if (addConsoleAppenderProgrammatically) {
      addConsoleJsonAppender()
    }

    getSampleLogger.setLevel(Level.INFO)
  }
}

object LogHelper {
  val LOGGER_PREFIX = "sample"

  def reconfigureLog4j(configurationLocation: String): Unit = {
    println(s"Reconfiguring log4j2 with $configurationLocation...")
    val context = LoggerContext.getContext(false)
    context.setConfigLocation(new URI(s"classpath:$configurationLocation"))
  }

  private[LogHelper] def addConsoleJsonAppender(): Unit = {
    println("Adding a console appender programmatically...")
    val layoutBuilder = JsonTemplateLayout.newBuilder()
    layoutBuilder.setEventTemplateUri("classpath:LogstashJsonEventLayoutV1.json")
    layoutBuilder.setEventTemplateAdditionalFields(Array(
      EventTemplateAdditionalField.newBuilder().setKey("foo").setValue("bar").build()
    ))

    layoutBuilder.setConfiguration(getSampleLogger.getContext.getConfiguration)
    val layout = layoutBuilder.build()

    val appender = ConsoleAppender.createAppender(layout, null, ConsoleAppender.Target.SYSTEM_OUT, "consoleJsonProgrammatically", false, false, false)
    appender.start()

    getSampleLogger.addAppender(appender)
  }

  private[LogHelper] def getSampleLogger: Logger = {
    LogManager.getLogger(LogHelper.LOGGER_PREFIX).asInstanceOf[Logger]
  }

  def printAppenders(): Unit = {
    println(s"Appenders: ${Try(getSampleLogger).map(_.getAppenders.asScala).getOrElse(Map.empty).keys}")
  }
}
