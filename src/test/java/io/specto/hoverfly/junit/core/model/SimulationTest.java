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

import static io.specto.hoverfly.junit.core.model.FieldMatcher.fromExactMatchString;
import static org.assertj.core.api.Assertions.assertThat;

public class SimulationTest {

    
    private ObjectMapper objectMapper = new ObjectMapper();

    private URL v1Resource = Resources.getResource("simulations/v1-simulation.json");
    private URL v2Resource = Resources.getResource("simulations/v2-simulation.json");
    private URL v2ResourceWithUnknownFields = Resources.getResource("simulations/v2-simulation-with-unknown-fields.json");

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


    private Simulation getV2Simulation() {
        HoverflyData data = getTestHoverflyData();
        HoverflyMetaData meta = new HoverflyMetaData();
        return new Simulation(data, meta);
    }


    private HoverflyData getTestHoverflyData() {
        RequestMatcher request = new RequestMatcher.Builder()
                .path(fromExactMatchString("/api/bookings/1"))
                .method(fromExactMatchString("GET"))
                .destination(fromExactMatchString("www.my-test.com"))
                .scheme(fromExactMatchString("http"))
                .body(fromExactMatchString(""))
                .query(fromExactMatchString(""))
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