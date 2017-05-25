package io.specto.hoverfly.junit.dsl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import io.specto.hoverfly.junit.core.model.FieldMatcher;
import io.specto.hoverfly.junit.core.model.RequestResponsePair;
import io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers;
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
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.any;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.endsWith;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.matches;
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
    public void shouldIgnoreSchemeIfItIsNotSet() throws Exception {
        final Set<RequestResponsePair> pairs = service("www.my-test.com").get("/").willReturn(response()).getRequestResponsePairs();

        assertThat(pairs).hasSize(1);
        RequestResponsePair pair = pairs.iterator().next();
        assertThat(pair.getRequest().getDestination().getExactMatch()).isEqualTo("www.my-test.com");
        assertThat(pair.getRequest().getScheme()).isNull();

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
    public void shouldBuildGetRequestWithPathMatcher() throws Exception {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get(matches("/api/*/booking")).willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        assertThat(Iterables.getLast(pairs).getRequest().getPath().getGlobMatch()).isEqualTo("/api/*/booking");
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
    public void shouldBuildPostRequestWithPathMatcher() throws Exception {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").post(matches("/api/*/booking")).willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        assertThat(Iterables.getLast(pairs).getRequest().getPath().getGlobMatch()).isEqualTo("/api/*/booking");
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
    public void shouldBuildPutRequestWithPathMatcher() throws Exception {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").put(matches("/api/*/booking")).willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        assertThat(Iterables.getLast(pairs).getRequest().getPath().getGlobMatch()).isEqualTo("/api/*/booking");
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
    public void shouldBuildPatchRequestWithPathMatcher() throws Exception {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").patch(matches("/api/*/booking")).willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        assertThat(Iterables.getLast(pairs).getRequest().getPath().getGlobMatch()).isEqualTo("/api/*/booking");
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
    public void shouldBuildDeleteRequestWithPathMatcher() throws Exception {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").delete(matches("/api/*/booking")).willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        assertThat(Iterables.getLast(pairs).getRequest().getPath().getGlobMatch()).isEqualTo("/api/*/booking");
    }

    @Test
    public void shouldBuildAnyMethodRequest() throws Exception {
        final Set<RequestResponsePair> pairs = service("www.base-url.com").anyMethod("/").willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        assertThat(Iterables.getLast(pairs).getRequest().getMethod()).isNull();
    }

    @Test
    public void shouldBuildAnyMethodRequestWithPathMatcher() throws Exception {
        final Set<RequestResponsePair> pairs = service("www.base-url.com").anyMethod(matches("/api/*/booking")).willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        assertThat(Iterables.getLast(pairs).getRequest().getPath().getGlobMatch()).isEqualTo("/api/*/booking");
    }

    @Test
    public void shouldBuildExactQueryMatcher() throws Exception {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/").queryParam("foo", "bar")
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        FieldMatcher query = Iterables.getLast(pairs).getRequest().getQuery();
        assertThat(query.getExactMatch()).isEqualTo("foo=bar");
        assertThat(query.getGlobMatch()).isNull();
    }

    @Test
    public void shouldBuildQueryMatcherWithFuzzyKey() throws Exception {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/").queryParam(any(), "bar")
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        FieldMatcher query = Iterables.getLast(pairs).getRequest().getQuery();
        assertThat(query.getGlobMatch()).isEqualTo("*=bar");
        assertThat(query.getExactMatch()).isNull();
    }

    @Test
    public void shouldBuildQueryMatcherWithFuzzyValue() throws Exception {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/").queryParam("foo", matches("b*r"))
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        FieldMatcher query = Iterables.getLast(pairs).getRequest().getQuery();
        assertThat(query.getGlobMatch()).isEqualTo("foo=b*r");
        assertThat(query.getExactMatch()).isNull();
    }

    @Test
    public void shouldBuildQueryMatcherWithFuzzyKeyAndValue() throws Exception {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/").queryParam(endsWith("token"), any())
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        FieldMatcher query = Iterables.getLast(pairs).getRequest().getQuery();
        assertThat(query.getGlobMatch()).isEqualTo("*token=*");
        assertThat(query.getExactMatch()).isNull();
    }

    @Test
    public void shouldBuildExactQueryWithMultipleKeyValuePairs() throws Exception {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/")
                .queryParam("page", 1)
                .queryParam("size", 10)
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        FieldMatcher query = Iterables.getLast(pairs).getRequest().getQuery();
        assertThat(query.getExactMatch()).isEqualTo("page=1&size=10");
    }

    @Test
    public void shouldBuildExactQueryForKeyWithMultipleValues() throws Exception {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/")
                .queryParam("category", "food", "drink")
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        FieldMatcher query = Iterables.getLast(pairs).getRequest().getQuery();
        assertThat(query.getExactMatch()).isEqualTo("category=food&category=drink");
    }

    @Test
    public void shouldBuildQueryWithMultipleFuzzyMatchers() throws Exception {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/")
                .queryParam("page", any())
                .queryParam("size", any())
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        FieldMatcher query = Iterables.getLast(pairs).getRequest().getQuery();
        assertThat(query.getGlobMatch()).isEqualTo("page=*&size=*");
    }

    @Test
    public void shouldBuildQueryWithBothExactAndFuzzyMatchers() throws Exception {
// When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/")
                .queryParam("page", any())
                .queryParam("category", "food")
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        FieldMatcher query = Iterables.getLast(pairs).getRequest().getQuery();
        assertThat(query.getGlobMatch()).isEqualTo("page=*&category=food");
    }

    @Test
    public void shouldEncodeSpacesInQueryParams() throws Exception {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/")
                .queryParam("destination", "New York")
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        FieldMatcher query = Iterables.getLast(pairs).getRequest().getQuery();
        assertThat(query.getExactMatch()).isEqualTo("destination=New%20York");
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
                .hasBody("{\"firstField\":\"requestFieldOne\",\"secondField\":\"requestFieldTwo\"}");

        assertThat(requestResponsePair.getResponse())
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