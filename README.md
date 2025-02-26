# influxdb-3-core-jdbc-test

- For,
  - https://github.com/testcontainers/testcontainers-java/issues/9528
  - https://github.com/InfluxCommunity/influxdb3-java/issues/219
  - https://github.com/InfluxCommunity/influxdb3-java/issues/220
  - https://github.com/InfluxCommunity/influxdb3-java/issues/221
  - https://github.com/InfluxCommunity/influxdb3-java/pull/223
  - https://github.com/InfluxCommunity/influxdb3-java/issues/222
  - https://github.com/InfluxCommunity/influxdb3-java/issues/224
  - https://github.com/influxdata/influxdb/issues/25983
  - https://github.com/apache/arrow-java/issues/636
  - https://github.com/apache/arrow-java/pull/464
  - https://github.com/apache/arrow-java/issues/463
- Verified unit test under Ubuntu 22.04.4 LTS with `SDKMAN!` and `Docker CE`.

```shell
sdk install java 21.0.6-ms
sdk use java 21.0.6-ms

git clone git@github.com:aiguofer/arrow-java.git -b improved_tz_support
cd ./arrow/
mvn clean install -DskipTests -Dspotless.check.skip=true
cd ../

git clone git@github.com:linghengqian/influxdb-3-core-jdbc-test.git
cd ./influxdb-3-core-jdbc-test/
./mvnw -T 1C clean test
```

Can be tested individually.

```shell
sdk install java 21.0.6-ms
sdk use java 21.0.6-ms

git clone git@github.com:aiguofer/arrow-java.git -b improved_tz_support
cd ./arrow/
mvn clean install -DskipTests -Dspotless.check.skip=true
cd ../

git clone git@github.com:linghengqian/influxdb-3-core-jdbc-test.git
cd ./influxdb-3-core-jdbc-test/
sdk use java 21.0.6-ms
./mvnw -T 1C -Dtest=TimeDifferenceTest clean test
```

- The log is as follows.

```shell
$ ./mvnw -T 1C clean test
[INFO] Scanning for projects...
[INFO] 
[INFO] Using the MultiThreadedBuilder implementation with a thread count of 16
[INFO] 
[INFO] ----------< io.github.linghengqian:influxdb-3-core-jdbc-test >----------
[INFO] Building influxdb-3-core-jdbc-test 1.0-SNAPSHOT
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- clean:3.2.0:clean (default-clean) @ influxdb-3-core-jdbc-test ---
[INFO] Deleting /home/linghengqian/TwinklingLiftWorks/git/public/influxdb-3-core-jdbc-test/target
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ influxdb-3-core-jdbc-test ---
[INFO] skip non existing resourceDirectory /home/linghengqian/TwinklingLiftWorks/git/public/influxdb-3-core-jdbc-test/src/main/resources
[INFO] 
[INFO] --- compiler:3.13.0:compile (default-compile) @ influxdb-3-core-jdbc-test ---
[INFO] No sources to compile
[INFO] 
[INFO] --- resources:3.3.1:testResources (default-testResources) @ influxdb-3-core-jdbc-test ---
[INFO] skip non existing resourceDirectory /home/linghengqian/TwinklingLiftWorks/git/public/influxdb-3-core-jdbc-test/src/test/resources
[INFO] 
[INFO] --- compiler:3.13.0:testCompile (default-testCompile) @ influxdb-3-core-jdbc-test ---
[INFO] Recompiling the module because of changed source code.
[INFO] Compiling 7 source files with javac [debug target 21] to target/test-classes
[INFO] 
[INFO] --- surefire:3.5.2:test (default-test) @ influxdb-3-core-jdbc-test ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running io.github.linghengqian.FlightSqlDriverTest
SLF4J(W): No SLF4J providers were found.
SLF4J(W): Defaulting to no-operation (NOP) logger implementation
SLF4J(W): See https://www.slf4j.org/codes.html#noProviders for further details.
2月 26, 2025 3:24:25 下午 org.apache.arrow.driver.jdbc.shaded.org.apache.arrow.memory.BaseAllocator <clinit>
信息: Debug mode disabled. Enable with the VM option -Darrow.memory.debug.allocator=true.
2月 26, 2025 3:24:25 下午 org.apache.arrow.driver.jdbc.shaded.org.apache.arrow.memory.DefaultAllocationManagerOption getDefaultAllocationManagerFactory
信息: allocation manager type not specified, using netty as the default type
2月 26, 2025 3:24:25 下午 org.apache.arrow.driver.jdbc.shaded.org.apache.arrow.memory.CheckAllocator reportResult
信息: Using DefaultAllocationManager at memory/netty/DefaultAllocationManagerFactory.class
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.526 s -- in io.github.linghengqian.FlightSqlDriverTest
[INFO] Running io.github.linghengqian.FlightSqlTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.088 s -- in io.github.linghengqian.FlightSqlTest
[INFO] Running io.github.linghengqian.influxdb3java.PointValuesTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.822 s -- in io.github.linghengqian.influxdb3java.PointValuesTest
[INFO] Running io.github.linghengqian.influxdb3java.InfluxQlTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0 s -- in io.github.linghengqian.influxdb3java.InfluxQlTest
[INFO] Running io.github.linghengqian.influxdb3java.SqlTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.796 s -- in io.github.linghengqian.influxdb3java.SqlTest
[INFO] Running io.github.linghengqian.influxdb3java.SqlParamsTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.977 s -- in io.github.linghengqian.influxdb3java.SqlParamsTest
[INFO] Running io.github.linghengqian.TimeDifferenceTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.025 s -- in io.github.linghengqian.TimeDifferenceTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  15.876 s (Wall Clock)
[INFO] Finished at: 2025-02-26T15:24:35+08:00
[INFO] ------------------------------------------------------------------------
```
