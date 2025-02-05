package io.github.linghengqian;

import com.influxdb.v3.client.InfluxDBClient;
import com.influxdb.v3.client.Point;
import com.influxdb.v3.client.PointValues;
import com.influxdb.v3.client.internal.NanosecondConverter;
import com.influxdb.v3.client.write.WritePrecision;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SuppressWarnings({"resource", "HttpUrlsUsage"})
@Testcontainers
class Influxdb3JavaTest {

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
        String sql = "select time,location,value from home order by time desc limit 10";
        try (Stream<Object[]> stream = client.query(sql)) {
            List<Object[]> list = stream.toList();
            assertThat(list.size(), is(1));
            Object[] row = list.getFirst();
            assertThat(row[1], is("London"));
            assertThat(row[2], is(30.01));
            // todo why internal class from `com.influxdb.v3.client.internal.**` ?
            assertThat(row[0], is(NanosecondConverter.convert(magicTime, WritePrecision.NS)));
        }
        try (Stream<PointValues> stream = client.queryPoints(sql)) {
            List<PointValues> list = stream.toList();
            assertThat(list.size(), is(1));
            PointValues p = list.getFirst();
            assertThat(p.getField("value", Double.class), is(30.01));
            assertThat(p.getTag("location"), is("London"));
            // todo why internal class from `com.influxdb.v3.client.internal.**` ?
            assertThat(p.getTimestamp(), is(NanosecondConverter.convert(magicTime, WritePrecision.NS)));
        }
    }
}
