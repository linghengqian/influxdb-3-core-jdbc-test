package io.github.linghengqian;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.net.URIBuilder;
import com.influxdb.v3.client.InfluxDBClient;
import com.influxdb.v3.client.Point;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings({"SqlNoDataSourceInspection", "HttpUrlsUsage", "resource"})
@Testcontainers
public class TimeDifferenceTest {

    private final Instant magicTime = Instant.now().minusSeconds(10);

    @Container
    private final GenericContainer<?> container = new GenericContainer<>("influxdb:3.2.1-core")
            .withCommand("influxdb3 serve --node-id local01 --object-store memory")
            .withExposedPorts(8181);

    @Test
    void test() throws Exception {
        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                        .uri(new URI("http://%s:%d/api/v3/configure/token/admin".formatted(container.getHost(), container.getMappedPort(8181))))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json")
                        .build(),
                BodyHandlers.ofString()
        );
        String token = new ObjectMapper().readTree(response.body()).get("token").asText();
        try (InfluxDBClient client = InfluxDBClient.getInstance(
                "http://" + container.getHost() + ":" + container.getMappedPort(8181),
                token.toCharArray(),
                "mydb")) {
            writeData(client);
        }
        queryDataByHttp(token);
        queryDataByJdbcDriver(token);
    }

    private void writeData(InfluxDBClient client) {
        Point point = Point.measurement("home")
                .setTag("location", "London")
                .setField("value", 30.01)
                .setTimestamp(magicTime);
        client.writePoint(point);
    }

    private void queryDataByHttp(String token) throws URISyntaxException, IOException, InterruptedException {
        URI uri = new URIBuilder().setScheme("http")
                .setHost(container.getHost())
                .setPort(container.getMappedPort(8181))
                .setPath("/api/v3/query_sql")
                .setParameter("db", "mydb")
                .setParameter("q", "select time,location,value from home order by time desc limit 10")
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(
                        HttpRequest.newBuilder().uri(uri).header("Authorization", "Bearer " + token).GET().build(),
                        BodyHandlers.ofString()
                );
        assertThat(
                new ObjectMapper().readTree(response.body()).get(0).get("time").asText(),
                is(magicTime.atOffset(ZoneOffset.UTC).toLocalDateTime().toString())
        );
    }

    private void queryDataByJdbcDriver(String token) throws SQLException {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:arrow-flight-sql://" + container.getHost() + ":" + container.getMappedPort(8181) + "/?useEncryption=0&database=mydb" + "&token=" + token);
        try (HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
             Connection connection = hikariDataSource.getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery("select time,location,value from home order by time desc limit 10");
            assertThat(resultSet.next(), is(true));
            assertThat(resultSet.getString("location"), is("London"));
            assertThat(resultSet.getString("value"), is("30.01"));
            assertThat(resultSet.getObject("time", LocalDateTime.class), is(magicTime.atOffset(ZoneOffset.UTC).toLocalDateTime()));
            assertThrows(SQLException.class, () -> resultSet.getObject("time", Instant.class), "See https://github.com/apache/arrow-java/issues/732 .");
        }
    }
}
