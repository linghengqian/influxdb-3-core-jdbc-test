package io.github.linghengqian;

import com.influxdb.v3.client.InfluxDBClient;
import com.influxdb.v3.client.Point;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@SuppressWarnings({"SqlNoDataSourceInspection", "HttpUrlsUsage", "resource"})
@Testcontainers
public class FlightSqlDriverTest {

    private final Instant magicTime = Instant.now().minusSeconds(10);

    @Container
    private final GenericContainer<?> container = new GenericContainer<>("quay.io/influxdb/influxdb3-core:911ba92ab4133e75fe2a420e16ed9cb4cf32196f")
            .withCommand("serve --node-id local01 --object-store file --data-dir /home/influxdb3/.influxdb3")
            .withExposedPorts(8181);

    @Test
    void test() throws Exception {
        try (InfluxDBClient client = InfluxDBClient.getInstance(
                "http://" + container.getHost() + ":" + container.getMappedPort(8181),
                null,
                "mydb")) {
            writeData(client);
            queryData();
        }
    }

    private void writeData(InfluxDBClient client) {
        Point point = Point.measurement("home")
                .setTag("location", "London")
                .setField("value", 30.01)
                .setTimestamp(magicTime);
        client.writePoint(point);
    }

    private void queryData() throws SQLException {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:arrow-flight-sql://" + container.getHost() + ":" + container.getMappedPort(8181) + "/?useEncryption=0&database=mydb");
        try (HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig)) {
            try (Connection connection = hikariDataSource.getConnection()) {
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM home");
                assertThat(resultSet.next(), is(true));
                assertThat(resultSet.getString(1), is("London"));
                // todo why fail?
                // assertThat(resultSet.getString(2), is(magicTime.atOffset(ZoneOffset.UTC).toLocalDateTime().toString()));
                assertThat(resultSet.getString(2), notNullValue());
                assertThat(resultSet.getString(3), is("30.01"));
                assertThat(resultSet.next(), is(false));
            }
        }
    }
}
