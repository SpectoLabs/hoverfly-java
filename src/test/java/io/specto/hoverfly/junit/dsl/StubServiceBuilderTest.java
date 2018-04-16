package io.specto.hoverfly.junit.dsl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import io.specto.hoverfly.junit.core.model.FieldMatcher;
import io.specto.hoverfly.junit.core.model.RequestResponsePair;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.Test;

import java.util.Set;

import static io.specto.hoverfly.assertions.Assertions.assertThat;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.*;
import static io.specto.hoverfly.junit.dsl.HttpBodyConverter.json;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.*;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class StubServiceBuilderTest {

    @Test
    public void shouldExtractHttpsUrlScheme() {

        final Set<RequestResponsePair> pairs = service("https://www.my-test.com").get("/").willReturn(response()).getRequestResponsePairs();

        assertThat(pairs).hasSize(1);
        RequestResponsePair pair = pairs.iterator().next();
        assertThat(pair.getRequest().getDestination().getExactMatch()).isEqualTo("www.my-test.com");
        assertThat(pair.getRequest().getScheme().getExactMatch()).isEqualTo("https");
    }

    @Test
    public void shouldIgnoreSchemeIfItIsNotSet() {
        final Set<RequestResponsePair> pairs = service("www.my-test.com").get("/").willReturn(response()).getRequestResponsePairs();

        assertThat(pairs).hasSize(1);
        RequestResponsePair pair = pairs.iterator().next();
        assertThat(pair.getRequest().getDestination().getExactMatch()).isEqualTo("www.my-test.com");
        assertThat(pair.getRequest().getScheme()).isNull();

    }

    @Test
    public void shouldExtractHttpScheme() {
        final Set<RequestResponsePair> pairs = service("http://www.my-test.com").get("/").willReturn(response()).getRequestResponsePairs();

        assertThat(pairs).hasSize(1);
        RequestResponsePair pair = pairs.iterator().next();
        assertThat(pair.getRequest().getDestination().getExactMatch()).isEqualTo("www.my-test.com");
        assertThat(pair.getRequest().getScheme().getExactMatch()).isEqualTo("http");
    }


    @Test
    public void shouldBuildExactMatchersForMethod() {
        assertExactMatcherForMethod(service("").get("/"), "GET");
        assertExactMatcherForMethod(service("").post("/"), "POST");
        assertExactMatcherForMethod(service("").put("/"), "PUT");
        assertExactMatcherForMethod(service("").patch("/") , "PATCH");
        assertExactMatcherForMethod(service("").delete("/"), "DELETE");
        assertExactMatcherForMethod(service("").options("/"), "OPTIONS");
        assertExactMatcherForMethod(service("").connect("/"), "CONNECT");
        assertExactMatcherForMethod(service("").head("/"), "HEAD");
    }

    @Test
    public void shouldBuildPathMatcher() {
        assertPathMatcher(service("").get(matches("/api/*/booking")), "/api/*/booking");
        assertPathMatcher(service("").post(matches("/api/*/booking")), "/api/*/booking");
        assertPathMatcher(service("").put(matches("/api/*/booking")), "/api/*/booking");
        assertPathMatcher(service("").patch(matches("/api/*/booking")), "/api/*/booking");
        assertPathMatcher(service("").delete(matches("/api/*/booking")), "/api/*/booking");
        assertPathMatcher(service("").options(matches("/api/*/booking")), "/api/*/booking");
        assertPathMatcher(service("").connect(matches("/api/*/booking")), "/api/*/booking");
        assertPathMatcher(service("").head(matches("/api/*/booking")), "/api/*/booking");
    }

    @Test
    public void shouldBuildAnyMethodRequest() {
        final Set<RequestResponsePair> pairs = service("www.base-url.com").anyMethod("/").willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        assertThat(Iterables.getLast(pairs).getRequest().getMethod()).isNull();
    }

    @Test
    public void shouldBuildAnyMethodRequestWithPathMatcher() {
        final Set<RequestResponsePair> pairs = service("www.base-url.com").anyMethod(matches("/api/*/booking")).willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        assertThat(Iterables.getLast(pairs).getRequest().getPath().getGlobMatch()).isEqualTo("/api/*/booking");
    }

    @Test
    public void shouldBuildExactQueryMatcher() {
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
    public void shouldBuildQueryMatcherWithFuzzyKey() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/").queryParam(any(), "bar")
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        FieldMatcher query = Iterables.getLast(pairs).getRequest().getQuery();
        assertThat(query.getRegexMatch()).isEqualTo(".*=bar");
        assertThat(query.getExactMatch()).isNull();
    }

    @Test
    public void shouldBuildQueryMatcherWithFuzzyValue() {
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
    public void shouldBuildQueryMatcherWithFuzzyKeyAndValue() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/").queryParam(endsWith("token"), any())
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        FieldMatcher query = Iterables.getLast(pairs).getRequest().getQuery();
        assertThat(query.getRegexMatch()).isEqualTo(".*token$=.*");
        assertThat(query.getExactMatch()).isNull();
    }

    @Test
    public void shouldBuildExactQueryWithMultipleKeyValuePairs() {
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
    public void shouldBuildExactQueryForKeyWithMultipleValues() {
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
    public void shouldBuildQueryWithMultipleFuzzyMatchers() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/")
                .queryParam("page", any())
                .queryParam("size", any())
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        FieldMatcher query = Iterables.getLast(pairs).getRequest().getQuery();
        assertThat(query.getRegexMatch()).isEqualTo("page=.*&size=.*");
    }

    @Test
    public void shouldBuildQueryWithBothExactAndFuzzyMatchers() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/")
                .queryParam("page", any())
                .queryParam("category", "food")
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        FieldMatcher query = Iterables.getLast(pairs).getRequest().getQuery();
        assertThat(query.getRegexMatch()).isEqualTo("page=.*&category=food");
    }

    @Test
    public void shouldBuildQueryParamMatcherThatIgnoresValue() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/")
                .queryParam("page")
                .queryParam("size")
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        FieldMatcher query = Iterables.getLast(pairs).getRequest().getQuery();
        assertThat(query.getRegexMatch()).isEqualTo("page=.*&size=.*");
    }

    @Test
    public void shouldBuildAnyQueryMatcher() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/")
                .anyQueryParams()
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        FieldMatcher query = Iterables.getLast(pairs).getRequest().getQuery();
        assertThat(query).isNull();
    }

    @Test
    public void shouldBuildEmptyQueryMatcherWhenQueryParamIsNotSet() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/")
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        FieldMatcher query = Iterables.getLast(pairs).getRequest().getQuery();
        assertThat(query.getExactMatch()).isEqualTo("");
    }

    @Test
    public void shouldNotEncodeSpacesInQueryParams() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/")
                .queryParam("destination", "New York")
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        FieldMatcher query = Iterables.getLast(pairs).getRequest().getQuery();
        assertThat(query.getExactMatch()).isEqualTo("destination=New York");
    }


    @Test
    public void shouldBuildAnyBodyMatcher() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").post("/")
                .anyBody()
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        FieldMatcher body = Iterables.getLast(pairs).getRequest().getBody();
        assertThat(body).isNull();
    }

    @Test
    public void shouldBuildEmptyBodyMatcherWhenBodyIsNotSet() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").post("/")
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        FieldMatcher body = Iterables.getLast(pairs).getRequest().getBody();
        assertThat(body.getExactMatch()).isEqualTo("");
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

    @Test
    public void shouldBuildTemplatedResponseByDefault() {

        final RequestResponsePair pair = service("www.base-url.com")
                .get("/")
                .willReturn(success().body("{\"id\":{{ Request.Path.[2] }}"))
                .getRequestResponsePairs()
                .iterator().next();

        assertThat(pair.getResponse().isTemplated()).isTrue();
    }

    @Test
    public void shouldBeAbleToDisableTemplatedResponse() {

        final RequestResponsePair pair = service("www.base-url.com")
                .get("/")
                .willReturn(success().body("{\"id\":{{ Request.Path.[2] }}").disableTemplating())
                .getRequestResponsePairs()
                .iterator().next();

        assertThat(pair.getResponse().isTemplated()).isFalse();
    }

    @Test
    public void shouldBeAbleToSetTransitionStates() {

        final RequestResponsePair pair = service("www.base-url.com")
            .get("/")
            .willReturn(success().body("{\"id\":{{ Request.Path.[2] }}")
                .andSetState("firstStateKey", "firstStateValue")
                .andSetState("secondStateKey", "secondStateValue"))
            .getRequestResponsePairs()
            .iterator().next();

        assertThat(pair.getResponse().getTransitionsState())
            .containsOnly(
                entry("firstStateKey", "firstStateValue"),
                entry("secondStateKey", "secondStateValue"));
    }

    @Test
    public void shouldBeAbleToSetStatesToRemove() {

        final RequestResponsePair pair = service("www.base-url.com")
            .get("/")
            .willReturn(success().body("{\"id\":{{ Request.Path.[2] }}")
                .andRemoveState("firstStateToRemove")
                .andRemoveState("secondStateToRemove"))
            .getRequestResponsePairs()
            .iterator().next();

        assertThat(pair.getResponse().getRemovesState())
            .containsExactlyInAnyOrder("firstStateToRemove", "secondStateToRemove");
    }

    @Test
    public void shouldBeAbleToSetRequiredStates() {

        final RequestResponsePair pair = service("https://www.my-test.com")
            .get("/")
            .withState("firstStateKey", "firstStateValue")
            .withState("secondStateKey", "secondStateValue")
            .willReturn(response()).getRequestResponsePairs()
            .iterator().next();

        assertThat(pair.getRequest().getRequiresState())
            .containsOnly(
                entry("firstStateKey", "firstStateValue"),
                entry("secondStateKey", "secondStateValue"));
    }

    private void assertExactMatcherForMethod(RequestMatcherBuilder builder, String method) {
        final Set<RequestResponsePair> pairs = builder.willReturn(response()).getRequestResponsePairs();

        assertThat(pairs).hasSize(1);
        assertThat(Iterables.getLast(pairs).getRequest().getMethod().getExactMatch()).isEqualTo(method);
    }

    private void assertPathMatcher(RequestMatcherBuilder builder, String globValue) {
        final Set<RequestResponsePair> pairs = builder.willReturn(response()).getRequestResponsePairs();

        assertThat(pairs).hasSize(1);
        assertThat(Iterables.getLast(pairs).getRequest().getPath().getGlobMatch()).isEqualTo(globValue);
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