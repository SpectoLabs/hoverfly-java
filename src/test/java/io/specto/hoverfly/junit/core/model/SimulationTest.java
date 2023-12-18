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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import static io.specto.hoverfly.junit.core.model.RequestFieldMatcher.newExactMatcher;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class SimulationTest {

    
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final URL v5Resource = Resources.getResource("simulations/v5-simulation.json");
    private final URL v5ResourceWithoutGlobalActions = Resources.getResource("simulations/v5-simulation-without-global-actions.json");
    private final URL v5ResourceWithUnknownFields = Resources.getResource("simulations/v5-simulation-with-unknown-fields.json");
    private final URL v5ResourceWithMixedCaseMatcherType = Resources.getResource("simulations/v5-simulation-with-mixed-case-matcher-type.json");
    private final URL v5_2Resource = Resources.getResource("simulations/v5_2-simulation.json");


    @Test
    public void shouldDeserialize() throws Exception {
        // Given
        Simulation expected = getLatestSimulation();

        // When
        Simulation actual = objectMapper.readValue(v5Resource, Simulation.class);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldSerialize() throws Exception {
        // given
        Simulation simulation = getLatestSimulation();

        // when
        String actual = objectMapper.writeValueAsString(simulation);

        // then
        String expected = Resources.toString(v5Resource, StandardCharsets.UTF_8);
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT);
    }

    @Test
    public void shouldDeserializeWithV5_2Matchers() throws Exception {
        // Given
        Simulation expected = getSimulationWithV5_2Matchers();

        // When
        Simulation actual = objectMapper.readValue(v5_2Resource, Simulation.class);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldSerializeWithV5_2Matchers() throws Exception {
        // Given
        Simulation simulation = getSimulationWithV5_2Matchers();

        // when
        String actual = objectMapper.writeValueAsString(simulation);

        // then
        String expected = Resources.toString(v5_2Resource, StandardCharsets.UTF_8);
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT);
    }

    @Test
    public void shouldIgnoreUnknownPropertiesWhenDeserialize() throws Exception {
        // Given
        Simulation expected = getLatestSimulation();

        // When
        Simulation actual = objectMapper.readValue(v5ResourceWithUnknownFields, Simulation.class);

        // Then
        assertThat(actual).isEqualTo(expected);
    }


    @Test
    public void deserializingMatcherTypeShouldBeCaseInsensitive() throws Exception {
        // Given
        Simulation expected = getLatestSimulation();

        // When
        Simulation actual = objectMapper.readValue(v5ResourceWithMixedCaseMatcherType, Simulation.class);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldNotIncludeNullGlobalActionsFieldWhenSerialize() throws Exception{
        String expected = Resources.toString(v5ResourceWithoutGlobalActions, StandardCharsets.UTF_8);

        Simulation simulation = objectMapper.readValue(expected, Simulation.class);

        String actual = objectMapper.writeValueAsString(simulation);

        JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT);
    }

    private Simulation getSimulationWithV5_2Matchers() {
        Request.Builder requestBuilder = getTestRequestBuilder()
                // Array Matcher
                .query(ImmutableMap.of("key", singletonList(RequestFieldMatcher.newArrayMatcher(Arrays.asList("value1", "value2")))))
                // JWT Matcher
                .headers(ImmutableMap.of("Authorization", singletonList(RequestFieldMatcher.newJwtMatcher("{\"header\":{\"alg\":\"HS256\"},\"payload\":{\"sub\":\"1234567890\",\"name\":\"John Doe\"}}"))))
                .deprecatedQuery(singletonList(newExactMatcher("")))
                .requiresState(ImmutableMap.of("requiresStateKey", "requiresStateValue"));
        Response.Builder responseBuilder = getTestResponseBuilder()
                .transitionsState(ImmutableMap.of("transitionsStateKey", "transitionsStateValue"))
                .removesState(ImmutableList.of("removesStateKey"))
                .fixedDelay(3000);
        HoverflyData data = getTestHoverflyData(requestBuilder, responseBuilder);
        HoverflyMetaData meta = new HoverflyMetaData();
        Simulation simulation = new Simulation(data, meta);
        return simulation;
    }

    private Simulation getLatestSimulation() {
        Request.Builder requestBuilder = getTestRequestBuilder()
                .deprecatedQuery(singletonList(newExactMatcher("")))
                .requiresState(ImmutableMap.of("requiresStateKey", "requiresStateValue"));
        Response.Builder responseBuilder = getTestResponseBuilder()
                .transitionsState(ImmutableMap.of("transitionsStateKey", "transitionsStateValue"))
                .removesState(ImmutableList.of("removesStateKey"))
                .fixedDelay(3000);
        HoverflyData data = getTestHoverflyData(requestBuilder, responseBuilder);
        HoverflyMetaData meta = new HoverflyMetaData();
        return new Simulation(data, meta);
    }


    private Request.Builder getTestRequestBuilder() {
        return new Request.Builder()
            .path(singletonList(newExactMatcher("/api/bookings/1")))
            .method(singletonList(newExactMatcher("GET")))
            .destination(singletonList(newExactMatcher("www.my-test.com")))
            .scheme(singletonList(newExactMatcher("http")))
            .body(singletonList(newExactMatcher("")))
            .query(ImmutableMap.of("key",  singletonList(newExactMatcher("value"))))
            .headers(ImmutableMap.of("Content-Type", singletonList(newExactMatcher("text/plain; charset=utf-8"))));
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
