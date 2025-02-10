package io.github.linghengqian;

import com.influxdb.v3.client.InfluxDBClient;
import com.influxdb.v3.client.Point;
import com.influxdb.v3.client.PointValues;
import com.influxdb.v3.client.internal.NanosecondConverter;
import com.influxdb.v3.client.query.QueryOptions;
import com.influxdb.v3.client.write.WritePrecision;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@SuppressWarnings({"SqlNoDataSourceInspection", "HttpUrlsUsage", "resource"})
@Testcontainers
public class TimeDifferenceTest {

    private final Instant magicTime = Instant.now().minusSeconds(10);

    @Container
    private final GenericContainer<?> container = new GenericContainer<>("quay.io/influxdb/influxdb3-core:911ba92ab4133e75fe2a420e16ed9cb4cf32196f")
            .withCommand("serve --node-id local01 --object-store file --data-dir /home/influxdb3/.influxdb3 -v")
            .withExposedPorts(8181);

    @Test
    void test() throws Exception {
        try (InfluxDBClient client = InfluxDBClient.getInstance(
                "http://" + container.getHost() + ":" + container.getMappedPort(8181),
                null,
                "mydb")) {
            writeData(client);
            queryDataByInfluxDbClient(client);
            queryDataByJdbcDriver();
        }
    }

    private void writeData(InfluxDBClient client) {
        Point point = Point.measurement("home")
                .setTag("location", "London")
                .setField("value", 30.01)
                .setTimestamp(magicTime);
        client.writePoint(point);
    }

    private void queryDataByInfluxDbClient(InfluxDBClient client) {
        try (Stream<PointValues> stream = client.queryPoints("select time,location,value from home order by time desc limit 10",
                QueryOptions.DEFAULTS)) {
            List<PointValues> list = stream.toList();
            assertThat(list.size(), is(1));
            PointValues p = list.getFirst();
            assertThat(p.getField("value", Double.class), is(30.01));
            assertThat(p.getTag("location"), is("London"));
            assertThat(p.getTimestamp(), is(NanosecondConverter.convert(magicTime, WritePrecision.NS)));
        }
    }

    private void queryDataByJdbcDriver() throws SQLException {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:arrow-flight-sql://" + container.getHost() + ":" + container.getMappedPort(8181) + "/?useEncryption=0&database=mydb");
        try (HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
             Connection connection = hikariDataSource.getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery("select time,location,value from home order by time desc limit 10");
            assertThat(resultSet.next(), is(true));
            assertThat(resultSet.getString("location"), is("London"));
            assertThat(resultSet.getString("value"), is("30.01"));
            assertThat(Timestamp.from(magicTime).getTime(), is(magicTime.toEpochMilli()));
            assertThat(resultSet.getTimestamp("time"), notNullValue());
            System.out.println(container.getLogs());
            // todo linghengqian why fail?
            assertThat(resultSet.getTimestamp("time").getTime(), is(magicTime.toEpochMilli()));
        }
    }
}
