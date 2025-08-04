package io.github.linghengqian;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SuppressWarnings({"HttpUrlsUsage", "resource"})
@Testcontainers
public class FlightSqlTest {

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
                HttpResponse.BodyHandlers.ofString()
        );
        String token = new ObjectMapper().readTree(response.body()).get("token").asText();
        try (InfluxDBClient client = InfluxDBClient.getInstance(
                "http://" + container.getHost() + ":" + container.getMappedPort(8181),
                token.toCharArray(),
                "mydb")) {
            writeData(client);
        }
        queryData(token);
    }

    private void writeData(InfluxDBClient client) {
        Point point = Point.measurement("home")
                .setTag("location", "London")
                .setField("value", 30.01)
                .setTimestamp(magicTime);
        client.writePoint(point);
    }

    private void queryData(String token) throws Exception {
        Factory f = info -> new FlightClientMiddleware() {
            @Override
            public void onBeforeSendingHeaders(CallHeaders outgoingHeaders) {
                outgoingHeaders.insert("database", "mydb");
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
        FlightSqlClient sqlClient = new FlightSqlClient(client);
        String query = "SELECT * FROM home";
        CredentialCallOption auth = new CredentialCallOption(new BearerCredentialWriter(token));
        FlightInfo flightInfo = sqlClient.execute(query, auth);
        Ticket ticket = flightInfo.getEndpoints().getFirst().getTicket();
        final FlightStream stream = sqlClient.getStream(ticket, auth);
        assertThat(stream.next(), is(true));
        final VectorSchemaRoot root = stream.getRoot();
        // todo linghengqian why LocalDateTime? See https://github.com/influxdata/influxdb/issues/26581 .
        assertThat(root.contentToTSVString(), is("""
                location	time	value
                London	%s	30.01
                """.formatted(magicTime.atOffset(ZoneOffset.UTC).toLocalDateTime())));
        assertThat(stream.next(), is(false));
        stream.close();
        sqlClient.close();
    }
}
