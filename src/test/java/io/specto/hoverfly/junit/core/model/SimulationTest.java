package io.specto.hoverfly.junit.core.model;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Set;

import static io.specto.hoverfly.junit.core.model.FieldMatcher.exactlyMatches;
import static org.assertj.core.api.Assertions.assertThat;

public class SimulationTest {

    
    private ObjectMapper objectMapper = new ObjectMapper();

    private URL v1Resource = Resources.getResource("simulations/v1-simulation.json");
    private URL v2Resource = Resources.getResource("simulations/v2-simulation.json");
    private URL v2ResourceWithUnknownFields = Resources.getResource("simulations/v2-simulation-with-unknown-fields.json");
    private URL v1ResourceWithLooseMatching = Resources.getResource("simulations/v1-simulation-with-loose-matching.json");
    private URL v1ResourceWithRecording = Resources.getResource("simulations/v1-simulation-with-recording.json");

    @Test
    public void shouldDeserializeAndUpgradeV1Simulation() throws Exception {

        // Given
        Simulation expected = getV2Simulation();

        // When
        Simulation actual = objectMapper.readValue(v1Resource, Simulation.class);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldDeserializeV2Simulation() throws Exception {
        // Given
        Simulation expected = getV2Simulation();

        // When
        Simulation actual = objectMapper.readValue(v2Resource, Simulation.class);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldSerializeV2Simulation() throws Exception {

        Simulation simulation = getV2Simulation();

        String actual = objectMapper.writeValueAsString(simulation);

        String expected = Resources.toString(v2Resource, Charset.forName("UTF-8"));
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT);
    }

    @Test
    public void shouldIgnoreUnknownPropertiesWhenDeserialize() throws Exception {
        // Given
        Simulation expected = getV2Simulation();

        // When
        Simulation actual = objectMapper.readValue(v2ResourceWithUnknownFields, Simulation.class);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldBeAbleToConvertV1LooseMatchingToGlobMatcher() throws Exception {

        Simulation actual = objectMapper.readValue(v1ResourceWithLooseMatching, Simulation.class);

        Set<RequestResponsePair> pairs = actual.getHoverflyData().getPairs();

        assertThat(pairs).hasSize(1);

        FieldMatcher path = pairs.iterator().next().getRequest().getPath();
        assertThat(path.getExactMatch()).isNull();
        assertThat(path.getGlobMatch()).isEqualTo("/api/bookings/*");
    }

    @Test
    public void shouldIgnoreHeadersWhenV1SimulationRequestTypeIsRecording() throws Exception {
        Simulation actual = objectMapper.readValue(v1ResourceWithRecording, Simulation.class);

        Set<RequestResponsePair> pairs = actual.getHoverflyData().getPairs();

        assertThat(pairs).hasSize(1);
        Request request = pairs.iterator().next().getRequest();
        assertThat(request.getRequestType()).isEqualTo(Request.RequestType.RECORDING);
        assertThat(request.getHeaders()).isEmpty();

    }


    private Simulation getV2Simulation() {
        HoverflyData data = getTestHoverflyData();
        HoverflyMetaData meta = new HoverflyMetaData();
        return new Simulation(data, meta);
    }


    private HoverflyData getTestHoverflyData() {
        Request request = new Request.Builder()
                .path(exactlyMatches("/api/bookings/1"))
                .method(exactlyMatches("GET"))
                .destination(exactlyMatches("www.my-test.com"))
                .scheme(exactlyMatches("http"))
                .body(exactlyMatches(""))
                .query(exactlyMatches(""))
                .headers(ImmutableMap.of("Content-Type", Lists.newArrayList("text/plain; charset=utf-8")))
                .build();
        Response response = new Response.Builder()
                .status(200)
                .body("{\"bookingId\":\"1\"}")
                .encodedBody(false)
                .headers(ImmutableMap.of("Content-Type", Lists.newArrayList("application/json")))
                .build();
        return new HoverflyData(
                Sets.newHashSet(new RequestResponsePair(request, response)), new GlobalActions(Collections.emptyList()));
    }
}