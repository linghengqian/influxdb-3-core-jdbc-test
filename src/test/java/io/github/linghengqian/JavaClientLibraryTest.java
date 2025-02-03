package io.github.linghengqian;

import com.influxdb.v3.client.InfluxDBClient;
import com.influxdb.v3.client.Point;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.stream.Stream;

@SuppressWarnings("resource")
@Testcontainers
class JavaClientLibraryTest {

    @Container
    private final GenericContainer<?> container = new GenericContainer<>("quay.io/influxdb/influxdb3-core:911ba92ab4133e75fe2a420e16ed9cb4cf32196f")
            .withCommand("serve --node-id local01 --object-store file --data-dir /home/influxdb3/.influxdb3")
            .withExposedPorts(8181);

    @Test
    void test() {
        try (InfluxDBClient client = InfluxDBClient.getInstance(
                "http://" + container.getHost() + ":" + container.getMappedPort(8181),
                null,
                "custom_db")) {
            writeData(client);
            queryData(client);
        } catch (Exception e) {
            System.err.println("An error occurred while connecting to InfluxDB!");
            throw new RuntimeException(e);
        }
    }

    private void writeData(InfluxDBClient client) {
        Point point = Point.measurement("temperature")
                .setTag("location", "London")
                .setField("value", 30.01)
                .setTimestamp(Instant.now().minusSeconds(10));
        try {
            client.writePoint(point);
            System.out.println("Data is written to the bucket.");
        } catch (Exception e) {
            System.err.println("Failed to write data to the bucket.");
            throw new RuntimeException(e);
        }
    }

    private void queryData(InfluxDBClient client) {
        System.out.printf("--------------------------------------------------------%n");
        System.out.printf("| %-8s | %-8s | %-30s |%n", "location", "value", "time");
        System.out.printf("--------------------------------------------------------%n");
        String sql = "select time,location,value from temperature order by time desc limit 10";
        try (Stream<Object[]> stream = client.query(sql)) {
            stream.forEach(row -> System.out.printf("| %-8s | %-8s | %-30s |%n", row[1], row[2], row[0]));
        } catch (Exception e) {
            System.err.println("Failed to query data from the bucket.");
            throw new RuntimeException(e);
        }
    }
}
