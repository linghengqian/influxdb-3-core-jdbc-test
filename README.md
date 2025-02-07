# influxdb-3-core-jdbc-test

- Verified unit test under Ubuntu 22.04.4 LTS with `SDKMAN!` and `Docker CE`.

```shell
sdk install java 21.0.6-ms

git clone git@github.com:linghengqian/influxdb-3-core-jdbc-test.git
cd ./influxdb-3-core-jdbc-test/
sdk use java 21.0.6-ms
./mvnw -T 1C clean test
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
[INFO] Compiling 6 source files with javac [debug target 21] to target/test-classes
[INFO] 
[INFO] --- surefire:3.5.2:test (default-test) @ influxdb-3-core-jdbc-test ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running io.github.linghengqian.FlightSqlTest
SLF4J(W): No SLF4J providers were found.
SLF4J(W): Defaulting to no-operation (NOP) logger implementation
SLF4J(W): See https://www.slf4j.org/codes.html#noProviders for further details.
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.645 s -- in io.github.linghengqian.FlightSqlTest
[INFO] Running io.github.linghengqian.influxdb3java.SqlParamsTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.981 s -- in io.github.linghengqian.influxdb3java.SqlParamsTest
[INFO] Running io.github.linghengqian.influxdb3java.InfluxQlTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.970 s -- in io.github.linghengqian.influxdb3java.InfluxQlTest
[INFO] Running io.github.linghengqian.influxdb3java.SqlTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.042 s -- in io.github.linghengqian.influxdb3java.SqlTest
[INFO] Running io.github.linghengqian.influxdb3java.PointValuesTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.013 s -- in io.github.linghengqian.influxdb3java.PointValuesTest
[INFO] Running io.github.linghengqian.FlightSqlDriverTest
2月 07, 2025 2:18:19 下午 org.apache.arrow.driver.jdbc.shaded.org.apache.arrow.memory.BaseAllocator <clinit>
信息: Debug mode disabled. Enable with the VM option -Darrow.memory.debug.allocator=true.
2月 07, 2025 2:18:19 下午 org.apache.arrow.driver.jdbc.shaded.org.apache.arrow.memory.DefaultAllocationManagerOption getDefaultAllocationManagerFactory
信息: allocation manager type not specified, using netty as the default type
2月 07, 2025 2:18:19 下午 org.apache.arrow.driver.jdbc.shaded.org.apache.arrow.memory.CheckAllocator reportResult
信息: Using DefaultAllocationManager at memory/netty/DefaultAllocationManagerFactory.class
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.919 s -- in io.github.linghengqian.FlightSqlDriverTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  18.330 s (Wall Clock)
[INFO] Finished at: 2025-02-07T14:18:21+08:00
[INFO] ------------------------------------------------------------------------
```
