# Sample Spark + Log4j2 Configuration Issue Project
This project aims at demonstrating issues we face while trying to re-configure log4j2 from a properties file containing plug-ins present in a shaded or fat jar.

We demonstrate that:
* When running with `java` directly, log4j2 can be reconfigured without issue. We can also add an appender programmatically using a plug-in present in the fat jar.
* When running with `spark-submit`, Spark can't find the plug-in in the fat jar, so it fails to reconfigure log4j.
* When Spark is manually provided with the plug-in jar in its driver classpath, then it works.

However, we shouldn't need to provide Spark with Log4j plug-ins, as it complicates deploying our Spark applications.
Moreover, with older Spark versions and Log4j1, we could configure appenders coming from plug-ins this way.

To make sure the issue does not come from Maven Shade plug-in only (that requires a transformer to fix log4j2 plug-ins),
we also provide the same example using Spring Boot.

## Example with Spring Boot

First, package the shaded jar:
```commandline:
mvn clean package -Pspring
```

When running with `java -jar` directly, the appender can be added, both by re-configuring log4j with a properties file
present in resources, or by running code that programmatically adds the appender.
```commandline:
java -jar target/sample_2.12-1.0.0.jar 
java -jar target/sample_2.12-1.0.0.jar --reconfigure
java -jar target/sample_2.12-1.0.0.jar --add-appender
```

When running with `spark-submit`, both commands will fail.
```commandline:
spark-submit target/sample_2.12-1.0.0.jar 
spark-submit target/sample_2.12-1.0.0.jar --reconfigure
spark-submit target/sample_2.12-1.0.0.jar --add-appender
```


## Example with Maven Shade

First, package the shaded jar:
```commandline:
mvn clean package -Pshade
```

When running with `java -cp` directly, the appender can be added, both by re-configuring log4j with a properties file
present in resources, or by running code that programmatically adds the appender.
```commandline:
java -cp target/sample_2.12-1.0.0-shaded.jar sample.Main
java -cp target/sample_2.12-1.0.0-shaded.jar sample.Main --reconfigure
java -cp target/sample_2.12-1.0.0-shaded.jar sample.Main --add-appender
```

When running with `spark-submit`, both commands will fail.
```commandline:
spark-submit --class sample.Main target/sample_2.12-1.0.0-shaded.jar
spark-submit --class sample.Main target/sample_2.12-1.0.0-shaded.jar --reconfigure
spark-submit --class sample.Main target/sample_2.12-1.0.0-shaded.jar --add-appender
```

## Workaround: provide plug-in jar(s) to the Spark cluster and add them to the driver classpath
There is a workaround that we'd prefer avoiding in practice, as different packages may use different
log4j2 plug-ins (redis, sentry, etc.), and it would force us to have a deployment strategy that includes
pushing those jars with the right version(s) in our Spark clusters that currently are independent of the
applications running on it.

Here, we show the example using Maven Shade only.

First, get all log4j2 plug-in jars into a `jars` folder (in this example there is only one, but in practice we'd have more)
```
log4j-layout-template-json-2.19.0.jar
```

Then, run:
```commandline:
spark-submit \
  --driver-class-path $(ls -xm jars/* | tr -d "[:space:]" | tr -s ',' ':') \
  --class sample.Main \
  target/sample_2.12-1.0.0-shaded.jar \
  --reconfigure
```

## Appendix: it's not only Spark's classpath that is the culprit
```commandline:
java \
  -cp "$(ls -xm $SPARK_HOME/jars/* | tr -d "[:space:]" | tr -s ',' ':'):target/sample_2.12-1.0.0-shaded.jar" \
  sample.Main \
  --reconfigure
```