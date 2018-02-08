package io.specto.hoverfly.junit.core.model;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
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

    private URL v2Resource = Resources.getResource("simulations/v2-simulation.json");
    private URL v3Resource = Resources.getResource("simulations/v3-simulation.json");
    private URL v4Resource = Resources.getResource("simulations/v4-simulation.json");
    private URL v2ResourceWithUnknownFields = Resources.getResource("simulations/v2-simulation-with-unknown-fields.json");
    private URL v2ResourceWithLooseMatching = Resources.getResource("simulations/v2-simulation-with-loose-matching.json");
    private URL v2ResourceWithRecording = Resources.getResource("simulations/v2-simulation-with-recording.json");

    @Test
    public void shouldDeserializeAndUpgradeV2ToV3Simulation() throws Exception {

        // Given
        Simulation expected = getV3Simulation();

        // When
        Simulation actual = objectMapper.readValue(v2Resource, Simulation.class);

        // Then
        assertThat(actual.getHoverflyData()).isEqualTo(expected.getHoverflyData());
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
    public void shouldSerializeV4Simulation() throws Exception {
        // given
        Simulation simulation = getLatestSimulation();

        // when
        String actual = objectMapper.writeValueAsString(simulation);

        // then
        String expected = Resources.toString(v4Resource, Charset.forName("UTF-8"));
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT);
    }

    @Test
    public void shouldSerializeV3Simulation() throws Exception {
        // given
        Simulation simulation = getV3Simulation();

        // when
        String actual = objectMapper.writeValueAsString(simulation);

        // then
        String expected = Resources.toString(v3Resource, Charset.forName("UTF-8"));
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
    public void shouldBeAbleToConvertV2LooseMatchingToGlobMatcher() throws Exception {

        Simulation actual = objectMapper.readValue(v2ResourceWithLooseMatching, Simulation.class);

        Set<RequestResponsePair> pairs = actual.getHoverflyData().getPairs();

        assertThat(pairs).hasSize(1);

        FieldMatcher path = pairs.iterator().next().getRequest().getPath();
        assertThat(path.getExactMatch()).isNull();
        assertThat(path.getGlobMatch()).isEqualTo("/api/bookings/*");
    }

    @Test
    public void shouldIgnoreHeadersWhenV1SimulationRequestTypeIsRecording() throws Exception {
        Simulation actual = objectMapper.readValue(v2ResourceWithRecording, Simulation.class);

        Set<RequestResponsePair> pairs = actual.getHoverflyData().getPairs();

        assertThat(pairs).hasSize(1);
        Request request = pairs.iterator().next().getRequest();
        assertThat(request.getRequestType()).isEqualTo(Request.RequestType.RECORDING);
        assertThat(request.getHeaders()).isEmpty();

    }

    private Simulation getLatestSimulation() {
        Request.Builder requestBuilder = getTestRequestBuilder()
            .requiresState(ImmutableMap.of("requiresStateKey", "requiresStateValue"));
        Response.Builder responseBuilder = getTestResponseBuilder()
            .transitionsState(ImmutableMap.of("transitionsStateKey", "transitionsStateValue"))
            .removesState(ImmutableList.of("removesStateKey"));
        HoverflyData data = getTestHoverflyData(requestBuilder, responseBuilder);
        HoverflyMetaData meta = new HoverflyMetaData();
        return new Simulation(data, meta);
    }

    private Simulation getV3Simulation() {
        HoverflyData data = getTestHoverflyData(getTestRequestBuilder(), getTestResponseBuilder());
        HoverflyMetaData meta = new HoverflyMetaData("v3");
        return new Simulation(data, meta);
    }

    private Simulation getV2Simulation() {
        HoverflyData data = getTestHoverflyData(getTestRequestBuilder(), getTestResponseBuilder());
        HoverflyMetaData meta = new HoverflyMetaData("v2");
        return new Simulation(data, meta);
    }

    private Request.Builder getTestRequestBuilder() {
        return new Request.Builder()
            .path(exactlyMatches("/api/bookings/1"))
            .method(exactlyMatches("GET"))
            .destination(exactlyMatches("www.my-test.com"))
            .scheme(exactlyMatches("http"))
            .body(exactlyMatches(""))
            .query(exactlyMatches(""))
            .headers(ImmutableMap.of("Content-Type", Lists.newArrayList("text/plain; charset=utf-8")));
    }

    private Response.Builder getTestResponseBuilder() {
        return new Response.Builder()
            .status(200)
            .body("{\"bookingId\":\"1\"}")
            .encodedBody(false)
            .headers(ImmutableMap.of("Content-Type", Lists.newArrayList("application/json")));
    }

    private HoverflyData getTestHoverflyData(Request.Builder testRequestBuilder, Response.Builder testResponseBuilder) {
        return new HoverflyData(
            Sets.newHashSet(new RequestResponsePair(testRequestBuilder.build(), testResponseBuilder.build())),
            new GlobalActions(Collections.emptyList()));
    }
}