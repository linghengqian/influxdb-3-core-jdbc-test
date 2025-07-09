package io.github.linghengqian.influxdb3java;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.influxdb.v3.client.InfluxDBClient;
import com.influxdb.v3.client.Point;
import com.influxdb.v3.client.PointValues;
import com.influxdb.v3.client.internal.NanosecondConverter;
import com.influxdb.v3.client.query.QueryOptions;
import com.influxdb.v3.client.write.WritePrecision;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SuppressWarnings({"resource", "HttpUrlsUsage"})
@Testcontainers
public class PointValuesTest {
    private final Instant magicTime = Instant.now().minusSeconds(10);

    @Container
    private final GenericContainer<?> container = new GenericContainer<>("influxdb:3.2.1-core")
            .withCommand("influxdb3 serve --node-id local01 --object-store file --data-dir /home/influxdb3/.influxdb3")
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
            queryData(client);
        }
    }

    private void writeData(InfluxDBClient client) {
        Point point = Point.measurement("home")
                .setTag("location", "London")
                .setField("value", 30.01)
                .setTimestamp(magicTime);
        client.writePoint(point);
    }

    private void queryData(InfluxDBClient client) {
        try (Stream<PointValues> stream = client.queryPoints("select time,location,value from home order by time desc limit 10",
                QueryOptions.defaultInfluxQlQueryOptions())) {
            List<PointValues> list = stream.toList();
            assertThat(list.size(), is(1));
            PointValues p = list.getFirst();
            assertThat(p.getField("value", Double.class), is(30.01));
            assertThat(p.getTag("location"), is("London"));
            assertThat(p.getTimestamp(), is(NanosecondConverter.convert(magicTime, WritePrecision.NS)));
        }
    }
}
