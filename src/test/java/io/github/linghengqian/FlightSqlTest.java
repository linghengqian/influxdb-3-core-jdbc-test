package io.github.linghengqian;

import com.influxdb.v3.client.InfluxDBClient;
import com.influxdb.v3.client.Point;
import org.apache.arrow.flight.CallHeaders;
import org.apache.arrow.flight.CallStatus;
import org.apache.arrow.flight.FlightClient;
import org.apache.arrow.flight.FlightClientMiddleware;
import org.apache.arrow.flight.FlightClientMiddleware.Factory;
import org.apache.arrow.flight.FlightInfo;
import org.apache.arrow.flight.FlightStream;
import org.apache.arrow.flight.Location;
import org.apache.arrow.flight.Ticket;
import org.apache.arrow.flight.auth2.BearerCredentialWriter;
import org.apache.arrow.flight.grpc.CredentialCallOption;
import org.apache.arrow.flight.sql.FlightSqlClient;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

@SuppressWarnings({"HttpUrlsUsage", "resource"})
@Testcontainers
public class FlightSqlTest {

    @Container
    private final GenericContainer<?> container = new GenericContainer<>("quay.io/influxdb/influxdb3-core:911ba92ab4133e75fe2a420e16ed9cb4cf32196f")
            .withCommand("serve --node-id local01 --object-store file --data-dir /home/influxdb3/.influxdb3")
            .withExposedPorts(8181, 443);

    @Test
    void test() {
        try (InfluxDBClient client = InfluxDBClient.getInstance(
                "http://" + container.getHost() + ":" + container.getMappedPort(8181),
                null,
                "linghengqian_db")) {
            writeData(client);
            queryData();
        } catch (Exception e) {
            System.err.println("An error occurred while connecting to InfluxDB!");
            throw new RuntimeException(e);
        }
    }

    private void writeData(InfluxDBClient client) {
        Point point = Point.measurement("home")
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

    private void queryData() {
        System.out.println("Query InfluxDB with the Java Flight SQL Client");
        Factory f = info -> new FlightClientMiddleware() {
            @Override
            public void onBeforeSendingHeaders(CallHeaders outgoingHeaders) {
                outgoingHeaders.insert("database", "linghengqian_db");
            }

            @Override
            public void onHeadersReceived(CallHeaders incomingHeaders) {
            }

            @Override
            public void onCallCompleted(CallStatus callStatus) {
            }
        };
        Location location = Location.forGrpcInsecure(container.getHost(), container.getMappedPort(8181));
        BufferAllocator allocator = new RootAllocator(Long.MAX_VALUE);
        FlightClient client = FlightClient.builder(allocator, location).intercept(f).build();
        System.out.println("client" + client);
        FlightSqlClient sqlClient = new FlightSqlClient(client);
        System.out.println("sqlClient: " + sqlClient);
        String query = "SELECT * FROM home";
        CredentialCallOption auth = new CredentialCallOption(new BearerCredentialWriter(null));
        FlightInfo flightInfo = sqlClient.execute(query, auth);
        Ticket ticket = flightInfo.getEndpoints().getFirst().getTicket();
        final FlightStream stream = sqlClient.getStream(ticket, auth);
        while (stream.next()) {
            try {
                final VectorSchemaRoot root = stream.getRoot();
                System.out.println(root.contentToTSVString());
            } catch (Exception e) {
                System.out.println("Error executing FlightSqlClient: " + e.getMessage());
            }
        }
        try {
            stream.close();
        } catch (Exception e) {
            System.out.println("Error closing stream: " + e.getMessage());
        }
        try {
            sqlClient.close();
        } catch (Exception e) {
            System.out.println("Error closing client: " + e.getMessage());
        }
    }
}
