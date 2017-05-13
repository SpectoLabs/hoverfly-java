package io.specto.hoverfly.junit.dsl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import io.specto.hoverfly.junit.core.model.RequestResponsePair;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.Test;

import java.util.Set;

import static io.specto.hoverfly.assertions.Assertions.assertThat;
import static io.specto.hoverfly.assertions.Header.header;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.HttpBodyConverter.json;
import static io.specto.hoverfly.junit.dsl.ResponseBuilder.response;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class StubServiceBuilderTest {

    @Test
    public void shouldExtractHttpsUrlScheme() throws Exception {

        final Set<RequestResponsePair> pairs = service("https://www.my-test.com").get("/").willReturn(response()).getRequestResponsePairs();

        assertThat(pairs).hasSize(1);
        RequestResponsePair pair = pairs.iterator().next();
        assertThat(pair.getRequest().getDestination().getExactMatch()).isEqualTo("www.my-test.com");
        assertThat(pair.getRequest().getScheme().getExactMatch()).isEqualTo("https");
    }

    @Test
    public void shouldDefaultToHttpScheme() throws Exception {
        final Set<RequestResponsePair> pairs = service("www.my-test.com").get("/").willReturn(response()).getRequestResponsePairs();

        assertThat(pairs).hasSize(1);
        RequestResponsePair pair = pairs.iterator().next();
        assertThat(pair.getRequest().getDestination().getExactMatch()).isEqualTo("www.my-test.com");
        assertThat(pair.getRequest().getScheme().getExactMatch()).isEqualTo("http");

    }

    @Test
    public void shouldExtractHttpScheme() throws Exception {
        final Set<RequestResponsePair> pairs = service("http://www.my-test.com").get("/").willReturn(response()).getRequestResponsePairs();

        assertThat(pairs).hasSize(1);
        RequestResponsePair pair = pairs.iterator().next();
        assertThat(pair.getRequest().getDestination().getExactMatch()).isEqualTo("www.my-test.com");
        assertThat(pair.getRequest().getScheme().getExactMatch()).isEqualTo("http");
    }

    @Test
    public void shouldBuildGetRequest() {
        // Given

        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/").willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        assertThat(Iterables.getLast(pairs).getRequest().getMethod().getExactMatch()).isEqualTo("GET");
    }

    @Test
    public void shouldBuildPost() {
        // Given

        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").post("/").willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        assertThat(Iterables.getLast(pairs).getRequest().getMethod().getExactMatch()).isEqualTo("POST");
    }

    @Test
    public void shouldBuildPutRequest() {
        // Given

        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").put("/").willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        assertThat(Iterables.getLast(pairs).getRequest().getMethod().getExactMatch()).isEqualTo("PUT");
    }

    @Test
    public void shouldBuildPatchRequest() {
        // Given

        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").patch("/").willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        assertThat(Iterables.getLast(pairs).getRequest().getMethod().getExactMatch()).isEqualTo("PATCH");
    }

    @Test
    public void shouldBuildDeleteRequest() {
        // Given

        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").delete("/").willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        assertThat(Iterables.getLast(pairs).getRequest().getMethod().getExactMatch()).isEqualTo("DELETE");
    }

    @Test
    public void shouldAutomaticallyMarshallJson() {
        // When
        final RequestResponsePair requestResponsePair =
                service("www.some-service.com")
                        .post("/path")
                        .body(json(new SomeJson("requestFieldOne", "requestFieldTwo")))
                        .willReturn(success(json(new SomeJson("responseFieldOne", "responseFieldTwo"))))
                        .getRequestResponsePairs()
                        .iterator()
                        .next();

        // Then
        assertThat(requestResponsePair.getRequest())
                .hasExactHeaders(header("Content-Type", "application/json"))
                .hasBody("{\"firstField\":\"requestFieldOne\",\"secondField\":\"requestFieldTwo\"}");

        assertThat(requestResponsePair.getResponse())
                .hasExactHeaders(header("Content-Type", "application/json"))
                .hasBody("{\"firstField\":\"responseFieldOne\",\"secondField\":\"responseFieldTwo\"}");
    }

    @Test
    public void shouldByAbleToConfigureTheObjectMapperWhenMarshallingJson() throws JsonProcessingException {
        // When
        final ObjectMapper objectMapper = spy(new ObjectMapper());

        final RequestResponsePair requestResponsePair =
                service("www.some-service.com")
                        .post("/path")
                        .body(json(new SomeJson("requestFieldOne", "requestFieldTwo"), objectMapper))
                        .willReturn(success())
                        .getRequestResponsePairs()
                        .iterator()
                        .next();

        // Then
        assertThat(requestResponsePair.getRequest())
                .hasExactHeaders(header("Content-Type", "application/json"))
                .hasBody("{\"firstField\":\"requestFieldOne\",\"secondField\":\"requestFieldTwo\"}");

        verify(objectMapper).writeValueAsString(new SomeJson("requestFieldOne", "requestFieldTwo"));
    }

    public static final class SomeJson {
        private final String firstField;
        private final String secondField;

        public SomeJson(final String firstField, final String secondField) {
            this.firstField = firstField;
            this.secondField = secondField;
        }

        public String getFirstField() {
            return firstField;
        }

        public String getSecondField() {
            return secondField;
        }

        @Override
        public boolean equals(Object obj) {
            return EqualsBuilder.reflectionEquals(this, obj);
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }
    }
}